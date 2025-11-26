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
                .randomTicks()
        );
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Spread antimatter to adjacent blocks
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.relative(dir);
            BlockState offsetState = level.getBlockState(offsetPos);

            // Check if it's life essence from Blood Magic
            if (offsetState.getBlock().getDescriptionId().contains("life_essence")) {
                // Convert to antimatter fluid
                level.setBlock(offsetPos, this.defaultBlockState(), 3);
            } else if (!level.isEmptyBlock(offsetPos)
                && offsetState.getBlock() != AnimusBlocks.BLOCK_ANTIMATTER.get()
                && offsetState.getBlock() != AnimusBlocks.BLOCK_FLUID_ANTIMATTER.get()) {
                // Convert to antimatter block
                level.setBlock(offsetPos, AnimusBlocks.BLOCK_ANTIMATTER.get().defaultBlockState(), 3);
            }
        }

        super.tick(state, level, pos, random);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        tick(state, level, pos, random);
    }
}
