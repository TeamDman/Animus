package com.teamdman.animus.blocks;

import WayofTime.bloodmagic.block.IBMBlock;
import WayofTime.bloodmagic.client.IVariantProvider;
import com.teamdman.animus.world.generation.BloodTreeGenerator;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBloodSapling extends BlockSapling implements IVariantProvider, IBMBlock {

	 private BloodTreeGenerator treeGenerator;
	
	public BlockBloodSapling() {
		super();
		this.treeGenerator =  new BloodTreeGenerator(true);
		setSoundType(SoundType.GROUND);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public ItemBlock getItem() {
		return new ItemBlock(this);
	}
	
    @Override
    public void generateTree(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world, rand, pos)) return;
        if (world.isRemote) {
            return;
        }

        world.setBlockToAir(pos);

        if(!treeGenerator.growTree(world, rand, pos)) {
            world.setBlockState(pos, state, 4);
        }
    }
    
  
    
}
