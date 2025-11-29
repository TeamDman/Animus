package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Relentless Tides (Floodgate)
 * Extracts fluids from a tank above the ritual stone and places them in the world below
 * Uses an optimized perimeter-based search algorithm for finding valid placement spots
 * Activation Cost: 5000 LP
 * Refresh Cost: Configurable (default: 50 LP per placement)
 * Refresh Time: 10 ticks (0.5 seconds)
 * Range: Configurable (default: 16 blocks)
 */
@RitualRegister(Constants.Rituals.RELENTLESS_TIDES)
public class RitualRelentlessTides extends Ritual {
    // Track current search position for each ritual to resume searching where we left off
    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();

    // Amount of fluid to extract/place per operation (1 bucket = 1000mB)
    private static final int BUCKET_AMOUNT = 1000;

    public RitualRelentlessTides() {
        super(
            Constants.Rituals.RELENTLESS_TIDES,
            0,
            5000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.RELENTLESS_TIDES
        );
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        // Check for fluid tank above the ritual stone
        BlockPos tankPos = masterPos.above();
        BlockEntity tankEntity = level.getBlockEntity(tankPos);

        if (tankEntity == null) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Get fluid handler capability
        IFluidHandler fluidHandler = tankEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).orElse(null);
        if (fluidHandler == null) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Try to extract a bucket's worth of fluid (simulate first)
        FluidStack extractedFluid = fluidHandler.drain(BUCKET_AMOUNT, IFluidHandler.FluidAction.SIMULATE);
        if (extractedFluid.isEmpty() || extractedFluid.getAmount() < BUCKET_AMOUNT) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Find a valid placement position below the ritual stone
        int range = AnimusConfig.rituals.relentlessTidesRange.get();
        BlockPos placementPos = findValidPlacementPosition(serverLevel, masterPos, range);

        if (placementPos == null) {
            // No valid placement found, emit smoke
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Check if we have enough LP
        int lpCost = AnimusConfig.rituals.relentlessTidesLPPerPlacement.get();
        int currentEssence = network.getCurrentEssence();
        if (currentEssence < lpCost) {
            network.causeNausea();
            return;
        }

        // Actually extract the fluid
        FluidStack actualExtracted = fluidHandler.drain(BUCKET_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
        if (actualExtracted.isEmpty() || actualExtracted.getAmount() < BUCKET_AMOUNT) {
            return;
        }

        // Place the fluid in the world
        Fluid fluid = actualExtracted.getFluid();
        Block fluidBlock = fluid.defaultFluidState().createLegacyBlock().getBlock();

        if (fluidBlock instanceof LiquidBlock) {
            // Place as source block (level 0)
            BlockState fluidState = fluidBlock.defaultBlockState();
            level.setBlockAndUpdate(placementPos, fluidState);

            // Consume LP
            network.syphon(new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_RELENTLESS_TIDES),
                lpCost
            ), false);
        }
    }

    /**
     * Find a valid position to place fluid using an optimized perimeter-based search
     * Searches in expanding square rings starting from below the ritual stone
     */
    private BlockPos findValidPlacementPosition(ServerLevel level, BlockPos masterPos, int range) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        // Start position is below the master ritual stone
        BlockPos startPos = masterPos.below();

        // Resume search from where we left off
        int startRing = state.currentRing;
        int maxChecksPerTick = 64; // Limit checks per tick to avoid lag
        int checksThisTick = 0;

        // Search in expanding square rings
        for (int ring = startRing; ring <= range && checksThisTick < maxChecksPerTick; ring++) {
            state.currentRing = ring;

            if (ring == 0) {
                // Check the center position
                BlockPos checkPos = startPos;
                if (isValidPlacementSpot(level, checkPos)) {
                    resetSearchState(masterPos);
                    return checkPos;
                }
                checksThisTick++;
                continue;
            }

            // Check the perimeter of the current ring
            // This creates a square pattern expanding outward
            for (int x = -ring; x <= ring && checksThisTick < maxChecksPerTick; x++) {
                for (int z = -ring; z <= ring && checksThisTick < maxChecksPerTick; z++) {
                    // Only check perimeter blocks (not interior)
                    if (Math.abs(x) != ring && Math.abs(z) != ring) {
                        continue;
                    }

                    BlockPos checkPos = startPos.offset(x, 0, z);
                    checksThisTick++;

                    if (isValidPlacementSpot(level, checkPos)) {
                        resetSearchState(masterPos);
                        return checkPos;
                    }
                }
            }
        }

        // If we've searched the entire range, reset to start over next tick
        if (state.currentRing > range) {
            resetSearchState(masterPos);
        }

        return null;
    }

    /**
     * Check if a position is valid for placing fluid
     * Valid positions are: air, replaceable blocks, or existing fluid of lower level
     */
    private boolean isValidPlacementSpot(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        // Check if it's air or replaceable
        if (state.isAir() || state.canBeReplaced()) {
            // Also check that there's a solid block below to prevent infinite falling fluids
            BlockState below = level.getBlockState(pos.below());
            return !below.isAir() && below.getFluidState().isEmpty();
        }

        // Check if it's existing fluid that we can replace (flowing fluid)
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            // Only replace flowing fluids, not source blocks
            return !fluidState.isSource();
        }

        return false;
    }

    /**
     * Emit smoke particles from the master ritual stone when unable to place fluid
     */
    private void emitSmokeParticles(ServerLevel level, BlockPos pos) {
        // Emit smoke particles at the ritual stone
        RandomSource random = level.getRandom();
        for (int i = 0; i < 5; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            level.sendParticles(
                ParticleTypes.SMOKE,
                x, y, z,
                1,
                0.0, 0.05, 0.0,
                0.01
            );
        }
    }

    /**
     * Reset the search state for a ritual position
     */
    private void resetSearchState(BlockPos pos) {
        searchStates.remove(pos);
    }

    /**
     * Clean up search state when ritual stops
     */
    public void onRitualStopped(Level level, BlockPos masterPos) {
        searchStates.remove(masterPos);
    }

    @Override
    public int getRefreshCost() {
        return 0; // Cost is per placement, not a flat refresh
    }

    @Override
    public int getRefreshTime() {
        return 10; // 0.5 seconds
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a water-themed pattern for fluid manipulation
        // Water runes represent fluid control
        // Air runes represent flow and movement

        // Inner circle with water runes (cardinal directions)
        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);

        // Middle ring with air runes (diagonals) for flow
        addRune(components, -2, 0, -2, EnumRuneType.AIR);
        addRune(components, -2, 0, 2, EnumRuneType.AIR);
        addRune(components, 2, 0, -2, EnumRuneType.AIR);
        addRune(components, 2, 0, 2, EnumRuneType.AIR);

        // Outer ring with more water runes for extended range
        addRune(components, 0, 0, -3, EnumRuneType.WATER);
        addRune(components, 0, 0, 3, EnumRuneType.WATER);
        addRune(components, -3, 0, 0, EnumRuneType.WATER);
        addRune(components, 3, 0, 0, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualRelentlessTides();
    }

    /**
     * Track the search state for each ritual to resume where it left off
     */
    private static class SearchState {
        int currentRing = 0;
    }
}
