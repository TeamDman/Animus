package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

/**
 * Created by TeamDman on 9/25/2016.
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID)
@GameRegistry.ObjectHolder(Constants.Mod.MODID)
public class AnimusBlocks {
	public static final Block       BLOCKANTIMATTER      = Blocks.AIR;
	public static final Block       BLOCKBLOODCORE       = Blocks.AIR;
	public static final BlockLeaves       BLOCKBLOODLEAVES     = Blocks.LEAVES;
	public static final Block       BLOCKBLOODPLANK      = Blocks.AIR;
	public static final BlockSapling       BLOCKBLOODSAPLING    = (BlockSapling) Blocks.SAPLING;
	public static final BlockLog       BLOCKBLOODWOOD       = (BlockLog) Blocks.LOG;
	public static final Block       BLOCKFLUIDANTIMATTER = Blocks.AIR;
	public static final Block       BLOCKFLUIDDIRT       = Blocks.AIR;
	public static final Block       BLOCKPHANTOMBUILDER  = Blocks.AIR;
	public static       List<Block> blocks;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		FluidRegistry.registerFluid(BlockFluidDirt.FLUID_INSTANCE);
		FluidRegistry.addBucketForFluid(BlockFluidDirt.FLUID_INSTANCE);
		FluidRegistry.registerFluid(BlockFluidAntimatter.FLUID_INSTANCE);
		FluidRegistry.addBucketForFluid(BlockFluidAntimatter.FLUID_INSTANCE);
		blocks = Arrays.asList(
				setupBlock(new BlockPhantomBuilder(), "blockphantombuilder"),
				setupBlock(new BlockBloodCore(), "blockbloodcore"),
				setupBlock(new BlockBloodSapling(), "blockbloodsapling"),
				setupBlock(new BlockBloodPlank(), "blockbloodplank"),
				setupBlock(new BlockBloodWood(), "blockbloodwood"),
				setupBlock(new BlockBloodLeaves(), "blockbloodleaves"),
				setupBlock(new BlockAntimatter(), "blockantimatter"),
				setupBlock(new BlockFluidDirt(), "blockfluiddirt"),
				setupBlock(new BlockFluidAntimatter(), "blockfluidantimatter")
		);
		blocks.forEach(event.getRegistry()::register);
	}

	private static Block setupBlock(Block block, String name) {
		if (block.getRegistryName() == null)
			block.setRegistryName(name);

		block.setUnlocalizedName(name);
		block.setCreativeTab(Animus.tabMain);
		return block;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomStateMapper(BLOCKFLUIDDIRT, new StateMapperBase() {
			@SuppressWarnings("NullableProblems")
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				//noinspection ConstantConditions
				return new ModelResourceLocation(Constants.Mod.MODID + ":" + Constants.Misc.FLUID_DIRT, "fluid");
			}
		});
		ModelLoader.setCustomStateMapper(BLOCKFLUIDANTIMATTER, new StateMapperBase() {
			@SuppressWarnings("NullableProblems")
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				//noinspection ConstantConditions
				return new ModelResourceLocation(Constants.Mod.MODID + ":" + Constants.Misc.FLUID_ANTIMATTER, "fluid");
			}
		});
	}
}
