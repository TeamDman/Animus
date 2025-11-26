package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Blood Wood Planks block
 * Behaves like oak planks - regular cube block
 */
public class BlockBloodWoodPlanks extends Block {
    public BlockBloodWoodPlanks() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0F, 3.0F)
            .sound(SoundType.WOOD)
        );
    }
}
