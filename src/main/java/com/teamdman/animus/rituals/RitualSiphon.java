package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Siphon (Fluid Pump)
 * Extracts fluids from the world below the ritual stone and places them into a tank above
 * Uses a center-outward search algorithm, starting directly below the ritual stone
 * Replaces extracted fluids with configurable replacement block (default: antilife)
 * Activation Cost: 5000 LP
 * Refresh Cost: Configurable (default: 50 LP per extraction)
 * Refresh Time: 10 ticks (0.5 seconds)
 * Horizontal Radius: Configurable (default: 32 blocks)
 * Vertical Depth: Configurable (default: 128 blocks)
 * Replacement Block: Configurable (default: animus:block_antilife)
 */
public class RitualSiphon extends Ritual {
    private static final Logger LOGGER = LoggerFactory.getLogger(RitualSiphon.class);

    // Track current search position for each ritual to resume searching where we left off
    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();

    // Cache of extracted positions to skip redundant checks
    private static final Map<BlockPos, Set<BlockPos>> extractedPositionsCache = new HashMap<>();

    // Amount of fluid to extract/place per operation (1 bucket = 1000mB)
    private static final int BUCKET_AMOUNT = 1000;

    public RitualSiphon() {
        super(
            Constants.Rituals.SIPHON,
            0,
            5000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SIPHON
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
            // Handle capability errors (e.g., when tank is removed)
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Find a fluid source to extract below the ritual stone (center-outward search)
        int horizontalRadius = AnimusConfig.rituals.siphonRange.get();
        int verticalDepth = AnimusConfig.rituals.siphonDepth.get();
        BlockPos fluidPos = findFluidSource(serverLevel, masterPos, horizontalRadius, verticalDepth);

        if (fluidPos == null) {
            // No fluid found, emit smoke
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Get the fluid at this position
        BlockState fluidState = level.getBlockState(fluidPos);
        FluidState fluidStateData = fluidState.getFluidState();

        if (fluidStateData.isEmpty() || !fluidStateData.isSource()) {
            // Not a valid source block, try again next tick
            return;
        }

        // Create fluid stack for the fluid we found
        FluidStack fluidStack = new FluidStack(fluidStateData.getType(), BUCKET_AMOUNT);

        // Try to fill the tank (simulate first)
        int filled;
        try {
            filled = fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
            if (filled < BUCKET_AMOUNT) {
                // Tank is full or doesn't accept this fluid type
                emitSmokeParticles(serverLevel, masterPos);
                return;
            }
        } catch (Exception e) {
            // Handle fill errors
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Check if we have enough LP
        int lpCost = AnimusConfig.rituals.siphonLPPerExtraction.get();
        int currentEssence = network.getCurrentEssence();
        if (currentEssence < lpCost) {
            // Note: causeNausea removed in BM 4.0
            return;
        }

        // Actually fill the tank
        int actualFilled;
        try {
            actualFilled = fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            if (actualFilled < BUCKET_AMOUNT) {
                return;
            }
        } catch (Exception e) {
            // Handle fill errors
            return;
        }

        // Replace the fluid source block with configured replacement block
        level.setBlockAndUpdate(fluidPos, getReplacementBlock());

        // Add to cache to skip this position in future searches
        Set<BlockPos> extractedPositions = extractedPositionsCache.computeIfAbsent(
            masterPos.immutable(),
            k -> new java.util.HashSet<>()
        );
        extractedPositions.add(fluidPos.immutable());

        // Consume LP
        network.syphon(SoulTicket.create(lpCost));
    }

    /**
     * Find a fluid source block using a DFS column-by-column search from center outward
     * Starts directly below the master ritual stone and expands outward in square rings
     * Searches each column vertically before moving to the next position
     */
    private BlockPos findFluidSource(ServerLevel level, BlockPos masterPos, int horizontalRadius, int verticalDepth) {
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

                        if (isFluidSource(level, checkPos, masterPos)) {
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

    /**
     * Check if a position contains a fluid source block
     * Uses cache to skip already-extracted positions for performance
     */
    private boolean isFluidSource(ServerLevel level, BlockPos pos, BlockPos masterPos) {
        // Check cache first - if we've already extracted from this position, skip it
        Set<BlockPos> extractedPositions = extractedPositionsCache.get(masterPos);
        if (extractedPositions != null && extractedPositions.contains(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);

        // Check if it's a liquid block
        if (!(state.getBlock() instanceof LiquidBlock)) {
            return false;
        }

        // Check if it's a source block (level 0)
        FluidState fluidState = state.getFluidState();
        return fluidState.isSource();
    }

    /**
     * Get the replacement block state from config
     * Falls back to antilife block if config value is invalid
     */
    private BlockState getReplacementBlock() {
        String blockId = AnimusConfig.rituals.siphonReplacementBlock.get();
        try {
            net.minecraft.resources.ResourceLocation resourceLocation =
                net.minecraft.resources.ResourceLocation.tryParse(blockId);

            if (resourceLocation != null) {
                net.minecraft.world.level.block.Block block =
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(resourceLocation).orElse(null);

                if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                    return block.defaultBlockState();
                }
            }

            // Log warning if block not found
            LOGGER.warn("[Siphon] Configured replacement block '{}' not found, using antilife block", blockId);
        } catch (Exception e) {
            LOGGER.warn("[Siphon] Invalid replacement block ID '{}', using antilife block: {}",
                blockId, e.getMessage());
        }

        // Fall back to antilife block
        return AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState();
    }

    /**
     * Emit smoke particles from the master ritual stone when unable to extract fluid
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
        extractedPositionsCache.remove(masterPos);
    }

    @Override
    public int getRefreshCost() {
        return 0; // Cost is per extraction, not a flat refresh
    }

    @Override
    public int getRefreshTime() {
        return 10; // 0.5 seconds
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a water-themed pattern for fluid manipulation (reverse of Relentless Tides)
        // Water runes represent fluid control
        // Air runes represent suction and upward flow

        // Inner circle with water runes (cardinal directions)
        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);

        // Middle ring with air runes (diagonals) for upward flow
        addRune(components, -2, 0, -2, EnumRuneType.EARTH);
        addRune(components, -2, 0, 2, EnumRuneType.EARTH);
        addRune(components, 2, 0, -2, EnumRuneType.EARTH);
        addRune(components, 2, 0, 2, EnumRuneType.EARTH);

        // Outer ring with more water runes for extended range
        addRune(components, 0, 0, -3, EnumRuneType.WATER);
        addRune(components, 0, 0, 3, EnumRuneType.WATER);
        addRune(components, -3, 0, 0, EnumRuneType.WATER);
        addRune(components, 3, 0, 0, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSiphon();
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
