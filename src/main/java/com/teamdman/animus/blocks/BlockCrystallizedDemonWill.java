package com.teamdman.animus.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Crystallized Demon Will Block for tier 6 Blood Altars
 * Acts as a valid CRYSTAL component for altar structures
 */
public class BlockCrystallizedDemonWill extends Block {
    // Slightly smaller than a full block for visual appeal
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(2, 0, 2, 14, 12, 14),  // Main pillar body
        Block.box(0, 12, 0, 16, 16, 16)   // Top cap
    );

    public BlockCrystallizedDemonWill() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_RED)
            .strength(2.0F, 5.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
