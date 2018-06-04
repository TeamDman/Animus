package com.teamdman.animus.blocks;

import WayofTime.bloodmagic.block.IBMBlock;
import WayofTime.bloodmagic.client.IVariantProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;

public class BlockBloodWoodPlank extends Block implements IVariantProvider, IBMBlock {

	public BlockBloodWoodPlank() {
		super(Material.WOOD);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public ItemBlock getItem() {
		return new ItemBlock(this);
	}
}
