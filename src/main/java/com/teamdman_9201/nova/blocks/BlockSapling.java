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
import net.minecraftforge.common.util.ForgeDirection;
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
    	super();
        float f = 0.4F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }


    
    public boolean canPlantGrowOnThisBlock(Block block, World world, int x, int y, int z){
    	System.out.println("Checking can sutain");
        return block.canSustainPlant(world, x, y, z, ForgeDirection.UP, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int metadata) {
        return icon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister register) {
        this.icon = register.registerIcon(NOVA.MODID + ":blockSapling");
    }

    
    @Override
    public void func_149878_d(World world, int x, int y, int z, Random rand)
    {
        if(!world.isRemote)
        {
            int meta = world.getBlockMetadata(x, y, z);

            world.setBlockToAir(x, y, z);
            WorldGenerator tree = new WorldGenTree(true, 5);
            if (!tree.generate(world, rand, x, y, z))
            {
                world.setBlock(x, y, z, this, meta, 2);
            }
        
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
        list.add(new ItemStack(this, 1, 0));
    }

    @Override
    public Item getItemDropped(int wut, Random random, int yeah)
    {
        return Item.getItemFromBlock(this);
    }
    
    public boolean isThisSapling(World world, int posX, int posY, int posZ, int meta) {
    	System.out.println("checking if block is this sapling");
    	return world.getBlock(posX, posY, posZ) == this;
    }

}