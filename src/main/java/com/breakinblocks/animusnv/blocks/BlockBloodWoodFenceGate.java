package com.breakinblocks.animusnv.blocks;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

public class BlockBloodWoodFenceGate extends FenceGateBlock {
    public BlockBloodWoodFenceGate(BlockBehaviour.Properties props) {
        super(WoodType.OAK, props);
    }
}
