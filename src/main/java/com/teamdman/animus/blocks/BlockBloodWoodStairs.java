package com.teamdman.animus.blocks;

import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Blood Wood Stairs block
 */
public class BlockBloodWoodStairs extends StairBlock {
    public BlockBloodWoodStairs() {
        super(
            AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get().defaultBlockState(),
            BlockBehaviour.Properties.of()
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
        );
    }
}
