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
 * Ritual of Siphon (Fluid Pump)
 * Extracts fluids from the world below the ritual stone and places them into a tank above
 * Uses an optimized perimeter-based search algorithm for finding fluid sources
 * Activation Cost: 5000 LP
 * Refresh Cost: Configurable (default: 50 LP per extraction)
 * Refresh Time: 10 ticks (0.5 seconds)
 * Horizontal Radius: Configurable (default: 32 blocks)
 * Vertical Depth: Configurable (default: 128 blocks)
 */
@RitualRegister(Constants.Rituals.SIPHON)
public class RitualSiphon extends Ritual {
    // Track current search position for each ritual to resume searching where we left off
    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();

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

        // Find a fluid source to extract below the ritual stone
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
        int filled = fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
        if (filled < BUCKET_AMOUNT) {
            // Tank is full or doesn't accept this fluid type
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        // Check if we have enough LP
        int lpCost = AnimusConfig.rituals.siphonLPPerExtraction.get();
        int currentEssence = network.getCurrentEssence();
        if (currentEssence < lpCost) {
            network.causeNausea();
            return;
        }

        // Actually fill the tank
        int actualFilled = fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        if (actualFilled < BUCKET_AMOUNT) {
            return;
        }

        // Remove the fluid source block from the world
        level.setBlockAndUpdate(fluidPos, Blocks.AIR.defaultBlockState());

        // Consume LP
        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_SIPHON),
            lpCost
        ), false);
    }

    /**
     * Find a fluid source block using an optimized perimeter-based search
     * Searches in expanding square rings starting from below the ritual stone
     * Also searches vertically downward in each column
     */
    private BlockPos findFluidSource(ServerLevel level, BlockPos masterPos, int horizontalRadius, int verticalDepth) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        // Start position is below the master ritual stone
        BlockPos startPos = masterPos.below();

        // Resume search from where we left off
        int startRing = state.currentRing;
        int maxChecksPerTick = 64; // Limit checks per tick to avoid lag
        int checksThisTick = 0;

        // Search in expanding square rings
        for (int ring = startRing; ring <= horizontalRadius && checksThisTick < maxChecksPerTick; ring++) {
            state.currentRing = ring;

            if (ring == 0) {
                // Check the center column (vertically downward)
                for (int y = 0; y < verticalDepth && checksThisTick < maxChecksPerTick; y++) {
                    BlockPos checkPos = startPos.below(y);
                    checksThisTick++;
                    if (isFluidSource(level, checkPos)) {
                        resetSearchState(masterPos);
                        return checkPos;
                    }
                }
                continue;
            }

            // Check the perimeter of the current ring (with vertical depth)
            // This creates a square pattern expanding outward
            for (int x = -ring; x <= ring && checksThisTick < maxChecksPerTick; x++) {
                for (int z = -ring; z <= ring && checksThisTick < maxChecksPerTick; z++) {
                    // Only check perimeter blocks (not interior)
                    if (Math.abs(x) != ring && Math.abs(z) != ring) {
                        continue;
                    }

                    // For each perimeter position, check vertically downward
                    for (int y = 0; y < verticalDepth && checksThisTick < maxChecksPerTick; y++) {
                        BlockPos checkPos = startPos.offset(x, -y, z);
                        checksThisTick++;

                        if (isFluidSource(level, checkPos)) {
                            resetSearchState(masterPos);
                            return checkPos;
                        }
                    }
                }
            }
        }

        // If we've searched the entire range, reset to start over next tick
        if (state.currentRing > horizontalRadius) {
            resetSearchState(masterPos);
        }

        return null;
    }

    /**
     * Check if a position contains a fluid source block
     */
    private boolean isFluidSource(ServerLevel level, BlockPos pos) {
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
     * Clean up search state when ritual stops
     */
    public void onRitualStopped(Level level, BlockPos masterPos) {
        searchStates.remove(masterPos);
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
        return new RitualSiphon();
    }

    /**
     * Track the search state for each ritual to resume where it left off
     */
    private static class SearchState {
        int currentRing = 0;
    }
}
