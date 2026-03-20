package com.teamdman.animus.blocks;

import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.world.level.block.StairBlock;

public class BlockBloodWoodStairs extends StairBlock {
    public BlockBloodWoodStairs() {
        super(
            AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get().defaultBlockState(),
            AnimusBlockProperties.bloodWoodPlanks()
        );
    }
}
