package com.teamdman.animus.blocks;

import com.teamdman.animus.registry.AnimusFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Dirt fluid block - slowly solidifies into dirt
 * When the fluid level is high enough (> 6), it solidifies
 * When it touches dirt blocks, it also solidifies
 */
public class BlockFluidDirt extends LiquidBlock {

    public BlockFluidDirt() {
        super(
            () -> (FlowingFluid) AnimusFluids.DIRT_SOURCE.get(),
            Properties.of()
                .noCollission()
                .strength(100.0F)
                .noLootTable()
        );
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // Schedule delayed tick to allow flowing
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 20);  // 1 second delay
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        FluidState fluidState = state.getFluidState();

        // If this is a flowing block (not source), solidify to dirt after delay
        // Amount < 8 means it's flowing fluid, not a source block
        if (fluidState.getAmount() < 8) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            return;
        }

        // If the fluid level is high enough (source block with amount == 8), check for adjacent dirt
        for (Direction face : Direction.values()) {
            BlockPos offsetPos = pos.relative(face);
            BlockState offsetState = level.getBlockState(offsetPos);

            if (offsetState.is(Blocks.DIRT)) {
                // Solidify this fluid block into dirt
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                return;
            }
        }

        // Schedule next tick to keep checking
        level.scheduleTick(pos, this, 20);
    }
}
