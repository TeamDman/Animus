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
	@Config.Comment({"Rituals"})
	public static ConfigRituals    rituals    = new ConfigRituals();
	@Config.Comment({"Sigils"})
	public static ConfigSigils     sigils     = new ConfigSigils();

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		System.out.println("Syncing Animus Config");
		if (event.getModID().equals(Constants.Mod.MODID))
			ConfigManager.sync(event.getModID(), Config.Type.INSTANCE);
	}

	public static class ConfigGeneral {
		public boolean muteDragon    = false;
		public boolean muteWither    = false;
		@Config.Comment({"Should Animus override the vanilla invulnerablity time between hits for everything?"})		
		public boolean disableHurtCooldown = false;
		@Config.Comment({"Should Animus override the vanilla invulnerablity time between hits for bosses"})		
		public boolean disableHurtCooldownBoss = false;
		@Config.Comment({"Should Animus override the vanilla invulnerablity time between hits for players"})		
		public boolean disableHurtCooldownPlayers = false;
		@Config.Comment({"Determines if the Ritual of Culling can kill mobs with potion effects"})		
		public boolean canKillBuffedMobs = false;
		public int     bloodPerApple = 50;
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
				"	0: Never move unbreakable blocks.",
				"	1: Allow moving unbreakables, but prevent setting source _position_ to an unbreakable block.",
				"	2: Always allow moving unbreakable blocks."})
		public int transpositionMovesUnbreakables = 1;
		public String[] leechBlacklist = {"ic2:te"};
	}
}
