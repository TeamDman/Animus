package com.teamdman.animus.blocks;

import java.util.Random;

import javax.annotation.Nonnull;

import com.teamdman.animus.Constants;
import com.teamdman.animus.world.generation.BloodTreeGenerator;

import WayofTime.bloodmagic.client.IVariantProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBloodSapling extends BlockSapling implements IVariantProvider {

	 private BloodTreeGenerator treeGenerator;
	
	public BlockBloodSapling() {
		super();
		setSoundType(SoundType.GROUND);
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
    
	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "normal");
	}
    

}
