package com.teamdman.animus.blocks;

import com.teamdman.animus.Constants;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockFluidDirt extends BlockFluidClassic {
	public static final FluidDirt FLUID_INSTANCE = new FluidDirt();

	public BlockFluidDirt() {
		super(FLUID_INSTANCE, new MaterialLiquid(MapColor.BROWN));
		setUnlocalizedName(Constants.Mod.MODID + ".fluid." + fluidName);
		setRegistryName(fluidName);
		FLUID_INSTANCE.setBlock(this);
	}

	@Override
	public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
		super.updateTick(world, pos, state, rand);
		if (state.getValue(BlockFluidBase.LEVEL) > 6)
			world.setBlockState(pos, Blocks.DIRT.getDefaultState());
		else
			for (EnumFacing face : EnumFacing.VALUES) {
				IBlockState offState = world.getBlockState(pos.offset(face));
				if (offState.getBlock() == Blocks.DIRT)
					world.setBlockState(pos, Blocks.DIRT.getDefaultState());
			}
	}

	private static class FluidDirt extends Fluid {
		private FluidDirt() {
			super(Constants.Misc.FLUID_DIRT, Constants.Resource.fluidDirtStill, Constants.Resource.fluidDirtFlowing);
			setUnlocalizedName(Constants.Misc.FLUID_DIRT);
			setRarity(EnumRarity.UNCOMMON);
			setDensity(750);
			setViscosity(200);
			setTemperature(200);
			setFillSound(SoundEvents.ITEM_BUCKET_FILL);
		}
	}

}
