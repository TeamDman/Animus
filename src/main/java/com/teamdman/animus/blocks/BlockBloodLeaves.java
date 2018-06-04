package com.teamdman.animus.blocks;

import java.util.List;

import WayofTime.bloodmagic.block.IBMBlock;
import WayofTime.bloodmagic.client.IVariantProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockBloodLeaves extends BlockLeaves implements IVariantProvider, IBMBlock {

	public BlockBloodLeaves() {
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public ItemBlock getItem() {
		return new ItemBlock(this);
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumType getWoodType(int meta) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
