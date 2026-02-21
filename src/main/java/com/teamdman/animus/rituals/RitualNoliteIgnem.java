package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ritual of Nolite Ignem (Do Not Burn) - Fire Suppression
 * Extinguishes all fires within a configurable radius
 * Activation Cost: 5000 LP
 * Refresh Cost: Configurable (default: 10 LP per fire)
 * Refresh Time: 20 ticks (1 second)
 * Range: Configurable (default: 64 blocks)
 */
@RitualRegister(Constants.Rituals.NOLITE_IGNEM)
public class RitualNoliteIgnem extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    public RitualNoliteIgnem() {
        super(
            Constants.Rituals.NOLITE_IGNEM,
            0,
            5000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.NOLITE_IGNEM
        );
        
        int radius = AnimusConfig.rituals.noliteIgnemRadius.get();
        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(
            new BlockPos(-radius, -radius, -radius), radius * 2 + 1));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return;
        }
        // Check if ritual is enabled
        if (!AnimusConfig.rituals.noliteIgnemEnabled.get()) {
            return;
        }


        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        // Get configuration
        int lpPerFire = AnimusConfig.rituals.noliteIgnemLPPerFire.get();

        // Find all fire blocks in range (respects Ritual Tinkerer modifications)
        AreaDescriptor effectRange = mrs.getBlockRange(EFFECT_RANGE);
        AABB aabb = effectRange.getAABB(masterPos);
        List<BlockPos> fireBlocks = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
            BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ),
            BlockPos.containing(aabb.maxX, aabb.maxY, aabb.maxZ)
        )) {

            BlockState state = level.getBlockState(pos);

            // Check if it's a fire block
            if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                fireBlocks.add(pos.immutable());
            }
        }

        // If no fires, nothing to do
        if (fireBlocks.isEmpty()) {
            return;
        }

        // Calculate total LP cost
        int totalCost = fireBlocks.size() * lpPerFire;

        // Check if we have enough LP
        int currentEssence = network.getCurrentEssence();
        if (currentEssence < totalCost) {
            // Not enough LP - only extinguish what we can afford
            int affordableFires = currentEssence / lpPerFire;
            if (affordableFires > 0) {
                // Extinguish what we can afford
                for (int i = 0; i < affordableFires && i < fireBlocks.size(); i++) {
                    level.removeBlock(fireBlocks.get(i), false);
                }

                // Consume available LP
                network.syphon(new SoulTicket(
                    Component.translatable(Constants.Localizations.Text.TICKET_NOLITE_IGNEM),
                    affordableFires * lpPerFire
                ), false);
            }
            network.causeNausea();
            return;
        }

        // Consume LP
        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_NOLITE_IGNEM),
            totalCost
        ), false);

        // Extinguish all fires
        for (BlockPos pos : fireBlocks) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public int getRefreshCost() {
        // Cost is calculated per fire, not a flat rate
        return 0;
    }

    @Override
    public int getRefreshTime() {
        return 20; // 1 second
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a pattern with water runes (to extinguish fire)
        // and air runes (for range)

        // Inner circle with water runes
        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);

        // Middle ring with more water runes
        addRune(components, -2, 0, -2, EnumRuneType.WATER);
        addRune(components, -2, 0, 2, EnumRuneType.WATER);
        addRune(components, 2, 0, -2, EnumRuneType.WATER);
        addRune(components, 2, 0, 2, EnumRuneType.WATER);

        // Outer corners with air runes for extended range
        addRune(components, -3, 0, -3, EnumRuneType.AIR);
        addRune(components, -3, 0, 3, EnumRuneType.AIR);
        addRune(components, 3, 0, -3, EnumRuneType.AIR);
        addRune(components, 3, 0, 3, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualNoliteIgnem();
    }
}
