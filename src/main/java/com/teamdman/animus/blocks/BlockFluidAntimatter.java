package com.teamdman.animus.blocks;

import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Antimatter fluid block - spreads and converts blocks to antimatter
 * When it touches Blood Magic's life essence, it spreads as fluid
 * When it touches other blocks, it converts them to antimatter blocks
 */
public class BlockFluidAntimatter extends LiquidBlock {

    public BlockFluidAntimatter() {
        super(
            () -> (FlowingFluid) AnimusFluids.ANTIMATTER_SOURCE.get(),
            Properties.of()
                .noCollission()
                .strength(100.0F)
                .noLootTable()
        );
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // Schedule immediate tick when placed
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        // When a neighbor changes, schedule a tick to convert it
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        boolean converted = false;

        // Spread antimatter to adjacent blocks
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.relative(dir);
            BlockState offsetState = level.getBlockState(offsetPos);

            // Skip if empty/air
            if (level.isEmptyBlock(offsetPos)) {
                continue;
            }

            // Skip if already antimatter (block or fluid)
            if (offsetState.getBlock() == AnimusBlocks.BLOCK_ANTIMATTER.get()
                || offsetState.getBlock() == AnimusBlocks.BLOCK_FLUID_ANTIMATTER.get()) {
                continue;
            }

            // Check if it's life essence from Blood Magic
            if (offsetState.getBlock().getDescriptionId().contains("life_essence")) {
                // Convert to antimatter fluid
                level.setBlock(offsetPos, this.defaultBlockState(), 3);
                converted = true;
            } else {
                // Convert solid blocks to antimatter blocks
                // Skip blocks with tile entities or unbreakable blocks
                if (level.getBlockEntity(offsetPos) == null
                    && offsetState.getDestroySpeed(level, offsetPos) != -1.0F) {
                    level.setBlock(offsetPos, AnimusBlocks.BLOCK_ANTIMATTER.get().defaultBlockState(), 3);
                    converted = true;
                }
            }
        }

        // Schedule next tick if we converted something
        if (converted) {
            level.scheduleTick(pos, this, 5);
        }
    }
}
