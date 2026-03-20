package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.AnimusStartupConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamdman.animus.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;

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
public class RitualNoliteIgnem extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    public RitualNoliteIgnem() {
        super(
            Constants.Rituals.NOLITE_IGNEM,
            0,
            5000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.NOLITE_IGNEM
        );

        int radius = AnimusStartupConfig.ritualRanges.noliteIgnemRadius.get();
        int size = radius * 2 + 1;

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-radius, -radius, -radius), size, size, size));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, radius + 20, radius + 20);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return;
        }

        ISoulNetwork network = AnimusRitualHelper.getOwnerNetwork(mrs);
        if (network == null) {
            return;
        }

        int lpPerFire = AnimusConfig.rituals.noliteIgnemLPPerFire.get();

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        List<BlockPos> fireBlocks = new ArrayList<>();

        for (BlockPos pos : effectRange.getContainedPositions(masterPos)) {
            BlockState state = level.getBlockState(pos);

            if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                fireBlocks.add(pos.immutable());
            }
        }

        if (fireBlocks.isEmpty()) {
            return;
        }

        int totalCost = fireBlocks.size() * lpPerFire;

        int currentEssence = network.getCurrentEssence();
        if (currentEssence < totalCost) {
            int affordableFires = currentEssence / lpPerFire;
            if (affordableFires > 0) {
                for (int i = 0; i < affordableFires && i < fireBlocks.size(); i++) {
                    level.removeBlock(fireBlocks.get(i), false);
                }

                network.syphon(SoulTicket.create(affordableFires * lpPerFire));
            }
            return;
        }

        network.syphon(SoulTicket.create(totalCost));
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
        return 20;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);

        addRune(components, -2, 0, -2, EnumRuneType.WATER);
        addRune(components, -2, 0, 2, EnumRuneType.WATER);
        addRune(components, 2, 0, -2, EnumRuneType.WATER);
        addRune(components, 2, 0, 2, EnumRuneType.WATER);

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
