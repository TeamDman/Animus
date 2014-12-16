package com.TeamDman_9201.nova.Blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.TeamDman_9201.nova.NOVA;
import com.TeamDman_9201.nova.Tiles.TileLightManipulator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

//net.minecraft.tileentity.TileEntityFurnace;

public class BlockLightManipulator extends BlockContainer {

	private final Random random = new Random();
	private static boolean updateDebounce;
	private boolean isActive = false;

	public BlockLightManipulator(boolean active) {
		super(Material.glass);
		this.isActive = active;
	}

	@SideOnly(Side.CLIENT)
	private IIcon iconFront;
	@SideOnly(Side.CLIENT)
	private IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(NOVA.MODID + ":"
				+ "lightManipulatorSide");
		this.iconFront = iconRegister.registerIcon(NOVA.MODID + ":"
				+ "lightManipulatorFront");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return (side == metadata ? this.iconFront : this.blockIcon);
	}

	/**
	 * Gets an item for the block being called on. Args: world, x, y, z
	 */
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z) {
		return Item.getItemFromBlock(NOVA.lightManipulator);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileLightManipulator();
	}

	public Item getItemDropped(int p_149650_1_, Random p_149650_2_,
			int p_149650_3_) {
		return Item.getItemFromBlock(NOVA.lightManipulator);
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		// this.rotate(world, x, y, z);
		if (!world.isRemote) {
			Block block = world.getBlock(x, y, z - 1);
			Block block1 = world.getBlock(x, y, z + 1);
			Block block2 = world.getBlock(x - 1, y, z);
			Block block3 = world.getBlock(x + 1, y, z);
			byte b0 = 3;

			if (block.func_149730_j() && !block1.func_149730_j()) {
				b0 = 3;
			}

			if (block1.func_149730_j() && !block.func_149730_j()) {
				b0 = 2;
			}

			if (block2.func_149730_j() && !block3.func_149730_j()) {
				b0 = 5;
			}

			if (block3.func_149730_j() && !block2.func_149730_j()) {
				b0 = 4;
			}

			world.setBlockMetadataWithNotify(x, y, z, b0, 2);
		}
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int p_149727_6_, float p_149727_7_,
			float p_149727_8_, float p_149727_9_) {
		if (world.isRemote) {
			return true;
		} else {
			TileLightManipulator tileentityfurnace = (TileLightManipulator) world
					.getTileEntity(x, y, z);
			if (tileentityfurnace != null) {

				// player.func_146101_a(new
				// TileEntityFurnace());//tileentityfurnace);
				// player.noClip=true;
				// player.capabilities.allowFlying =
				// !player.capabilities.allowFlying;
				// player.capabilities.isFlying = !player.capabilities.isFlying;
				// player.openGui(mod, modGuiId, world, x, y, z);
				player.openGui(NOVA.instance, NOVA.guiLightManipulator, world,
						x, y, z);

			}

			return true;
		}
	}

	/**
	 * Called when the block is placed in the world.
	 */
	public void onBlockPlacedBy(World world, int x, int y, int z,
			EntityLivingBase entityLivingBase, ItemStack item) {
		int l = MathHelper
				.floor_double((double) (entityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

		if (l == 0) {
			world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		}

		if (l == 1) {
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
		}

		if (l == 2) {
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
		}

		if (l == 3) {
			world.setBlockMetadataWithNotify(x, y, z, 4, 2);
		}

		if (item.hasDisplayName()) {
			((TileLightManipulator) world.getTileEntity(x, y, z))
					.setName(item.getDisplayName());
		}
	}

	public void breakBlock(World world, int x, int y, int z, Block block,
			int p_149749_6_) {
		if (!updateDebounce) {
			TileLightManipulator tileentityfurnace = (TileLightManipulator) world
					.getTileEntity(x, y, z);

			if (tileentityfurnace != null) {
				for (int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
					ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

					if (itemstack != null) {
						float f = this.random.nextFloat() * 0.8F + 0.1F;
						float f1 = this.random.nextFloat() * 0.8F + 0.1F;
						float f2 = this.random.nextFloat() * 0.8F + 0.1F;

						while (itemstack.stackSize > 0) {
							int j1 = this.random.nextInt(21) + 10;

							if (j1 > itemstack.stackSize) {
								j1 = itemstack.stackSize;
							}

							itemstack.stackSize -= j1;
							EntityItem entityitem = new EntityItem(world,
									(double) ((float) x + f),
									(double) ((float) y + f1),
									(double) ((float) z + f2), new ItemStack(
											itemstack.getItem(), j1,
											itemstack.getItemDamage()));

							if (itemstack.hasTagCompound()) {
								entityitem.getEntityItem().setTagCompound(
										(NBTTagCompound) itemstack
												.getTagCompound().copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (double) ((float) this.random
									.nextGaussian() * f3);
							entityitem.motionY = (double) ((float) this.random
									.nextGaussian() * f3 + 0.2F);
							entityitem.motionZ = (double) ((float) this.random
									.nextGaussian() * f3);
							world.spawnEntityInWorld(entityitem);
						}
					}
				}

				world.func_147453_f(x, y, z, block);
			}
		}

		super.breakBlock(world, x, y, z, block, p_149749_6_);
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which
	 * neighbor changed (coordinates passed are their own) Args: x, y, z,
	 * neighbor Block
	 */
	public void onNeighborBlockChange(World world, int x, int y, int z,
			Block block) {
		if (world.isBlockIndirectlyGettingPowered(x, y, z)
				|| world.isBlockIndirectlyGettingPowered(x, y + 1, z)) {
			TileLightManipulator tile = (TileLightManipulator) world
					.getTileEntity(x, y, z);
			tile.commence();
		}

	}

	/**
	 * If this returns true, then comparators facing away from this block will
	 * use the value from getComparatorInputOverride instead of the actual
	 * redstone signal strength.
	 */
	public boolean hasComparatorInputOverride() {
		return true;
	}

	/**
	 * If hasComparatorInputOverride returns true, the return value from this is
	 * used instead of the redstone signal strength when this block inputs to a
	 * comparator.
	 */
	public int getComparatorInputOverride(World world, int x, int y, int z,
			int p_149736_5_) {
		return Container.calcRedstoneFromInventory((IInventory) world
				.getTileEntity(x, y, z));
	}

}
