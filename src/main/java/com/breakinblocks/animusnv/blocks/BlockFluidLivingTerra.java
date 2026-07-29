package com.breakinblocks.animusnv.blocks;

import com.breakinblocks.animusnv.registry.AnimusFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Living Terra fluid block - slowly solidifies into dirt
 * When the fluid level is high enough (> 6), it solidifies
 * When it touches dirt blocks, it also solidifies
 */
public class BlockFluidLivingTerra extends LiquidBlock {

    public BlockFluidLivingTerra(BlockBehaviour.Properties props) {
        super((FlowingFluid) AnimusFluids.LIVING_TERRA_SOURCE.get(), props);
    }

    public static BlockBehaviour.Properties defaultProperties() {
        return BlockBehaviour.Properties.of()
            .noCollision()
            .strength(100.0F)
            .noLootTable();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        FluidState fluidState = state.getFluidState();

        // Flowing fluid (non-source) solidifies to dirt
        if (fluidState.getAmount() < 8) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            return;
        }

        // Source blocks solidify when adjacent to dirt
        for (Direction face : Direction.values()) {
            BlockPos offsetPos = pos.relative(face);
            BlockState offsetState = level.getBlockState(offsetPos);

            if (offsetState.is(Blocks.DIRT)) {
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                return;
            }
        }

        level.scheduleTick(pos, this, 20);
    }
}
