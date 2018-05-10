package com.teamdman.animus;

import java.util.Locale;

public class Constants {
	//todo: add flavour texts
	public static class Localizations {
		public static class Text {
			public static final String DIVINER_MISSING       = "text.component.diviner.missing";
			public static final String DIVINER_OBSTRUCTED    = "text.component.diviner.obstructed";
			public static final String TRANSPOSITION_CLEARED = "text.component.transposition.cleared";
			public static final String TRANSPOSITION_SET     = "text.component.transposition.set";
		}

		public static class Tooltips {
			public static final String DIVINER_FIRST               = "tooltip.animus.diviner.first";
			public static final String DIVINER_SECOND              = "tooltip.animus.diviner.second";
			public static final String DIVINER_THIRD               = "tooltip.animus.diviner.third";
			public static final String HEALING_FLAVOUR             = "tooltip.animus.healing.flavour";
			public static final String HEALING_INFO                = "tooltip.animus.healing.info";
			public static final String KAMA_FIRST                  = "tooltip.animus.kama_bound.first";
			public static final String KAMA_SECOND                 = "tooltip.animus.kama_bound.second";
			public static final String KEY                         = "tooltip.animus.key";
			public static final String OWNER                       = "tooltip.animus.currentOwner";
			public static final String SIGIL_BUILDER_FLAVOUR       = "tooltip.animus.sigil.builder.flavour";
			public static final String SIGIL_CHAINS_FLAVOUR        = "tooltip.animus.sigil.chains.flavour";
			public static final String SIGIL_CONSUMPTION_FLAVOUR   = "tooltip.animus.sigil.consumption.flavour";
			public static final String SIGIL_LEECH_FLAVOUR         = "tooltip.animus.sigil.leech.flavour";
			public static final String SIGIL_STORM_FLAVOUR         = "tooltip.animus.sigil.storm.flavour";
			public static final String SIGIL_TRANSPOSITION_FLAVOUR = "tooltip.animus.sigil.transposition.flavour";
			public static final String SIGIL_TRANSPOSITION_STORED  = "tooltip.animus.sigil.transposition.stored";

		}
	}

	public static class Misc {
		public static final String CRAFTING_KEYBINDING = "keybindingcrafting";
		public static final String DAMAGE_ABSOLUTE     = "animus.absolute";
	}

	public static class Mod {
		public static final String DEPEND  = "required-after:bloodmagic;required-after:guideapi;after:theoneprobe;";
		public static final String MODID   = "animus";
		public static final String DOMAIN  = MODID.toLowerCase(Locale.ENGLISH) + ":";
		public static final String NAME    = "Animus";
		public static final String VERSION = "1";
	}

	public static class NBT {
		public static final String CULLING_BUFFER_WILL = "willBuffer";
		public static final String SOUL_DATA           = "mobdata";
		public static final String SOUL_ENTITY_ID      = "id";
		public static final String SOUL_ENTITY_NAME    = "entity";
		public static final String SOUL_NAME           = "name";
	}

	public static class Rituals {
		public static final String HUNGER     = "ritualHunger";
		public static final String REGRESSION = "ritualRegression";
		public static final String CULLING    = "ritualCulling";
		public static final String ENTROPY    = "ritualEntropy";
		public static final String LEECH      = "ritualNaturesLeech";
		public static final String LUNA       = "ritualLuna";
		public static final String PEACE      = "ritualPeace";
		public static final String SOL        = "ritualSol";
		public static final String STEADFAST  = "ritualSteadfastHeart";
		public static final String UNMAKING   = "ritualUnmaking";

	}

	public static class Sigils {
		public static final String BUILDER       = "builder";
		public static final String CHAINS        = "chains";
		public static final String CONSUMPTION   = "consumption";
		public static final String LEECH         = "leech";
		public static final String STORM         = "storm";
		public static final String TRANSPOSITION = "transposition";
	}
}
