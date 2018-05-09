package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.BlockAntimatter;
import com.teamdman.animus.blocks.BlockBloodCore;
import com.teamdman.animus.blocks.BlockBloodLeaves;
import com.teamdman.animus.blocks.BlockBloodPlank;
import com.teamdman.animus.blocks.BlockBloodSapling;
import com.teamdman.animus.blocks.BlockBloodWood;
import com.teamdman.animus.blocks.BlockPhantomBuilder;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Arrays;
import java.util.List;

/**
 * Created by TeamDman on 9/25/2016.
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID)
@GameRegistry.ObjectHolder(Constants.Mod.MODID)
public class AnimusBlocks {
	public static final Block BLOCKANTIMATTER     = Blocks.AIR;
	public static final Block BLOCKPHANTOMBUILDER = Blocks.AIR;
	public static final Block BLOCKBLOODCORE = Blocks.AIR;
	public static final Block BLOCKBLOODSAPLING = Blocks.AIR;
	public static final Block BLOCKBLOODPLANK = Blocks.AIR;
	public static final Block BLOCKBLOODWOOD = Blocks.AIR;
	public static final Block BLOCKBLOODLEAVES = Blocks.AIR;
	public static List<Block> blocks;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		blocks = Arrays.asList(
				setupBlock(new BlockPhantomBuilder(), "blockphantombuilder"),
				setupBlock(new BlockBloodCore(), "blockbloodcore"),
				setupBlock(new BlockBloodSapling(), "blockbloodsapling"),
				setupBlock(new BlockBloodPlank(), "blockbloodplank"),
				setupBlock(new BlockBloodWood(), "blockbloodwood"),
				setupBlock(new BlockBloodLeaves(), "blockbloodleaves"),
				setupBlock(new BlockAntimatter(), "blockantimatter")
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
}
