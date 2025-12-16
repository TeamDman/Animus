package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.RotatedPillarBlock;

/**
 * Stripped Blood Wood Log block
 * Behaves like stripped oak log - rotatable pillar block
 */
public class BlockBloodWoodStripped extends RotatedPillarBlock {
    public BlockBloodWoodStripped() {
        super(AnimusBlockProperties.bloodWoodLog());
    }
}
