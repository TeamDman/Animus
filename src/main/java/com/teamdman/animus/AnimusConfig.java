package com.teamdman.animus;

import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.BlockStack;
import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by TeamDman on 10/1/2016.
 */
public class AnimusConfig {
	private static Configuration config;


	// Item/Block Disabling
	public static List<String> itemBlacklist;
	public static List<String> blockBlacklist;


	// Ritual Disabling
	public static boolean ritualSol;

	public static void init(File file)
	{
		config = new Configuration(file);
		syncConfig();
	}

	public static void syncConfig() {
		String category;

		category = "Item/Block Blacklisting";
		config.addCustomCategoryComment(category, "Allows disabling of specific Blocks/Items.\nNote that using this may result in crashes. Use is not supported.");
		config.setCategoryRequiresMcRestart(category, true);
		itemBlacklist = Arrays.asList(config.getStringList("itemBlacklist", category, new String[] {}, "Items to not be registered. This requires their mapping name. Usually the same as the class name. Can be found in F3+H mode."));
		blockBlacklist = Arrays.asList(config.getStringList("blockBlacklist", category, new String[] {}, "Blocks to not be registered. This requires their mapping name. Usually the same as the class name. Can be found in F3+H mode."));

		category = "Rituals";
		config.addCustomCategoryComment(category, "Ritual toggling");
		config.setCategoryRequiresMcRestart(category, true);
		ritualSol = config.get(category, "ritualSol", true).getBoolean();

		config.save();
	}


	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent event)
	{
		if (event.getModID().equals(Animus.MODID))
			ConfigHandler.syncConfig();
	}
}
