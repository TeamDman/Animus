package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.items.sigils.ItemSigilTemporalDominance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ritual of Floral Supremacy
 *
 * Supercharges nearby Botania mana-generating flowers as if they were on enchanted soil
 * and accelerates mana spreaders using temporal dominance.
 *
 * Activation Cost: 10000 LP
 * Refresh Cost: 50 LP per flower (configurable)
 * Refresh Time: 20 ticks (1 second)
 * Radius: 8 blocks (configurable)
 *
 * Effects:
 * - Doubles tick rate of Botania mana-generating flowers (as if on enchanted soil)
 * - 2x tick accelerates mana spreaders in the area
 *
 * Requires Botania to be installed
 */
@RitualRegister(Constants.Rituals.FLORAL_SUPREMACY)
public class RitualFloralSupremacy extends Ritual {

    public RitualFloralSupremacy() {
        super(
            Constants.Rituals.FLORAL_SUPREMACY,
            0,
            10000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.FLORAL_SUPREMACY
        );
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Check if Botania is loaded
        if (!ModList.get().isLoaded("botania")) {
            return;
        }

        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        int radius = AnimusConfig.rituals.floralSupremacyRadius.get();

        // Find all flowers and spreaders in range
        List<GeneratingFlowerBlockEntity> flowers = new ArrayList<>();
        List<BlockPos> spreaderPositions = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
            masterPos.offset(-radius, -radius, -radius),
            masterPos.offset(radius, radius, radius)
        )) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof GeneratingFlowerBlockEntity flower) {
                flowers.add(flower);
            } else if (blockEntity != null && blockEntity.getClass().getName().contains("ManaSpreaderBlockEntity")) {
                spreaderPositions.add(pos.immutable());
            }
        }

        // Calculate total LP cost based on number of flowers
        int lpPerFlower = AnimusConfig.rituals.floralSupremacyLPPerFlower.get();
        int totalLPCost = flowers.size() * lpPerFlower;

        if (totalLPCost == 0) {
            // No flowers found, don't consume LP
            return;
        }

        // Check if we have enough LP
        int currentEssence = network.getCurrentEssence();
        if (currentEssence < totalLPCost) {
            network.causeNausea();
            return;
        }

        // Consume LP
        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_FLORAL_SUPREMACY),
            totalLPCost
        ), false);

        // Supercharge flowers (double tick = call tickFlower once extra)
        for (GeneratingFlowerBlockEntity flower : flowers) {
            try {
                flower.tickFlower();
            } catch (Exception e) {
                // Ignore errors from individual flowers
            }
        }

        // Accelerate mana spreaders using temporal dominance system
        long expiryTime = level.getGameTime() + 40; // 2 seconds
        for (BlockPos spreaderPos : spreaderPositions) {
            try {
                // Add to temporal dominance with level 1 (2x speed)
                ItemSigilTemporalDominance.AccelerationState state =
                    new ItemSigilTemporalDominance.AccelerationState(1, expiryTime, level.dimension());

                // Access the acceleratedBlocks map via reflection since it's private
                java.lang.reflect.Field acceleratedBlocksField =
                    ItemSigilTemporalDominance.class.getDeclaredField("acceleratedBlocks");
                acceleratedBlocksField.setAccessible(true);

                @SuppressWarnings("unchecked")
                java.util.Map<BlockPos, ItemSigilTemporalDominance.AccelerationState> acceleratedBlocks =
                    (java.util.Map<BlockPos, ItemSigilTemporalDominance.AccelerationState>)
                    acceleratedBlocksField.get(null);

                acceleratedBlocks.put(spreaderPos, state);
            } catch (Exception e) {
                // Ignore reflection errors
            }
        }
    }

    @Override
    public int getRefreshCost() {
        return 0; // Cost is per flower, not a flat refresh
    }

    @Override
    public int getRefreshTime() {
        return 20; // 1 second
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a nature-themed pattern for botanical enhancement
        // Earth runes represent nature and growth
        // Water runes represent life force and mana
        // Air runes represent acceleration and flow

        // Inner circle with earth runes (cardinal directions) for nature
        addRune(components, 0, 0, -2, EnumRuneType.EARTH);
        addRune(components, 0, 0, 2, EnumRuneType.EARTH);
        addRune(components, -2, 0, 0, EnumRuneType.EARTH);
        addRune(components, 2, 0, 0, EnumRuneType.EARTH);

        // Middle ring with water runes (diagonals) for mana flow
        addRune(components, -2, 0, -2, EnumRuneType.WATER);
        addRune(components, -2, 0, 2, EnumRuneType.WATER);
        addRune(components, 2, 0, -2, EnumRuneType.WATER);
        addRune(components, 2, 0, 2, EnumRuneType.WATER);

        // Outer ring with air runes for acceleration
        addRune(components, 0, 0, -3, EnumRuneType.AIR);
        addRune(components, 0, 0, 3, EnumRuneType.AIR);
        addRune(components, -3, 0, 0, EnumRuneType.AIR);
        addRune(components, 3, 0, 0, EnumRuneType.AIR);

        // Additional earth runes at outer corners for extended range
        addRune(components, -3, 0, -3, EnumRuneType.EARTH);
        addRune(components, -3, 0, 3, EnumRuneType.EARTH);
        addRune(components, 3, 0, -3, EnumRuneType.EARTH);
        addRune(components, 3, 0, 3, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualFloralSupremacy();
    }
}
