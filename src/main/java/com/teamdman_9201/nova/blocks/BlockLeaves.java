package com.teamdman_9201.nova.blocks;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-04-14.
 */
public class BlockLeaves extends BlockNewLeaf {

    IIcon icon;

    int[] leafNodes;

    @Override
    public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
        return icon;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icon = register.registerIcon(NOVA.MODID + ":blockLeaves");
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return NOVA.itemBlockSapling;
    }

    /**
     * Ticks the block if it's been scheduled
     */
    @Override
    public void updateTick(World world, int posX, int posY, int posZ, Random rnd) {
        if (!world.isRemote) {
            int l = world.getBlockMetadata(posX, posY, posZ);
            byte b0 = 4;
            int i1 = b0 + 1;
            byte b1 = 32;
            int j1 = b1 * b1;
            int k1 = b1 / 2;

            if (this.leafNodes == null) {
                this.leafNodes = new int[b1 * b1 * b1];
            }

            int l1;

            if (world.checkChunksExist(posX - i1, posY - i1, posZ - i1, posX + i1, posY + i1,
                    posZ + i1)) {
                int i2;
                int j2;

                for (l1 = -b0; l1 <= b0; ++l1) {
                    for (i2 = -b0; i2 <= b0; ++i2) {
                        for (j2 = -b0; j2 <= b0; ++j2) {
                            Block block = world.getBlock(posX + l1, posY + i2, posZ + j2);

                            if (!block.canSustainLeaves(world, posX + l1, posY + i2, posZ + j2)) {
                                if (block.isLeaves(world, posX + l1, posY + i2, posZ + j2)) {
                                    this.leafNodes[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -2;
                                } else {
                                    this.leafNodes[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -1;
                                }
                            } else {
                                this.leafNodes[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = 0;
                            }
                        }
                    }
                }

                for (l1 = 1; l1 <= 4; ++l1) {
                    for (i2 = -b0; i2 <= b0; ++i2) {
                        for (j2 = -b0; j2 <= b0; ++j2) {
                            for (int k2 = -b0; k2 <= b0; ++k2) {
                                if (this.leafNodes[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] ==
                                        l1 - 1) {
                                    if (this.leafNodes[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 +
                                            k1] == -2) {
                                        this.leafNodes[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 +
                                                k1] = l1;
                                    }

                                    if (this.leafNodes[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 +
                                            k1] == -2) {
                                        this.leafNodes[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 +
                                                k1] = l1;
                                    }

                                    if (this.leafNodes[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 +
                                            k1] == -2) {
                                        this.leafNodes[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 +
                                                k1] = l1;
                                    }

                                    if (this.leafNodes[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 +
                                            k1] == -2) {
                                        this.leafNodes[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 +
                                                k1] = l1;
                                    }

                                    if (this.leafNodes[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1
                                            - 1)] == -2) {
                                        this.leafNodes[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1
                                                - 1)] = l1;
                                    }

                                    if (this.leafNodes[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1
                                            + 1] == -2) {
                                        this.leafNodes[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1
                                                + 1] = l1;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            l1 = this.leafNodes[k1 * j1 + k1 * b1 + k1];

            if (l1 >= 0) {
                world.setBlockMetadataWithNotify(posX, posY, posZ, l & -9, 4);
            } else {
                this.removeLeaves(world, posX, posY, posZ);
            }

        }
    }

    private void removeLeaves(World world, int posX, int posY, int posZ) {
    	if (world.rand.nextInt(100) < 20){ //20% chance of apple drop, was 100%
        this.dropBlockAsItem(world, posX, posY, posZ, world.getBlockMetadata(posX, posY, posZ), 0);
        world.setBlockToAir(posX, posY, posZ);
        EntityItem drop = new EntityItem(world,posX,posY,posZ);
        drop.setEntityItemStack(new ItemStack(NOVA.itemBloodApple));
        world.spawnEntityInWorld(drop);
    	}
    }

    @Override
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
        p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
    }
}
