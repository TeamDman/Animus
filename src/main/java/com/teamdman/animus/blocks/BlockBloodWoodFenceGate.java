package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * Blood Wood Fence Gate block
 */
public class BlockBloodWoodFenceGate extends FenceGateBlock {
    public BlockBloodWoodFenceGate() {
        super(
            WoodType.OAK, // Using OAK wood type for sound/behavior
            AnimusBlockProperties.bloodWoodPlanks()
        );
    }
}
