package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Stripped Blood Wood Log block
 * Behaves like stripped oak log - rotatable pillar block
 */
public class BlockBloodWoodStripped extends RotatedPillarBlock {
    public BlockBloodWoodStripped() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0F)
            .sound(SoundType.WOOD)
        );
    }
}
