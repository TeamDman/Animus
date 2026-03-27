package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.AnimusStartupConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.animusnv.util.ChebyshevSearcher;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Siphon (Fluid Pump)
 * Extracts fluids from the world below the ritual stone and places them into a tank above
 * Uses a center-outward search algorithm, starting directly below the ritual stone
 * Replaces extracted fluids with configurable replacement block (default: antilife)
 * Activation Cost: 5000 EV
 * Refresh Cost: Configurable (default: 50 EV per extraction)
 * Refresh Time: 10 ticks (0.5 seconds)
 * Horizontal Radius: Configurable (default: 32 blocks)
 * Vertical Depth: Configurable (default: 128 blocks)
 * Replacement Block: Configurable (default: animus:block_antilife)
 */
public class RitualSiphon extends Ritual {
    private static final Logger LOGGER = LoggerFactory.getLogger(RitualSiphon.class);
    public static final String EFFECT_RANGE = "effect";
    public static final String TANK_RANGE = "tank";

    private static final ChebyshevSearcher SEARCHER = new ChebyshevSearcher();
    private static final Map<BlockPos, Set<BlockPos>> extractedPositionsCache = new HashMap<>();
    private static final int BUCKET_AMOUNT = 1000;

    public RitualSiphon() {
        super(
            Constants.Rituals.SIPHON,
            0,
            5000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SIPHON
        );

        int hRadius = AnimusStartupConfig.ritualRanges.siphonRange.get();
        int vDepth = AnimusStartupConfig.ritualRanges.siphonDepth.get();
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

        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
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

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        net.minecraft.world.phys.AABB effectAABB = effectRange.getAABB(masterPos);
        int horizontalRadius = (int) Math.max(Math.abs(effectAABB.maxX - masterPos.getX()), Math.abs(effectAABB.maxZ - masterPos.getZ()));
        int verticalDepth = (int) Math.abs(effectAABB.minY - masterPos.getY());
        BlockPos fluidPos = findFluidSource(serverLevel, masterPos, horizontalRadius, verticalDepth);

        if (fluidPos == null) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        BlockState fluidState = level.getBlockState(fluidPos);
        FluidState fluidStateData = fluidState.getFluidState();

        if (fluidStateData.isEmpty() || !fluidStateData.isSource()) {
            return;
        }

        FluidStack fluidStack = new FluidStack(fluidStateData.getType(), BUCKET_AMOUNT);

        int filled;
        try {
            filled = fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
            if (filled < BUCKET_AMOUNT) {
                emitSmokeParticles(serverLevel, masterPos);
                return;
            }
        } catch (Exception e) {
            emitSmokeParticles(serverLevel, masterPos);
            return;
        }

        int evCost = AnimusConfig.rituals.siphonEVPerExtraction.get();
        int currentEV = network.getCurrentEV();
        if (currentEV < evCost) {
            return;
        }

        int actualFilled;
        try {
            actualFilled = fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            if (actualFilled < BUCKET_AMOUNT) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        level.setBlockAndUpdate(fluidPos, getReplacementBlock());

        Set<BlockPos> extractedPositions = extractedPositionsCache.computeIfAbsent(
            masterPos.immutable(),
            k -> new java.util.HashSet<>()
        );
        extractedPositions.add(fluidPos.immutable());

        network.syphon(AnimaTicket.create(evCost));
    }

    private BlockPos findFluidSource(ServerLevel level, BlockPos masterPos, int horizontalRadius, int verticalDepth) {
        return SEARCHER.search(
            masterPos,
            masterPos.below(),
            horizontalRadius,
            verticalDepth,
            true,
            64,
            checkPos -> isFluidSource(level, checkPos, masterPos)
        );
    }

    private boolean isFluidSource(ServerLevel level, BlockPos pos, BlockPos masterPos) {
        Set<BlockPos> extractedPositions = extractedPositionsCache.get(masterPos);
        if (extractedPositions != null && extractedPositions.contains(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof LiquidBlock)) {
            return false;
        }

        FluidState fluidState = state.getFluidState();
        return fluidState.isSource();
    }

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

            LOGGER.warn("[Siphon] Configured replacement block '{}' not found, using antilife block", blockId);
        } catch (Exception e) {
            LOGGER.warn("[Siphon] Invalid replacement block ID '{}', using antilife block: {}",
                blockId, e.getMessage());
        }

        return AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState();
    }

    private void emitSmokeParticles(ServerLevel level, BlockPos pos) {
        AnimusRitualHelper.emitSmokeParticles(level, pos);
    }

    private void resetSearchState(BlockPos pos) {
        SEARCHER.reset(pos);
    }

    public void onRitualStopped(Level level, BlockPos masterPos) {
        SEARCHER.reset(masterPos);
        extractedPositionsCache.remove(masterPos);
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

        addRune(components, -2, 0, -2, EnumRuneType.EARTH);
        addRune(components, -2, 0, 2, EnumRuneType.EARTH);
        addRune(components, 2, 0, -2, EnumRuneType.EARTH);
        addRune(components, 2, 0, 2, EnumRuneType.EARTH);

        addRune(components, 0, 0, -3, EnumRuneType.WATER);
        addRune(components, 0, 0, 3, EnumRuneType.WATER);
        addRune(components, -3, 0, 0, EnumRuneType.WATER);
        addRune(components, 3, 0, 0, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSiphon();
    }

}
