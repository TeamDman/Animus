package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Blood Wood Fence block
 */
public class BlockBloodWoodFence extends FenceBlock {
    public BlockBloodWoodFence() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0F, 3.0F)
            .sound(SoundType.WOOD)
        );
    }
}
