package com.teamdman_9201.nova.blocks;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-04-14.
 */

public class BlockLeavesBlood extends BlockLeaves {

	int[] decayCheck;
	int[] leafNodes;
	@SideOnly(Side.CLIENT)
    private IIcon opaqueIcon;
    @SideOnly(Side.CLIENT)
    private IIcon transparentIcon;

	public BlockLeavesBlood() {
		super();

		setCreativeTab(CreativeTabs.tabDecorations);
		setHardness(0.2F);
		setLightOpacity(0);
	}

	@Override
    public IIcon getIcon(int side, int meta)
    {
        return !Blocks.leaves.isOpaqueCube() ? transparentIcon : opaqueIcon;
    }

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
        opaqueIcon = register.registerIcon(NOVA.MODID + ":blockLeavesFast");
        transparentIcon = register.registerIcon(NOVA.MODID + ":blockLeaves");
	}

	@Override
	public Item getItemDropped(int i, Random random, int j) {

		return Item.getItemFromBlock(NOVA.blockSapling);
	}


	/**
	 * Ticks the block if it's been scheduled
	 */

	@Override
	public void updateTick(World world, int posX, int posY, int posZ, Random rnd) {
		if (!world.isRemote) {

			int l = world.getBlockMetadata(posX, posY, posZ);

			if ((l & 8) != 0 && (l & 4) == 0) {
				byte b0 = 4;
				int i1 = b0 + 1;
				byte b1 = 32;
				int j1 = b1 * b1;
				int k1 = b1 / 2;

				if (this.decayCheck == null) {
					this.decayCheck = new int[b1 * b1 * b1];
				}

				int l1;

				if (world.checkChunksExist(posX - i1, posY - i1, posZ - i1,
						posX + i1, posY + i1, posZ + i1)) {
					// System.out.println("leaveS: if chunk exists, do stuff");
					int i2;
					int j2;

					for (l1 = -b0; l1 <= b0; ++l1) {
						for (i2 = -b0; i2 <= b0; ++i2) {
							for (j2 = -b0; j2 <= b0; ++j2) {
								Block block = world.getBlock(posX + l1, posY
										+ i2, posZ + j2);

								if (block != Blocks.log && block != Blocks.log2) {
									if (block.isLeaves(world, posX + l1, posY
											+ i2, posZ + j2)) {
										this.decayCheck[(l1 + k1) * j1
										                + (i2 + k1) * b1 + j2 + k1] = -2;
									} else {
										this.decayCheck[(l1 + k1) * j1
										                + (i2 + k1) * b1 + j2 + k1] = -1;
									}
								} else {
									this.decayCheck[(l1 + k1) * j1 + (i2 + k1)
									                * b1 + j2 + k1] = 0;
								}
							}
						}
					}

					for (l1 = 1; l1 <= 4; ++l1) {
						for (i2 = -b0; i2 <= b0; ++i2) {
							for (j2 = -b0; j2 <= b0; ++j2) {
								for (int k2 = -b0; k2 <= b0; ++k2) {
									if (this.decayCheck[(i2 + k1) * j1
									                    + (j2 + k1) * b1 + k2 + k1] == l1 - 1) {
										if (this.decayCheck[(i2 + k1 - 1) * j1
										                    + (j2 + k1) * b1 + k2 + k1] == -2) {
											this.decayCheck[(i2 + k1 - 1) * j1
											                + (j2 + k1) * b1 + k2 + k1] = l1;
										}

										if (this.decayCheck[(i2 + k1 + 1) * j1
										                    + (j2 + k1) * b1 + k2 + k1] == -2) {
											this.decayCheck[(i2 + k1 + 1) * j1
											                + (j2 + k1) * b1 + k2 + k1] = l1;
										}

										if (this.decayCheck[(i2 + k1) * j1
										                    + (j2 + k1 - 1) * b1 + k2 + k1] == -2) {
											this.decayCheck[(i2 + k1) * j1
											                + (j2 + k1 - 1) * b1 + k2
											                + k1] = l1;
										}

										if (this.decayCheck[(i2 + k1) * j1
										                    + (j2 + k1 + 1) * b1 + k2 + k1] == -2) {
											this.decayCheck[(i2 + k1) * j1
											                + (j2 + k1 + 1) * b1 + k2
											                + k1] = l1;
										}

										if (this.decayCheck[(i2 + k1) * j1
										                    + (j2 + k1) * b1
										                    + (k2 + k1 - 1)] == -2) {
											this.decayCheck[(i2 + k1) * j1
											                + (j2 + k1) * b1
											                + (k2 + k1 - 1)] = l1;
										}

										if (this.decayCheck[(i2 + k1) * j1
										                    + (j2 + k1) * b1 + k2 + k1 + 1] == -2) {
											this.decayCheck[(i2 + k1) * j1
											                + (j2 + k1) * b1 + k2 + k1
											                + 1] = l1;
										}
									}
								}
							}
						}
					}
				}

				l1 = this.decayCheck[k1 * j1 + k1 * b1 + k1];

				if (l1 >= 0) {
					world.setBlockMetadataWithNotify(posX, posY, posZ, l & -9,
							4);
				} else {

					this.removeLeaves(world, posX, posY, posZ);
				}
			}
		}
	}

	private void removeLeaves(World world, int posX, int posY, int posZ) {

		if (world.rand.nextInt(20) == 0) {
			EntityItem drop = new EntityItem(world, posX, posY, posZ);
			drop.setEntityItemStack(new ItemStack(NOVA.blockSapling));
			world.spawnEntityInWorld(drop);
		}

		if (world.rand.nextInt(8) == 0) {
			EntityItem drop = new EntityItem(world, posX, posY, posZ);
			drop.setEntityItemStack(new ItemStack(NOVA.itemBloodApple));
			world.spawnEntityInWorld(drop);
		}
		world.setBlockToAir(posX, posY, posZ);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_,
			List p_149666_3_) {
		p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
	}

	@Override
	protected int func_150123_b(int p_150123_1_) {
		return 65;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess iba, int x, int y, int z,
			int side) {
		return true;
	}

	@Override
	public boolean isOpaqueCube() {
		return Blocks.leaves.isOpaqueCube();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderColor(int metadata) {
		return 0xFFFFFF;
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_,
			int p_149720_3_, int p_149720_4_) {
		return 0xFFFFFF;
	}

	@Override
	public String[] func_150125_e() {
		// TODO Auto-generated method stub
		return null;
	}

}
