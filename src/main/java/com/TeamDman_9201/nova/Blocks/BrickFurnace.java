package com.TeamDman_9201.nova.Blocks;

import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;

import com.TeamDman_9201.nova.*;
import com.TeamDman_9201.nova.Tiles.TileEntityBrickFurnace;

import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BrickFurnace extends BlockContainer {

	private final Random	random	= new Random();
	private static boolean	breakDebounce;

	public BrickFurnace() {
		super(Material.rock);
	}

	@SideOnly(Side.CLIENT)
	private IIcon	iconFrontActive;
	@SideOnly(Side.CLIENT)
	private IIcon	iconFrontIdle;
	@SideOnly(Side.CLIENT)
	private IIcon	blockIcon;

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(NOVA.MODID + ":" + "brickFurnaceSide");
		this.iconFrontActive = iconRegister.registerIcon(NOVA.MODID + ":" + "brickFurnaceFrontActive");
		this.iconFrontIdle = iconRegister.registerIcon(NOVA.MODID + ":" + "brickFurnaceFrontIdle");
	}

	// @SideOnly(Side.CLIENT)
	// public void registerBlockIcons(IIconRegister p_149651_1_)
	// {
	// this.blockIcon = p_149651_1_.registerIcon("furnace_side");
	// this.field_149936_O = p_149651_1_.registerIcon(this.field_149932_b ?
	// "furnace_front_on" : "furnace_front_off");
	// this.field_149935_N = p_149651_1_.registerIcon("furnace_top");
	// }

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		int facing = facingFromMeta(metadata);
		boolean active = activeFromMeta(metadata);
		return (side == facing ? (active ? iconFrontActive : iconFrontIdle) : blockIcon);
	}

	/**
	 * Gets an item for the block being called on. Args: world, x, y, z
	 */
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z) {
		return Item.getItemFromBlock(NOVA.brickFurnace);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityBrickFurnace();
	}

	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(NOVA.brickFurnace);
	}

	int toMeta(int facing, boolean active) {
		int mask = 0;

		mask = facing << 1;
		mask |= active ? 1 : 0;

		return mask;
	}

	int facingFromMeta(int meta) {
		return (meta & 15) >> 1;
	}

	boolean activeFromMeta(int meta) {
		return (meta & 1) == 1;
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

			world.setBlockMetadataWithNotify(x, y, z, toMeta(b0, false), 2);
		}
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_,
			float p_149727_9_) {
		if (world.isRemote) {
			return true;
		} else {
			TileEntityBrickFurnace tileentityfurnace = (TileEntityBrickFurnace) world.getTileEntity(x, y, z);
			System.out.println("Opening Furnace " + random.nextInt());
			if (tileentityfurnace != null) {

				// player.func_146101_a(new
				// TileEntityFurnace());//tileentityfurnace);
				// player.noClip=true;
				// player.capabilities.allowFlying =
				// !player.capabilities.allowFlying;
				// player.capabilities.isFlying = !player.capabilities.isFlying;
				// player.openGui(mod, modGuiId, world, x, y, z);
				player.openGui(NOVA.instance, NOVA.guiBrickFurnace, world, x, y, z);

				System.out.println("End Open " + random.nextInt());
			}

			return true;
		}
	}

	/**
	 * Called when the block is placed in the world.
	 */
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack item) {
		int l = MathHelper.floor_double((double) (entityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int face = 2;
		if (l == 0) {
			face = 2;
		}

		if (l == 1) {
			face = 5;
		}

		if (l == 2) {
			face = 3;
		}

		if (l == 3) {
			face = 4;
		}
		world.setBlockMetadataWithNotify(x, y, z, toMeta(face, false), 2);
		if (item.hasDisplayName()) {
			((TileEntityBrickFurnace) world.getTileEntity(x, y, z)).setName(item.getDisplayName());
		}
	}

	/**
	 * Update which block the furnace is using depending on whether or not it is
	 * burning
	 */
	public static void updateFurnaceBlockState(boolean active, World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity tileentity = world.getTileEntity(x, y, z);
		breakDebounce = true;
		world.setBlock(x, y, z, NOVA.brickFurnace);
		breakDebounce = false;

		int mask = 0;
		mask = ((meta & 15) >> 1) << 1;
		mask |= active ? 1 : 0;

		int newMeta = mask;

		// tometa9meta0

		world.setBlockMetadataWithNotify(x, y, z, newMeta, 2);
		if (tileentity != null) {
			tileentity.validate();
			world.setTileEntity(x, y, z, tileentity);
		}
	}

	public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
		if (!breakDebounce) {
			TileEntityBrickFurnace tileentityfurnace = (TileEntityBrickFurnace) world.getTileEntity(x, y, z);

			if (tileentityfurnace != null) {
				for (int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
					ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

					if (itemstack != null) {
						float f = random.nextFloat() * 0.8F + 0.1F;
						float f1 = random.nextFloat() * 0.8F + 0.1F;
						float f2 = random.nextFloat() * 0.8F + 0.1F;

						while (itemstack.stackSize > 0) {
							int j1 = random.nextInt(21) + 10;

							if (j1 > itemstack.stackSize) {
								j1 = itemstack.stackSize;
							}

							itemstack.stackSize -= j1;
							EntityItem entityitem = new EntityItem(world, (double) ((float) x + f), (double) ((float) y + f1),
									(double) ((float) z + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));

							if (itemstack.hasTagCompound()) {
								entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (double) ((float) random.nextGaussian() * f3);
							entityitem.motionY = (double) ((float) random.nextGaussian() * f3 + 0.2F);
							entityitem.motionZ = (double) ((float) random.nextGaussian() * f3);
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
	 * A randomly called display update to be able to add particles or other
	 * items for display
	 */
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if (activeFromMeta(world.getBlockMetadata(x, y, z))) {
			int l = facingFromMeta(world.getBlockMetadata(x, y, z));
			float f = (float) x + 0.5F;
			float f1 = (float) y + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
			float f2 = (float) z + 0.5F;
			float f3 = 0.52F;
			float f4 = rand.nextFloat() * 0.6F - 0.3F;

			if (l == 4) {
				world.spawnParticle("smoke", (double) (f - f3), (double) f1, (double) (f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double) (f - f3), (double) f1, (double) (f2 + f4), 0.0D, 0.0D, 0.0D);
			} else if (l == 5) {
				world.spawnParticle("smoke", (double) (f + f3), (double) f1, (double) (f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double) (f + f3), (double) f1, (double) (f2 + f4), 0.0D, 0.0D, 0.0D);
			} else if (l == 2) {
				world.spawnParticle("smoke", (double) (f + f4), (double) f1, (double) (f2 - f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double) (f + f4), (double) f1, (double) (f2 - f3), 0.0D, 0.0D, 0.0D);
			} else if (l == 3) {
				world.spawnParticle("smoke", (double) (f + f4), (double) f1, (double) (f2 + f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double) (f + f4), (double) f1, (double) (f2 + f3), 0.0D, 0.0D, 0.0D);
			}
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
	public int getComparatorInputOverride(World world, int x, int y, int z, int p_149736_5_) {
		return Container.calcRedstoneFromInventory((IInventory) world.getTileEntity(x, y, z));
	}
}

/*
 * public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int
 * p_149650_3_){ return Item.getItemFromBlock(First.brickFurnaceIdleIdle); }
 * 
 * public void onBlockAdded(World world, int x, int y, int z) {
 * super.onBlockAdded(world, x, y, z); this.setDefaultDirection(world, x, y, z);
 * }
 * 
 * private void setDefaultDirection(World world, int x, int y, int z) { if
 * (!world.isRemote) { Block block1 = world.getBlock(x, y, z-1); Block block2 =
 * world.getBlock(x, y, z+1); Block block3 = world.getBlock(x-1, y, z); Block
 * block4 = world.getBlock(x+1, y, z);
 * 
 * byte b0 = 3; if (block1.func_149730_j() && ~ block2.func_149730_j()) { b0 =
 * 3; } if (block2.func_149730_j() && !block1..func_149730_j()) { b0 = 2; } if
 * (block3.func_149730_j() && Block4.func_149730_j()) { b0 = 5 } if
 * (block4.func_149730_j() && Block3.func_149730_j()) { b0 = 4 }
 * world.setBlockMetadataWithNotify(x, y, z, b0, 2); } }
 */

