package com.teamdman.animus.blocks;

import WayofTime.bloodmagic.block.IBMBlock;
import WayofTime.bloodmagic.client.IVariantProvider;

import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.world.generation.BloodTreeGenerator;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockBloodSapling extends BlockSapling implements IVariantProvider, IBMBlock {

	 private BloodTreeGenerator treeGenerator;
	
	public BlockBloodSapling() {
		super();
		this.treeGenerator =  new BloodTreeGenerator(true);
		this.setDefaultState(this.getDefaultState().withProperty(STAGE, 0));
		setSoundType(SoundType.PLANT);		
	}
	
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }	
	
	
	@Override
	public ItemBlock getItem() {
		return new ItemBlock(AnimusBlocks.BLOCKBLOODSAPLING);
	}
	
    @Override
    public void generateTree(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world, rand, pos)) return;
        if (world.isRemote) {
            return;
        }

        world.setBlockToAir(pos);
//		TODO: Enable
//        if(!treeGenerator.growTree(world, rand, pos)) {
//            world.setBlockState(pos, state, 4);
//        }
    }
    
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
	}  
    
}
