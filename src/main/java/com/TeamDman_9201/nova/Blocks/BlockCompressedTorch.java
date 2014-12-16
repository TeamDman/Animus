package com.TeamDman_9201.nova.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.TeamDman_9201.nova.NOVA;
import com.TeamDman_9201.nova.Tiles.TileCompressedTorch;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCompressedTorch extends BlockTorch implements ITileEntityProvider {
	// Cazzar> Teamy, create another class, which extends that, and register it
	// with the block.
	private final Random	random	= new Random();

	public BlockCompressedTorch() {
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
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> rtn = new ArrayList<ItemStack>();
		return rtn;
	}

	public boolean hasTileEntity(int meta) {
		return true;
	}

	public TileEntity createNewTileEntity(World world, int var2) {
		return new TileCompressedTorch();
	}

	/**
	 * Called when the block is placed in the world.
	 */
	public void onBlockPlacedBy(World world, int xPos, int yPos, int zPos, EntityLivingBase entity, ItemStack stack) {
		NBTTagCompound tags = stack.getTagCompound();
		TileCompressedTorch tile = (TileCompressedTorch) world.getTileEntity(xPos, yPos, zPos);
		if (tags != null && tile != null) {
			tile.setTorches(tags.getLong("Torches"));
		}
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_,
			float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		if (world.isRemote) {
			return true;
		} else {
			TileCompressedTorch tile = (TileCompressedTorch) world.getTileEntity(x, y, z);
			if (tile != null) {
				System.out.println("Torches:" + tile.getTorches());
			}

			return true;
		}
	}

	public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {

		TileCompressedTorch tile = (TileCompressedTorch) world.getTileEntity(x, y, z);

		if (tile != null) {
			ItemStack stack = new ItemStack(NOVA.compressedTorch);
			NBTTagCompound tags = new NBTTagCompound();
			tags.setLong("Torches", tile.getTorches());
			stack.setTagCompound(tags);

			float f = random.nextFloat() * 0.8F + 0.1F;
			float f1 = random.nextFloat() * 0.8F + 0.1F;
			float f2 = random.nextFloat() * 0.8F + 0.1F;

			while (stack.stackSize > 0) {
				int j1 = random.nextInt(21) + 10;

				if (j1 > stack.stackSize) {
					j1 = stack.stackSize;
				}

				stack.stackSize -= j1;
				EntityItem entityitem = new EntityItem(world, (double) ((float) x + f), (double) ((float) y + f1),
						(double) ((float) z + f2), new ItemStack(stack.getItem(), j1, stack.getItemDamage()));

				if (stack.hasTagCompound()) {
					entityitem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
				}

				float f3 = 0.05F;
				entityitem.motionX = (double) ((float) random.nextGaussian() * f3);
				entityitem.motionY = (double) ((float) random.nextGaussian() * f3 + 0.2F);
				entityitem.motionZ = (double) ((float) random.nextGaussian() * f3);
				world.spawnEntityInWorld(entityitem);
			}

			world.func_147453_f(x, y, z, block); // triggers block updates,
													// should prob keep in
		}

		super.breakBlock(world, x, y, z, block, p_149749_6_); // removes my tile
																// entity if
																// isinstanceof
																// blockContainer
	}
}
