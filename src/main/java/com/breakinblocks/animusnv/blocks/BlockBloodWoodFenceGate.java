package com.breakinblocks.animusnv.blocks;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

public class BlockBloodWoodFenceGate extends FenceGateBlock {
    public BlockBloodWoodFenceGate() {
        super(
            WoodType.OAK,
            AnimusBlockProperties.bloodWoodPlanks()
        );
    }
}
