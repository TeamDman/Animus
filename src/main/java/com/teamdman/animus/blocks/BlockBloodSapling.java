package com.teamdman.animus.blocks;

import WayofTime.bloodmagic.block.IBMBlock;
import WayofTime.bloodmagic.client.IVariantProvider;
import com.teamdman.animus.world.generation.BloodTreeGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockBloodSapling extends BlockSapling implements IVariantProvider, IBMBlock {

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

	@Override
	public ItemBlock getItem() {
		return new ItemBlock(this);
	}
}
