package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.AnimusStartupConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import com.teamdman.animus.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;

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
    public static final String EFFECT_RANGE = "effect";
    public static final String TANK_RANGE = "tank";

    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();
    private static final Map<BlockPos, Set<BlockPos>> filledPositionsCache = new HashMap<>();
    private static final int BUCKET_AMOUNT = 1000;

    public RitualRelentlessTides() {
        super(
            Constants.Rituals.RELENTLESS_TIDES,
            0,
            5000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.RELENTLESS_TIDES
        );

        int hRadius = AnimusStartupConfig.ritualRanges.relentlessTidesRange.get();
        int vDepth = AnimusStartupConfig.ritualRanges.relentlessTidesDepth.get();
        int hSize = hRadius * 2 + 1;

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-hRadius, -vDepth, -hRadius), hSize, vDepth, hSize));
        addBlockRange(TANK_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1, 1, 1));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, hRadius + 32, vDepth + 64);
        setMaximumVolumeAndDistanceOfRange(TANK_RANGE, 0, 5, 5);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ISoulNetwork network = AnimusRitualHelper.getOwnerNetwork(mrs);
        if (network == null) {
            return;
        }

        AreaDescriptor tankRange = getBlockRange(TANK_RANGE);
        BlockPos tankPos = tankRange.getContainedPositions(masterPos).iterator().next();
        BlockEntity tankEntity = level.getBlockEntity(tankPos);

        if (tankEntity == null) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        IFluidHandler fluidHandler;
        try {
            fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, tankPos, Direction.DOWN);
            if (fluidHandler == null) {
                emitSmokeParticles(serverLevel, masterPos);
                return;
            }
        } catch (Exception e) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        FluidStack extractedFluid;
        try {
            extractedFluid = fluidHandler.drain(BUCKET_AMOUNT, IFluidHandler.FluidAction.SIMULATE);
            if (extractedFluid.isEmpty() || extractedFluid.getAmount() < BUCKET_AMOUNT) {
                emitSmokeParticles(serverLevel, masterPos);
                return;
            }
        } catch (Exception e) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        net.minecraft.world.phys.AABB effectAABB = effectRange.getAABB(masterPos);
        int horizontalRadius = (int) Math.max(Math.abs(effectAABB.maxX - masterPos.getX()), Math.abs(effectAABB.maxZ - masterPos.getZ()));
        int verticalDepth = (int) Math.abs(effectAABB.minY - masterPos.getY());
        Fluid fluidToPlace = extractedFluid.getFluid();
        BlockPos placementPos = findValidPlacementPosition(serverLevel, masterPos, horizontalRadius, verticalDepth, fluidToPlace);

        if (placementPos == null) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        int lpCost = AnimusConfig.rituals.relentlessTidesLPPerPlacement.get();
        int currentEssence = network.getCurrentEssence();
        if (currentEssence < lpCost) {
            return;
        }

        FluidStack actualExtracted;
        try {
            actualExtracted = fluidHandler.drain(BUCKET_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
            if (actualExtracted.isEmpty() || actualExtracted.getAmount() < BUCKET_AMOUNT) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        Fluid fluid = actualExtracted.getFluid();
        Block fluidBlock = fluid.defaultFluidState().createLegacyBlock().getBlock();

        if (fluidBlock instanceof LiquidBlock) {
            BlockState fluidState = fluidBlock.defaultBlockState();
            level.setBlockAndUpdate(placementPos, fluidState);

            Set<BlockPos> filledPositions = filledPositionsCache.computeIfAbsent(
                masterPos.immutable(),
                k -> new java.util.HashSet<>()
            );
            filledPositions.add(placementPos.immutable());

            network.syphon(SoulTicket.create(lpCost));
        }
    }

    private BlockPos findValidPlacementPosition(ServerLevel level, BlockPos masterPos, int horizontalRadius, int verticalDepth, Fluid fluidToPlace) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        BlockPos startPos = masterPos.below();

        int maxChecksPerTick = 64;
        int checksThisTick = 0;

        for (int radius = state.currentRadius; radius <= horizontalRadius && checksThisTick < maxChecksPerTick; radius++) {
            for (int x = -radius; x <= radius && checksThisTick < maxChecksPerTick; x++) {
                if (radius == state.currentRadius && x < state.currentX) continue;

                for (int z = -radius; z <= radius && checksThisTick < maxChecksPerTick; z++) {
                    if (radius == state.currentRadius && x == state.currentX && z < state.currentZ) continue;

                    // Only check perimeter positions (skip interior of ring)
                    if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    int startY = (radius == state.currentRadius && x == state.currentX && z == state.currentZ) ? state.currentY : 0;
                    for (int y = startY; y < verticalDepth && checksThisTick < maxChecksPerTick; y++) {
                        BlockPos checkPos = startPos.offset(x, -y, z);
                        checksThisTick++;

                        state.currentRadius = radius;
                        state.currentX = x;
                        state.currentZ = z;
                        state.currentY = y;

                        if (isValidPlacementSpot(level, checkPos, fluidToPlace, masterPos)) {
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
                    state.currentY = 0;
                }
                state.currentZ = -radius;
            }
        }

        if (state.currentRadius > horizontalRadius) {
            resetSearchState(masterPos);
        }

        return null;
    }


    private boolean isValidPlacementSpot(ServerLevel level, BlockPos pos, Fluid fluidToPlace, BlockPos masterPos) {
        Set<BlockPos> filledPositions = filledPositionsCache.get(masterPos);
        if (filledPositions != null && filledPositions.contains(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();

        if (!fluidState.isEmpty() && fluidState.isSource() && fluidState.getType() == fluidToPlace) {
            return false;
        }

        return state.isAir() || state.canBeReplaced() || (!fluidState.isEmpty() && !fluidState.isSource());
    }

    private void emitSmokeParticles(ServerLevel level, BlockPos pos) {
        AnimusRitualHelper.emitSmokeParticles(level, pos);
    }

    private void resetSearchState(BlockPos pos) {
        searchStates.remove(pos);
    }

    public void onRitualStopped(Level level, BlockPos masterPos) {
        searchStates.remove(masterPos);
        filledPositionsCache.remove(masterPos);
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public int getRefreshTime() {
        return 10;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);

        addRune(components, -2, 0, -2, EnumRuneType.AIR);
        addRune(components, -2, 0, 2, EnumRuneType.AIR);
        addRune(components, 2, 0, -2, EnumRuneType.AIR);
        addRune(components, 2, 0, 2, EnumRuneType.AIR);

        addRune(components, 0, 0, -3, EnumRuneType.WATER);
        addRune(components, 0, 0, 3, EnumRuneType.WATER);
        addRune(components, -3, 0, 0, EnumRuneType.WATER);
        addRune(components, 3, 0, 0, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualRelentlessTides();
    }

    private static class SearchState {
        int currentRadius = 0;
        int currentX = 0;
        int currentZ = 0;
        int currentY = 0;
    }
}
