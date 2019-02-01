package com.teamdman.animus.blocks;

import java.util.List;
import java.util.Random;

import com.teamdman.animus.Animus;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBloodLeaves extends BlockLeaves {

	public BlockBloodLeaves() {
		this.setDefaultState(this.getDefaultState());
		Blocks.FIRE.setFireInfo(this, 30, 60);;
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		List<ItemStack> list = new java.util.ArrayList<>();
		list.add(new ItemStack(this, 1, 0));
		return list;
	}

	
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
    
	@Override
	public BlockPlanks.EnumType getWoodType(int meta) {
		return null;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return fancyLeaves() ? BlockRenderLayer.CUTOUT_MIPPED : super.getRenderLayer();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		if(!fancyLeaves()){
			return super.isOpaqueCube(state);
		}
		return false;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	public int getBlockColor() {
		return 16777215;
	}

	@SideOnly(Side.CLIENT)
	public int getRenderColor(IBlockState state) {
		return 16777215;
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
		return 16777215;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.SAPLING);
	}
	
	public boolean fancyLeaves(){
		return Animus.proxy.fancyGraphics();
	}
	
}
