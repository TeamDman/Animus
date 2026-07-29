package com.breakinblocks.animusnv.blocks;

import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockBloodWoodStairs extends StairBlock {
    public BlockBloodWoodStairs(BlockBehaviour.Properties props) {
        super(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get().defaultBlockState(), props);
    }
}
