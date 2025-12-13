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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Relentless Tides (Floodgate)
 * Extracts fluids from a tank above the ritual stone and places them in the world below
 * Uses an optimized perimeter-based search algorithm for finding valid placement spots
 * Activation Cost: 5000 LP
 * Refresh Cost: Configurable (default: 50 LP per placement)
 * Refresh Time: 10 ticks (0.5 seconds)
 * Horizontal Radius: Configurable (default: 32 blocks)
 * Vertical Depth: Configurable (default: 128 blocks)
 */
public class RitualRelentlessTides extends Ritual {
    // Track current search position for each ritual to resume searching where we left off
    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();

    // Cache of filled positions to skip redundant checks
    private static final Map<BlockPos, Set<BlockPos>> filledPositionsCache = new HashMap<>();

    // Amount of fluid to extract/place per operation
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

        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(mrs.getOwner());
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

        // Get fluid handler capability with error handling
        IFluidHandler fluidHandler;
        try {
            fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, tankPos, Direction.DOWN);
            if (fluidHandler == null) {
                emitSmokeParticles(serverLevel, masterPos);
                return;
            }
        } catch (Exception e) {
            // Handle capability errors (in case the tank is removed)
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Try to extract fluid by simulation first
        FluidStack extractedFluid;
        try {
            extractedFluid = fluidHandler.drain(BUCKET_AMOUNT, IFluidHandler.FluidAction.SIMULATE);
            if (extractedFluid.isEmpty() || extractedFluid.getAmount() < BUCKET_AMOUNT) {
                emitSmokeParticles(serverLevel, masterPos);
                return;
            }
        } catch (Exception e) {
            // Handle any drain errors
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Find a valid placement position below the ritual stone
        int horizontalRadius = AnimusConfig.rituals.relentlessTidesRange.get();
        int verticalDepth = AnimusConfig.rituals.relentlessTidesDepth.get();
        Fluid fluidToPlace = extractedFluid.getFluid();
        BlockPos placementPos = findValidPlacementPosition(serverLevel, masterPos, horizontalRadius, verticalDepth, fluidToPlace);

        if (placementPos == null) {
            // No valid placement found, emit smoke
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Check if we have enough LP
        int lpCost = AnimusConfig.rituals.relentlessTidesLPPerPlacement.get();
        int currentEssence = network.getCurrentEssence();
        if (currentEssence < lpCost) {
            // Note: causeNausea removed in BM 4.0
            return;
        }

        // Actually extract the fluid
        FluidStack actualExtracted;
        try {
            actualExtracted = fluidHandler.drain(BUCKET_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
            if (actualExtracted.isEmpty() || actualExtracted.getAmount() < BUCKET_AMOUNT) {
                return;
            }
        } catch (Exception e) {
            // Handle extraction errors
            return;
        }

        // Place the fluid in the world
        Fluid fluid = actualExtracted.getFluid();
        Block fluidBlock = fluid.defaultFluidState().createLegacyBlock().getBlock();

        if (fluidBlock instanceof LiquidBlock) {
            // Place as source block (level 0)
            BlockState fluidState = fluidBlock.defaultBlockState();
            level.setBlockAndUpdate(placementPos, fluidState);

            // Add to cache to skip this position in future searches
            Set<BlockPos> filledPositions = filledPositionsCache.computeIfAbsent(
                masterPos.immutable(),
                k -> new java.util.HashSet<>()
            );
            filledPositions.add(placementPos.immutable());

            // Consume LP
            network.syphon(SoulTicket.create(lpCost));
        }
    }

    /**
     * Find a valid position to place fluid using center-outward search
     * Starts directly below the master ritual stone and expands outward in square rings
     * Searches each column vertically before moving to the next position
     */
    private BlockPos findValidPlacementPosition(ServerLevel level, BlockPos masterPos, int horizontalRadius, int verticalDepth, Fluid fluidToPlace) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        // Start position is below the master ritual stone
        BlockPos startPos = masterPos.below();

        int maxChecksPerTick = 64; // Limit checks per tick to avoid lag
        int checksThisTick = 0;

        // Search by expanding square rings from center outward
        for (int radius = state.currentRadius; radius <= horizontalRadius && checksThisTick < maxChecksPerTick; radius++) {
            // Iterate through all positions in this radius ring
            for (int x = -radius; x <= radius && checksThisTick < maxChecksPerTick; x++) {
                // Skip if we're not resuming from this X position
                if (radius == state.currentRadius && x < state.currentX) continue;

                for (int z = -radius; z <= radius && checksThisTick < maxChecksPerTick; z++) {
                    // Skip if we're not resuming from this Z position
                    if (radius == state.currentRadius && x == state.currentX && z < state.currentZ) continue;

                    // Only check positions on the perimeter of this radius ring
                    // (except for radius 0, which is just the center point)
                    if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    // Search vertically down this column
                    int startY = (radius == state.currentRadius && x == state.currentX && z == state.currentZ) ? state.currentY : 0;
                    for (int y = startY; y < verticalDepth && checksThisTick < maxChecksPerTick; y++) {
                        BlockPos checkPos = startPos.offset(x, -y, z);
                        checksThisTick++;

                        // Update search state to resume from here next tick
                        state.currentRadius = radius;
                        state.currentX = x;
                        state.currentZ = z;
                        state.currentY = y;

                        if (isValidPlacementSpot(level, checkPos, fluidToPlace, masterPos)) {
                            // Advance to next position for next search
                            state.currentY++;
                            if (state.currentY >= verticalDepth) {
                                state.currentY = 0;
                                state.currentZ++;
                                if (state.currentZ > radius) {
                                    state.currentZ = -radius;
                                    state.currentX++;
                                    if (state.currentX > radius) {
                                        state.currentX = -radius;
                                        state.currentZ = -radius;
                                        state.currentRadius++;
                                    }
                                }
                            }
                            return checkPos;
                        }
                    }
                    // Column complete, reset Y for next column
                    state.currentY = 0;
                }
                // Row complete, reset Z for next row
                state.currentZ = -radius;
            }
        }

        // If we've searched the entire range, reset to start over next tick
        if (state.currentRadius > horizontalRadius) {
            resetSearchState(masterPos);
        }

        return null;
    }


    // Check if a position is valid for placing fluid
    private boolean isValidPlacementSpot(ServerLevel level, BlockPos pos, Fluid fluidToPlace, BlockPos masterPos) {
        // Check cache first - skip positions we've already filled
        Set<BlockPos> filledPositions = filledPositionsCache.get(masterPos);
        if (filledPositions != null && filledPositions.contains(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();

        // Don't place if it's already a source block of the same fluid type
        if (!fluidState.isEmpty() && fluidState.isSource() && fluidState.getType() == fluidToPlace) {
            return false;
        }

        // Can place anywhere that's air, replaceable, or contains flowing fluid
        // We don't care if there's air below - let Minecraft's fluid physics handle it
        // Block replacement is handled by setBlockAndUpdate in performRitual
        return state.isAir() || state.canBeReplaced() || (!fluidState.isEmpty() && !fluidState.isSource());
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
     * Clean up search state and cache when ritual stops
     */
    public void onRitualStopped(Level level, BlockPos masterPos) {
        searchStates.remove(masterPos);
        filledPositionsCache.remove(masterPos);
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
     * Searches from center outward in expanding square rings
     */
    private static class SearchState {
        int currentRadius = 0; // Start from center (directly below ritual stone)
        int currentX = 0; // X position within current radius ring
        int currentZ = 0; // Z position within current radius ring
        int currentY = 0; // Vertical position (0 = ritual stone level, increases downward)
    }
}
