package com.teamdman.animus;

import net.minecraft.util.DamageSource;
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
	@Config.Comment({"Hurt Cooldown (iframes)"})
	public static ConfigHurtCooldown iFrames = new ConfigHurtCooldown();

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		System.out.println("Syncing Animus Config");
		if (event.getModID().equals(Constants.Mod.MODID))
			ConfigManager.sync(event.getModID(), Config.Type.INSTANCE);
	}

	public static class ConfigGeneral {
		public boolean muteDragon    = false;
		public boolean muteWither    = false;
		public boolean canKillBuffedMobs = false;
		public int     bloodPerApple = 50;
	}

	public enum Mode {
		DISABLED,
		WHITELIST,
		BLACKLIST
	}

	public static class ConfigHurtCooldown {
		@Config.Comment({
				"How will the Hurt Cooldown (iframes) of vanilla be affected, per damage source.",
				"An empty list on [Blacklist] mode will remove the cooldown for all damage types."
		})
		public Mode mode = Mode.BLACKLIST;
		@Config.Comment({"If true, bosses will have no iframes."})
		public boolean affectBosses = false;
		@Config.Comment({"If true, players will have no iframes."})
		public boolean affectPlayers = false;
		@Config.Comment({"List to be used when evaluating whitelist/blacklist functionality."})
		public String[] sources = {
				DamageSource.IN_FIRE.getDamageType(),
				DamageSource.IN_WALL.getDamageType(),
				DamageSource.CACTUS.getDamageType(),
				DamageSource.LIGHTNING_BOLT.getDamageType(),
				DamageSource.LAVA.getDamageType(),
				DamageSource.OUT_OF_WORLD.getDamageType()
		};
	}

	public static class ConfigRituals {
		public boolean killWither = true;
		public int     witherCost = 25000;
		@Config.Comment({"Will the ritual of Culling Destroy Primed TNT true/false"})
		public boolean CullingKillsTnT = true;
		@Config.Comment({"How much should each point of flux be multiplied by when converting to demon will 0 for no will generation."})
		public int     fluxToWillConversionMultiplier = 1;
		@Config.Comment({"Eldritch Will ritual radius in chunks default is 0 for single chunk, or 1 for a 3x3 chunk area."})
		public int     willRadius = 0;
		@Config.Comment({"Maximum amount of flux drained per update for Eldritch Will. Must be non zero positive number"})
		public int     fluxDrainMax = 10;
		@Config.Comment({"Eldritch Will update speed. Should be non zero positive number."})
		public int     eldritchWillSpeed = 30;
		@Config.Comment({"Eldritch Will cost per update. Should be non zero positive number."})
		public int     eldritchWillCost = 60;		
		@Config.Comment({"Added debug logging for culling ritual."})
		public boolean CullingDebug = false;
		@Config.Comment({"Upkeep cost for ritual of peace."})
		public int peaceCost = 1000;
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
		public String[] leechBlacklist = {"ic2:te", "minecraft:grass"};
	}
}
