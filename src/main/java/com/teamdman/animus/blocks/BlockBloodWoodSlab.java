package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Blood Wood Slab block
 */
public class BlockBloodWoodSlab extends SlabBlock {
    public BlockBloodWoodSlab() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0F, 3.0F)
            .sound(SoundType.WOOD)
        );
    }
}
