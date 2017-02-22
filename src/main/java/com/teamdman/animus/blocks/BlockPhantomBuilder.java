package com.teamdman.animus.blocks;

import WayofTime.bloodmagic.block.BlockPhantom;
import WayofTime.bloodmagic.tile.TilePhantomBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by TeamDman on 9/25/2016.
 */
//
public class BlockPhantomBuilder extends BlockPhantom {
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldStack = playerIn.getHeldItem(hand);
		Item heldItem  = heldStack.getItem();

		if (heldStack.isEmpty())
			return false;
		
		if (heldItem instanceof ItemBlock) {
			Item _item = heldItem;
			@SuppressWarnings("deprecation")
			IBlockState _state = Block.getBlockFromItem(_item).getStateFromMeta(_item.getDamage(playerIn.getHeldItem(hand)));
			worldIn.setBlockState(pos, _state);
			
			if (playerIn.capabilities.isCreativeMode)
			heldStack.setCount(heldStack.getCount()-1);
		}
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TilePhantomBlock(600);
	}

}
