package com.breakinblocks.animusnv.blocks;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * AntiLife fluid block - spreads and converts blocks to antilife
 * When it touches NeoVitae's Essentia Vitae, it spreads as fluid
 * When it touches other blocks, it converts them to antilife blocks
 * Will convert ANY block unless it's in the animus:disallow_antilife tag
 */
public class BlockFluidAntiLife extends LiquidBlock {

    public BlockFluidAntiLife() {
        super(
            (FlowingFluid) AnimusFluids.ANTILIFE_SOURCE.get(),
            Properties.of()
                .noCollission()
                .strength(100.0F)
                .noLootTable()
                .replaceable()
        );
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        boolean converted = false;

        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.relative(dir);
            BlockState offsetState = level.getBlockState(offsetPos);

            if (level.isEmptyBlock(offsetPos)) {
                continue;
            }

            if (offsetState.getBlock() == AnimusBlocks.BLOCK_ANTILIFE.get()
                || offsetState.getBlock() == AnimusBlocks.BLOCK_FLUID_ANTILIFE.get()) {
                continue;
            }

            if (offsetState.getBlock().getDescriptionId().contains("life_essence")) {
                level.setBlock(offsetPos, this.defaultBlockState(), 3);
                converted = true;
            } else {
                if (!offsetState.is(Constants.Tags.DISALLOW_ANTILIFE)) {
                    level.setBlock(offsetPos, AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState(), 3);
                    converted = true;
                }
            }
        }

        if (converted) {
            level.scheduleTick(pos, this, 5);
        }
    }
}
