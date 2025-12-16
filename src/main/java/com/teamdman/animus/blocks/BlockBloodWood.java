package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.RotatedPillarBlock;

/**
 * Blood Wood Log block
 * Original: 1.12.2 Material.WOOD based block
 * Ported: 1.20.1 RotatedPillarBlock for proper log behavior (can be rotated and stripped)
 */
public class BlockBloodWood extends RotatedPillarBlock {
    public BlockBloodWood() {
        super(AnimusBlockProperties.bloodWoodLog());
    }
}
