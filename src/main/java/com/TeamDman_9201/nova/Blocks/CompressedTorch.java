package com.TeamDman_9201.nova.Blocks;

import java.util.ArrayList;

import net.minecraft.block.BlockTorch;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.TeamDman_9201.nova.NOVA;
import com.TeamDman_9201.nova.Tiles.TileEntityCompressedTorch;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CompressedTorch extends BlockTorch {
	public CompressedTorch() {
	}

	@SideOnly(Side.CLIENT)
	private IIcon	blockIcon;

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon("torch_on");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return this.blockIcon;
	}

	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z) {
		return Item.getItemFromBlock(NOVA.compressedTorch);
	}

	/**
	 * This returns a complete list of items dropped from this block.
	 * 
	 * @param world
	 *            The current world
	 * @param x
	 *            X Position
	 * @param y
	 *            Y Position
	 * @param z
	 *            Z Position
	 * @param metadata
	 *            Current metadata
	 * @param fortune
	 *            Breakers fortune level
	 * @return A ArrayList containing all items this block drops
	 */
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z,
			int metadata, int fortune) {
		ArrayList<ItemStack> rtn = new ArrayList<ItemStack>();
		ItemStack item = new ItemStack(NOVA.compressedTorch);
		TileEntityCompressedTorch tile = (TileEntityCompressedTorch) world
				.getTileEntity(x, y, z);
		if (tile != null) {
			System.out.println("GOT TILE");
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("Torches", tile.getTorches());
			item.setTagCompound(compound);
		}
		rtn.add(item);
		return rtn;
	}

	public boolean hasTileEntity(int meta) {
		return true;
	}

	public TileEntity createNewTileEntity(World world,int var2) {
		System.out.println("NEW TILE WORLD " + world.toString() + "INT " + var2);
		return new TileEntityCompressedTorch();
	}

	/**
	 * Called when the block is placed in the world.
	 */
	public void onBlockPlacedBy(World world, int xPos, int yPos, int zPos,
			EntityLivingBase entity, ItemStack stack) {
		NBTTagCompound tags = stack.getTagCompound();
		TileEntityCompressedTorch tile = (TileEntityCompressedTorch) createNewTileEntity(world,1);
		world.setTileEntity(xPos, yPos, zPos, tile);
		if (tags != null && tile != null) {
			tile.writeToNBT(tags);
		} else {
			System.out.println("Whut no tags?");
		}
	}

	// /**
	// * Called after a block is placed
	// */
	// public void onPostBlockPlaced(World world, int xPos, int yPos, int zPos,
	// int meta) {
	// TileEntityCompressedTorch tile = (TileEntityCompressedTorch) world
	// .getTileEntity(xPos, yPos, zPos);
	// NBTTagCompound compound = new NBTTagCompound();
	// tile.writeToNBT(compound);
	// }

}
