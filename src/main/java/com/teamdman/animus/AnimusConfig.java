package com.teamdman.animus;

import WayofTime.bloodmagic.ConfigHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by TeamDman on 10/1/2016.
 */
public class AnimusConfig {
	private static Configuration config;

	public static Configuration getConfig() {
		return config;
	}

	// Item/Block Disabling
	public static List<String> itemBlacklist;
	public static List<String> blockBlacklist;


	// Ritual Disabling
	public static boolean ritualSol;
	public static boolean ritualLuna;
	public static boolean ritualEntropy;
	public static boolean ritualUnmaking;
	public static boolean ritualPeace;
	public static boolean ritualNaturesLeech;
	public static boolean ritualCulling;
	public static boolean ritualRegression;

	// Sigil Variables
	public static int chainsConsumption;
	public static int transpositionConsumption;
	public static int builderRange;
	public static int antimatterRange;
	public static int antimatterConsumption;
	public static int stormConsumption;

	// General
	public static boolean muteWither;

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
		ritualLuna = config.get(category, "ritualLuna", true).getBoolean();
		ritualEntropy = config.get(category, "ritualEntropy", true).getBoolean();
		ritualUnmaking = config.get(category, "ritualUnmaking", true).getBoolean();
		ritualPeace = config.get(category, "ritualPeace", true).getBoolean();
		ritualNaturesLeech = config.get(category, "ritualNaturesLeech", true).getBoolean();
		ritualCulling = config.get(category, "ritualCulling", true).getBoolean();
		ritualRegression = config.get(category,"ritualRegression",true).getBoolean();

		category = "Sigil Variables";
		config.addCustomCategoryComment(category,"Costs of various actions");
		config.setCategoryRequiresMcRestart(category,false);
		chainsConsumption = config.get(category,"chainsConsumption",500).getInt();
		transpositionConsumption = config.get(category,"transpositionConsumption",2000).getInt();
		builderRange = config.get(category,"builderRange",64).getInt();
		antimatterRange = config.get(category,"antimatterRange",8).getInt();
		antimatterConsumption = config.get(category,"antimatterConsumption",25).getInt();
		stormConsumption = config.get(category,"stormConsumption",1000).getInt();


		category = "General";
		config.addCustomCategoryComment(category,"General Preferences");
		config.setCategoryRequiresMcRestart(category,false);
		muteWither = config.get(category,"muteWither",true).getBoolean();
		config.save();
	}


	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent event)
	{
		if (event.getModID().equals(Animus.MODID))
			ConfigHandler.syncConfig();
	}
}
