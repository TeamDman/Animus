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
 * Horizontal Radius: Configurable (default: 32 blocks)
 * Vertical Depth: Configurable (default: 128 blocks)
 */
@RitualRegister(Constants.Rituals.RELENTLESS_TIDES)
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

        // Get fluid handler capability with error handling
        IFluidHandler fluidHandler;
        try {
            fluidHandler = tankEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).orElse(null);
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
            network.causeNausea();
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
            network.syphon(new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_RELENTLESS_TIDES),
                lpCost
            ), false);
        }
    }

    /**
     * Find a valid position to place fluid and place it
     */
    private BlockPos findValidPlacementPosition(ServerLevel level, BlockPos masterPos, int horizontalRadius, int verticalDepth, Fluid fluidToPlace) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        // Start position is below the master ritual stone
        BlockPos startPos = masterPos.below();

        // Resume search from where we left off
        int startX = state.currentX;
        int startZ = state.currentZ;
        int startY = state.currentY;
        int maxChecksPerTick = 64; // Limit checks per tick to avoid lag
        int checksThisTick = 0;

        // DFS: Search each column fully (vertically) before moving horizontally
        for (int x = startX; x <= horizontalRadius && checksThisTick < maxChecksPerTick; x++) {
            for (int z = (x == startX ? startZ : -horizontalRadius); z <= horizontalRadius && checksThisTick < maxChecksPerTick; z++) {
                // Search vertically down this column
                for (int y = (x == startX && z == startZ ? startY : 0); y < verticalDepth && checksThisTick < maxChecksPerTick; y++) {
                    BlockPos checkPos = startPos.offset(x, -y, z);
                    checksThisTick++;

                    // Update search state to resume from here next tick
                    state.currentX = x;
                    state.currentZ = z;
                    state.currentY = y;

                    if (isValidPlacementSpot(level, checkPos, fluidToPlace, masterPos)) {
                        // Advance to next position for next search
                        state.currentY++;
                        if (state.currentY >= verticalDepth) {
                            state.currentY = 0;
                            state.currentZ++;
                            if (state.currentZ > horizontalRadius) {
                                state.currentZ = -horizontalRadius;
                                state.currentX++;
                            }
                        }
                        return checkPos;
                    }
                }
                // Column complete, move to next Z
                state.currentY = 0;
            }
            // Row complete, move to next X
            state.currentZ = -horizontalRadius;
        }

        // If we've searched the entire range, reset to start over next tick
        if (state.currentX > horizontalRadius) {
            resetSearchState(masterPos);
        }

        return null;
    }


    // Check if a position is valid for placing fluid
    private boolean isValidPlacementSpot(ServerLevel level, BlockPos pos, Fluid fluidToPlace, BlockPos masterPos) {
        // Check cache first
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

    // Track the search state for each ritual to resume where it left off
    private static class SearchState {
        int currentY = 0;
        int currentX = 0;
        int currentZ = 0;
    }
}
