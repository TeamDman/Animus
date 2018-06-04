package com.teamdman.animus.blocks;

import com.teamdman.animus.tiles.TileBloodCore;

import WayofTime.bloodmagic.block.IBMBlock;
import WayofTime.bloodmagic.client.IVariantProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBloodCore extends Block implements IVariantProvider, IBMBlock {


	public BlockBloodCore() {
		super(Material.WOOD);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileBloodCore();
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
