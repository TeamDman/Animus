package com.teamdman.animus.blocks;

import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.tiles.TileBloodCore;

import WayofTime.bloodmagic.block.IBMBlock;
import WayofTime.bloodmagic.client.IVariantProvider;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBloodCore extends Block implements IVariantProvider, IBMBlock {


	public BlockBloodCore() {
		super(Material.WOOD);
		this.setHardness(10.0F);
		this.setDefaultState(this.getDefaultState());
		this.setTickRandomly(true);
		this.setSoundType(SoundType.WOOD);
		Blocks.FIRE.setFireInfo(this, 5, 5);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileBloodCore();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public ItemBlock getItem() {
		return new ItemBlock(AnimusBlocks.BLOCKBLOODCORE);
	}
	
}
