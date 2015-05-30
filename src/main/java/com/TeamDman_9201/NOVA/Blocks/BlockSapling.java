package com.teamdman_9201.nova.blocks;

import com.teamdman_9201.nova.NOVA;
import com.teamdman_9201.nova.generation.WorldGenTree;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-04-13.
 */
public class BlockSapling extends net.minecraft.block.BlockSapling implements IGrowable {

    IIcon icon;

    public BlockSapling() {
        float f = 0.4F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    public void updateTick(World world, int posX, int posY, int posZ, Random rnd) {
        if (!world.isRemote) {
            super.updateTick(world, posX, posY, posZ, rnd);
            if (world.getBlockLightValue(posX, posY + 1, posZ) >= 9 && rnd.nextInt(2) == 0) {
                makeTree(world, posX, posY, posZ, rnd);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return icon;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.icon = register.registerIcon(NOVA.MODID + ":blockSapling");
    }

    public void makeTree(World world, int posX, int posY, int posZ, Random rnd) {
        if (!TerrainGen.saplingGrowTree(world, rnd, posX, posY, posZ)) {
            System.out.println("!saplingGrowTree");
            return;
        }
        int l = world.getBlockMetadata(posX, posY, posZ) & 7;
        System.out.println("l:" + l);
        Object  object    = new WorldGenTree(true);//.nextInt(10) == 0 ? new WorldGenBigTree
        // (true) : new WorldGenTrees(true);
        int     i1        = 0;
        int     j1        = 0;
        boolean isBigTree = false;

        if (l == 1) {
            for (i1 = 0; i1 >= -1; --i1) {
                for (j1 = 0; j1 >= -1; --j1) {
                    if (this.isThisSapling(world, posX + i1, posY, posZ + j1, 1) && this
                            .isThisSapling(world, posX + i1 + 1, posY, posZ + j1, 1) && this
                            .isThisSapling(world, posX + i1, posY, posZ + j1 + 1, 1) && this
                            .isThisSapling(world, posX + i1 + 1, posY, posZ + j1 + 1, 1)) {
                        object = new WorldGenMegaPineTree(false, rnd.nextBoolean());
                        isBigTree = true;
                    }
                }
            }
            if (!isBigTree) {
                j1 = 0;
                i1 = 0;
                object = new WorldGenTaiga2(true);
            }
        }

        Block block = Blocks.air;

        if (isBigTree) {
            world.setBlock(posX + i1, posY, posZ + j1, block, 0, 4);
            world.setBlock(posX + i1 + 1, posY, posZ + j1, block, 0, 4);
            world.setBlock(posX + i1, posY, posZ + j1 + 1, block, 0, 4);
            world.setBlock(posX + i1 + 1, posY, posZ + j1 + 1, block, 0, 4);
        } else {
            world.setBlock(posX, posY, posZ, block, 0, 4);
        }

        if (!((WorldGenerator) object).generate(world, rnd, posX + i1, posY, posZ + j1)) {
            if (isBigTree) {
                world.setBlock(posX + i1, posY, posZ + j1, this, l, 4);
                world.setBlock(posX + i1 + 1, posY, posZ + j1, this, l, 4);
                world.setBlock(posX + i1, posY, posZ + j1 + 1, this, l, 4);
                world.setBlock(posX + i1 + 1, posY, posZ + j1 + 1, this, l, 4);
            } else {
                world.setBlock(posX, posY, posZ, this, l, 4);
            }
        }
    }

    public boolean isThisSapling(World world, int posX, int posY, int posZ, int meta) {
        return world.getBlock(posX, posY, posZ) == this;
    }

    public boolean func_149851_a(World world, int posX, int posY, int posZ, boolean p_149851_5_) {
        return true;
    }

    public boolean func_149852_a(World world, Random rnd, int posX, int posY, int posZ) {
        return (double) world.rand.nextFloat() < 0.45D;
    }

    public void func_149853_b(World world, Random rnd, int posX, int posY, int posZ) {
        this.makeTree(world, posX, posY, posZ, rnd);
    }

    @Override
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
        p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
    }
}