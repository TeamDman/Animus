package com.teamdman.animus;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by TeamDman on 10/1/2016.
 */
@SuppressWarnings("CanBeFinal")
@Config(modid = Constants.Mod.MODID, name = Constants.Mod.NAME, category = "")
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID)
public class AnimusConfig {
	@Config.Comment({"General Options"})
	public static ConfigGeneral    general    = new ConfigGeneral();
	@Config.Comment({"Ritual List"})
	@Config.RequiresMcRestart
	public static ConfigRitualList ritualList = new ConfigRitualList();
	@Config.Comment({"Ritual Numbers Balance"})
	public static ConfigRituals    rituals    = new ConfigRituals();
	@Config.Comment({"Sigil Numbers Balance"})
	public static ConfigSigils     sigils     = new ConfigSigils();

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		System.out.println("Syncing Animus Config");
		if (event.getModID().equals(Constants.Mod.MODID))
			ConfigManager.sync(event.getModID(), Config.Type.INSTANCE);
	}

	public static class ConfigGeneral {
		public boolean muteDragon = false;
		public boolean muteWither = false;
	}

	public static class ConfigRitualList {
		public boolean ritualCulling        = true;
		public boolean ritualEntropy        = true;
		public boolean ritualHunger         = true;
		public boolean ritualLuna           = true;
		public boolean ritualNaturesLeech   = true;
		public boolean ritualPeace          = true;
		public boolean ritualRegression     = true;
		public boolean ritualSol            = true;
		public boolean ritualSteadfastHeart = true;
		public boolean ritualUnmaking       = true;
		public boolean ritualVengefulSpirit = true;
	}

	public static class ConfigRituals {
		public boolean killWither = true;
		public int     witherCost = 25000;
	}

	@SuppressWarnings("CanBeFinal")
	public static class ConfigSigils {
		public int antimatterConsumption          = 25;
		public int antimatterRange                = 8;
		public int builderRange                   = 64;
		@Config.Comment({"Determines if the transposition sigil is allowed to move unbreakable blocks.",
				"	0: Never move blocks",
				"	1: Allow moving unbreakables, but prevent setting source _position_ to an unbreakable block.",
				"	2: Always allow moving unbreakable blocks"})
		public int transpositionMovesUnbreakables = 1;
	}
}
