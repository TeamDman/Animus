package com.teamdman.animus.blocks;

import com.teamdman.animus.Constants;
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
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * AntiLife fluid block - spreads and converts blocks to antilife
 * When it touches Blood Magic's life essence, it spreads as fluid
 * When it touches other blocks, it converts them to antilife blocks
 * Will convert ANY block unless it's in the animus:disallow_antilife tag
 */
public class BlockFluidAntiLife extends LiquidBlock {

    public BlockFluidAntiLife() {
        super(
            () -> (FlowingFluid) AnimusFluids.ANTILIFE_SOURCE.get(),
            Properties.of()
                .noCollission()
                .strength(100.0F)
                .noLootTable()
                .replaceable()  // Allow blocks to be placed in the fluid
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

        // Spread antilife to adjacent blocks
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.relative(dir);
            BlockState offsetState = level.getBlockState(offsetPos);

            // Skip if empty/air
            if (level.isEmptyBlock(offsetPos)) {
                continue;
            }

            // Skip if already antilife (block or fluid)
            if (offsetState.getBlock() == AnimusBlocks.BLOCK_ANTILIFE.get()
                || offsetState.getBlock() == AnimusBlocks.BLOCK_FLUID_ANTILIFE.get()) {
                continue;
            }

            // Check if it's life essence from Blood Magic
            if (offsetState.getBlock().getDescriptionId().contains("life_essence")) {
                // Convert to antilife fluid
                // Note: Natural fluid spreading doesn't have player context, so no protection check
                level.setBlock(offsetPos, this.defaultBlockState(), 3);
                converted = true;
            } else {
                // Convert ANY solid block to antilife blocks unless blacklisted
                // Check if block is in the disallow_antilife tag
                if (!offsetState.is(Constants.Tags.DISALLOW_ANTILIFE)) {
                    // Note: Natural fluid spreading doesn't have player context, so no protection check
                    level.setBlock(offsetPos, AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState(), 3);
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
