package com.teamdman.animus.blocks;

import WayofTime.bloodmagic.core.RegistrarBloodMagicBlocks;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockFluidAntimatter extends BlockFluidClassic {
	public static final FluidAntimatter FLUID_INSTANCE = new FluidAntimatter();

	public BlockFluidAntimatter() {
		super(FLUID_INSTANCE, new MaterialLiquid(MapColor.WHITE_STAINED_HARDENED_CLAY));
		setUnlocalizedName(Constants.Mod.MODID + ".fluid." + fluidName);
		setRegistryName(fluidName);
		FLUID_INSTANCE.setBlock(this);
	}

	@Override
	public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
		for (EnumFacing dir : EnumFacing.VALUES) {
			IBlockState offState = world.getBlockState(pos.offset(dir));
			if (offState.getBlock() == RegistrarBloodMagicBlocks.LIFE_ESSENCE)
				world.setBlockState(pos.offset(dir), AnimusBlocks.BLOCKFLUIDANTIMATTER.getDefaultState());
			else if (!world.isAirBlock(pos.offset(dir)) && offState.getBlock() != AnimusBlocks.BLOCKANTIMATTER && offState.getBlock() != AnimusBlocks.BLOCKFLUIDANTIMATTER)
				world.setBlockState(pos.offset(dir), AnimusBlocks.BLOCKANTIMATTER.getDefaultState());
		}
		super.updateTick(world, pos, state, rand);
	}

	private static class FluidAntimatter extends Fluid {
		private FluidAntimatter() {
			super(Constants.Misc.FLUID_ANTIMATTER, Constants.Resource.fluidAntimatterStill, Constants.Resource.fluidAntimatterFlowing);
			setUnlocalizedName(Constants.Misc.FLUID_ANTIMATTER);
			setRarity(EnumRarity.UNCOMMON);
			setDensity(10000);
			setViscosity(1);
			setTemperature(0);
		}
	}
}
