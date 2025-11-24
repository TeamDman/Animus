package com.teamdman.animus;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class Constants {
    public static class Localizations {
        public static class Text {
            public static final String DIVINER_MISSING = "text.component.animus.diviner.missing";
            public static final String DIVINER_OBSTRUCTED = "text.component.animus.diviner.obstructed";
            public static final String TRANSPOSITION_CLEARED = "text.component.animus.transposition.cleared";
            public static final String TRANSPOSITION_SET = "text.component.animus.transposition.set";
            public static final String TRANSPOSITION_UNMOVABLE = "text.component.animus.transposition.unmovable";
            public static final String TICKET_SOL = "text.component.animus.ticket.sol";
            public static final String TICKET_TRANSPOSITION = "text.component.animus.ticket.transposition";
            public static final String TICKET_STORM = "text.component.animus.ticket.storm";
            public static final String TICKET_ANTIMATTER = "text.component.animus.ticket.antimatter";
            public static final String TICKET_PEACE = "text.component.animus.ticket.peace";
            public static final String TICKET_ENTROPY = "text.component.animus.ticket.entropy";
            public static final String TICKET_LUNA = "text.component.animus.ticket.luna";
            public static final String TICKET_BUILDER = "text.component.animus.ticket.builder";
            public static final String TICKET_CULLING = "text.component.animus.ticket.culling";
            public static final String TICKET_LEECH = "text.component.animus.ticket.leech";
            public static final String TICKET_ELDRITCH = "text.component.animus.ticket.eldritch";
            public static final String TICKET_UNMAKING = "text.component.animus.ticket.unmaking";
            public static final String TICKET_APPLE = "text.component.animus.ticket.apple";
            public static final String TICKET_CHAINS = "text.component.animus.ticket.chains";
            public static final String TICKET_STEADFAST = "text.component.animus.ticket.steadfast";
        }

        public static class Tooltips {
            public static final String DIVINER_FIRST = "tooltip.animus.diviner.first";
            public static final String DIVINER_SECOND = "tooltip.animus.diviner.second";
            public static final String DIVINER_THIRD = "tooltip.animus.diviner.third";
            public static final String HEALING_FLAVOUR = "tooltip.animus.healing.flavour";
            public static final String HEALING_INFO = "tooltip.animus.healing.info";
            public static final String KAMA_FIRST = "tooltip.animus.kama_bound.first";
            public static final String KAMA_SECOND = "tooltip.animus.kama_bound.second";
            public static final String KEY = "tooltip.animus.key";
            public static final String OWNER = "tooltip.animus.currentOwner";
            public static final String SIGIL_BUILDER_FLAVOUR = "tooltip.animus.sigil.builder.flavour";
            public static final String SIGIL_CHAINS_FLAVOUR = "tooltip.animus.sigil.chains.flavour";
            public static final String SIGIL_CONSUMPTION_FLAVOUR = "tooltip.animus.sigil.consumption.flavour";
            public static final String SIGIL_LEECH_FLAVOUR = "tooltip.animus.sigil.leech.flavour";
            public static final String SIGIL_STORM_FLAVOUR = "tooltip.animus.sigil.storm.flavour";
            public static final String SIGIL_TRANSPOSITION_FLAVOUR = "tooltip.animus.sigil.transposition.flavour";
            public static final String SIGIL_TRANSPOSITION_STORED = "tooltip.animus.sigil.transposition.stored";
        }
    }

    public static class Misc {
        public static final String CRAFTING_KEYBINDING = "keybindingcrafting";
        public static final String CRAFTING_DIRTBUCKET = "dirtbucketcrafting";
        public static final String DAMAGE_ABSOLUTE = "animus.absolute";
        public static final String FLUID_ANTIMATTER = "blockfluidantimatter";
        public static final String FLUID_DIRT = "blockfluiddirt";
    }

    public static class Resource {
        public static final ResourceLocation fluidAntimatterFlowing = ResourceLocation.fromNamespaceAndPath(Mod.MODID, "block/fluid/antimatter_flowing");
        public static final ResourceLocation fluidAntimatterStill = ResourceLocation.fromNamespaceAndPath(Mod.MODID, "block/fluid/antimatter_still");
        public static final ResourceLocation fluidDirtFlowing = ResourceLocation.fromNamespaceAndPath(Mod.MODID, "block/fluid/dirt_flowing");
        public static final ResourceLocation fluidDirtStill = ResourceLocation.fromNamespaceAndPath(Mod.MODID, "block/fluid/dirt_still");
    }

    public static class Mod {
        public static final String MODID = "animus";
        public static final String DOMAIN = MODID.toLowerCase(Locale.ENGLISH) + ":";
        public static final String NAME = "Animus";
        public static final String VERSION = "3.0.0";
    }

    public static class NBT {
        public static final String CULLING_BUFFER_WILL = "willBuffer";
        public static final String SOUL_DATA = "mobdata";
        public static final String SOUL_ENTITY_ID = "id";
        public static final String SOUL_ENTITY_NAME = "entity";
        public static final String SOUL_NAME = "name";
        public static final String TRANSPOSITION_POS = "pos";
    }

    public static class Rituals {
        public static final String CULLING = "ritualCulling";
        public static final String ENTROPY = "ritualEntropy";
        public static final String HUNGER = "ritualHunger";
        public static final String LEECH = "ritualNaturesLeech";
        public static final String ELDRITCH = "ritualEldritchWill";
        public static final String LUNA = "ritualLuna";
        public static final String PEACE = "ritualPeace";
        public static final String REGRESSION = "ritualRegression";
        public static final String SOL = "ritualSol";
        public static final String STEADFAST = "ritualSteadfastHeart";
        public static final String UNMAKING = "ritualUnmaking";
    }

    public static class Sigils {
        public static final String BUILDER = "builder";
        public static final String CHAINS = "chains";
        public static final String CONSUMPTION = "consumption";
        public static final String LEECH = "leech";
        public static final String STORM = "storm";
        public static final String TRANSPOSITION = "transposition";
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Mod.MODID, path);
    }
}
