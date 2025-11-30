package com.teamdman.animus;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

public class Constants {
    private Constants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static class Localizations {
        private Localizations() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static class Text {
            private Text() {
                throw new UnsupportedOperationException("Utility class");
            }
            public static final String DIVINER_MISSING = "text.component.animus.diviner.missing";
            public static final String DIVINER_OBSTRUCTED = "text.component.animus.diviner.obstructed";
            public static final String TRANSPOSITION_CLEARED = "text.component.animus.transposition.cleared";
            public static final String TRANSPOSITION_SET = "text.component.animus.transposition.set";
            public static final String TRANSPOSITION_UNMOVABLE = "text.component.animus.transposition.unmovable";
            public static final String CHAINS_CAPTURE_FAILED = "text.component.animus.chains.capture_failed";
            public static final String HEALING_CANNOT_DROP = "text.component.animus.healing.cannot_drop";
            public static final String HEALING_WARNING = "text.component.animus.healing.warning";
            public static final String SACRIFICE_TOO_POWERFUL = "text.component.animus.sacrifice.too_powerful";
            public static final String ACTIVATION_CRYSTAL_UNBOUND = "text.component.animus.activation_crystal.unbound";
            public static final String ACTIVATION_CRYSTAL_SHATTERED = "text.component.animus.activation_crystal.shattered";
            public static final String TICKET_PERSISTENCE = "text.component.animus.ticket.persistence";
            public static final String TICKET_SOL = "text.component.animus.ticket.sol";
            public static final String TICKET_TRANSPOSITION = "text.component.animus.ticket.transposition";
            public static final String TICKET_STORM = "text.component.animus.ticket.storm";
            public static final String TICKET_ANTILIFE = "text.component.animus.ticket.antilife";
            public static final String TICKET_PEACEFUL_BECKONING = "text.component.animus.ticket.peaceful_beckoning";
            public static final String TICKET_ENTROPY = "text.component.animus.ticket.entropy";
            public static final String TICKET_LUNA = "text.component.animus.ticket.luna";
            public static final String TICKET_BUILDER = "text.component.animus.ticket.builder";
            public static final String TICKET_CULLING = "text.component.animus.ticket.culling";
            public static final String TICKET_CONSUMPTION = "text.component.animus.ticket.consumption";
            public static final String TICKET_LEACH = "text.component.animus.ticket.leach";
            public static final String TICKET_ELDRITCH = "text.component.animus.ticket.eldritch";
            public static final String TICKET_UNMAKING = "text.component.animus.ticket.unmaking";
            public static final String TICKET_APPLE = "text.component.animus.ticket.apple";
            public static final String TICKET_CHAINS = "text.component.animus.ticket.chains";
            public static final String TICKET_STEADFAST = "text.component.animus.ticket.steadfast";
            public static final String TICKET_HEAVENLY_WRATH = "text.component.animus.ticket.heavenly_wrath";
            public static final String TICKET_REMEDIUM = "text.component.animus.ticket.remedium";
            public static final String TICKET_REPARARE = "text.component.animus.ticket.reparare";
            public static final String TICKET_SERENITY = "text.component.animus.ticket.serenity";
            public static final String TICKET_NOLITE_IGNEM = "text.component.animus.ticket.nolite_ignem";
            public static final String TICKET_RELENTLESS_TIDES = "text.component.animus.ticket.relentless_tides";
            public static final String TICKET_SIPHON = "text.component.animus.ticket.siphon";
            public static final String TICKET_SOURCE_VITAEUM = "text.component.animus.ticket.source_vitaeum";
            public static final String TICKET_FREE_SOUL = "text.component.animus.ticket.free_soul";
            public static final String TICKET_TEMPORAL_DOMINANCE = "text.component.animus.ticket.temporal_dominance";
            public static final String TICKET_EQUIVALENCY = "text.component.animus.ticket.equivalency";
            public static final String EQUIVALENCY_CLEARED = "text.component.animus.equivalency.cleared";
            public static final String EQUIVALENCY_ALREADY_SELECTED = "text.component.animus.equivalency.already_selected";
            public static final String EQUIVALENCY_MAX_SELECTED = "text.component.animus.equivalency.max_selected";
            public static final String EQUIVALENCY_NO_BLOCKS = "text.component.animus.equivalency.no_blocks";
            public static final String EQUIVALENCY_ADDED = "text.component.animus.equivalency.added";
            public static final String EQUIVALENCY_NO_SELECTION = "text.component.animus.equivalency.no_selection";
            public static final String EQUIVALENCY_NO_MATCHES = "text.component.animus.equivalency.no_matches";
            public static final String EQUIVALENCY_NO_LP = "text.component.animus.equivalency.no_lp";
            public static final String EQUIVALENCY_STARTED = "text.component.animus.equivalency.started";

            // Free Soul Sigil
            public static final String FREE_SOUL_ALREADY_SPECTATOR = "text.component.animus.free_soul.already_spectator";
            public static final String FREE_SOUL_NO_LP = "text.component.animus.free_soul.no_lp";
            public static final String FREE_SOUL_ACTIVATED = "text.component.animus.free_soul.activated";
            public static final String FREE_SOUL_SAVED = "text.component.animus.free_soul.saved";
            public static final String FREE_SOUL_ON_COOLDOWN = "text.component.animus.free_soul.on_cooldown";
            public static final String FREE_SOUL_RETURNING = "text.component.animus.free_soul.returning";
            public static final String FREE_SOUL_EXPIRED = "text.component.animus.free_soul.expired";

            // Remedium Sigil
            public static final String REMEDIUM_ACTIVATED = "text.component.animus.remedium.activated";
            public static final String REMEDIUM_DEACTIVATED = "text.component.animus.remedium.deactivated";
            public static final String REMEDIUM_NO_LP = "text.component.animus.remedium.no_lp";

            // Reparare Sigil
            public static final String REPARARE_ACTIVATED = "text.component.animus.reparare.activated";
            public static final String REPARARE_DEACTIVATED = "text.component.animus.reparare.deactivated";
            public static final String REPARARE_NO_LP = "text.component.animus.reparare.no_lp";

            // Key of Binding
            public static final String KEY_CANNOT_BIND_KEY = "text.component.animus.key.cannot_bind_key";
            public static final String KEY_ITEM_BOUND = "text.component.animus.key.item_bound";

            // Blood Core
            public static final String BLOOD_CORE_SPREADING_ENABLED = "text.component.animus.blood_core.spreading_enabled";
            public static final String BLOOD_CORE_SPREADING_DISABLED = "text.component.animus.blood_core.spreading_disabled";

            // Bound Pilum
            public static final String PILUM_BOUND_SUCCESS = "text.component.animus.pilum.bound_success";
            public static final String PILUM_ACTIVATED = "text.component.animus.pilum.activated";
            public static final String PILUM_DEACTIVATED = "text.component.animus.pilum.deactivated";
            public static final String PILUM_NO_LP_THROW = "text.component.animus.pilum.no_lp_throw";
            public static final String PILUM_NO_LP_ATTACK = "text.component.animus.pilum.no_lp_attack";

            // Sanguine Diviner
            public static final String DIVINER_ALTAR_INFO = "text.component.animus.diviner.altar_info";
            public static final String DIVINER_BLOOD_INFO = "text.component.animus.diviner.blood_info";
            public static final String DIVINER_TIER_INFO = "text.component.animus.diviner.tier_info";

            // Temporal Dominance Sigil
            public static final String TEMPORAL_NO_TILE = "text.component.animus.temporal.no_tile";
            public static final String TEMPORAL_DISALLOWED = "text.component.animus.temporal.disallowed";
            public static final String TEMPORAL_GAG_ACTIVE = "text.component.animus.temporal.gag_active";
            public static final String TEMPORAL_NO_LP = "text.component.animus.temporal.no_lp";
            public static final String TEMPORAL_ACTIVATED = "text.component.animus.temporal.activated";
        }

        public static class Tooltips {
            private Tooltips() {
                throw new UnsupportedOperationException("Utility class");
            }

            public static final String DIVINER_FIRST = "tooltip.animus.diviner.first";
            public static final String DIVINER_SECOND = "tooltip.animus.diviner.second";
            public static final String DIVINER_THIRD = "tooltip.animus.diviner.third";
            public static final String HEALING_FLAVOUR = "tooltip.animus.healing.flavour";
            public static final String HEALING_INFO = "tooltip.animus.healing.info";
            public static final String HEALING_RATE = "tooltip.animus.healing.rate";
            public static final String HEALING_PERMANENT = "tooltip.animus.healing.permanent";
            public static final String PILUM_FIRST = "tooltip.animus.pilum_bound.first";
            public static final String PILUM_SECOND = "tooltip.animus.pilum_bound.second";
            public static final String KEY = "tooltip.animus.key";
            public static final String OWNER = "tooltip.animus.currentOwner";
            public static final String SIGIL_BUILDER_FLAVOUR = "tooltip.animus.sigil.builder.flavour";
            public static final String SIGIL_BUILDER_INFO = "tooltip.animus.sigil.builder.info";
            public static final String SIGIL_CHAINS_FLAVOUR = "tooltip.animus.sigil.chains.flavour";
            public static final String SIGIL_CHAINS_INFO = "tooltip.animus.sigil.chains.info";
            public static final String SIGIL_CONSUMPTION_FLAVOUR = "tooltip.animus.sigil.consumption.flavour";
            public static final String SIGIL_CONSUMPTION_INFO = "tooltip.animus.sigil.consumption.info";
            public static final String SIGIL_LEACH_FLAVOUR = "tooltip.animus.sigil.leach.flavour";
            public static final String SIGIL_LEACH_INFO = "tooltip.animus.sigil.leach.info";
            public static final String SIGIL_STORM_FLAVOUR = "tooltip.animus.sigil.storm.flavour";
            public static final String SIGIL_STORM_INFO = "tooltip.animus.sigil.storm.info";
            public static final String SIGIL_HEAVENLY_WRATH_FLAVOUR = "tooltip.animus.sigil.heavenly_wrath.flavour";
            public static final String SIGIL_HEAVENLY_WRATH_INFO = "tooltip.animus.sigil.heavenly_wrath.info";
            public static final String SIGIL_REMEDIUM_FLAVOUR = "tooltip.animus.sigil.remedium.flavour";
            public static final String SIGIL_REMEDIUM_INFO = "tooltip.animus.sigil.remedium.info";
            public static final String SIGIL_REMEDIUM_COST = "tooltip.animus.sigil.remedium.cost";
            public static final String SIGIL_REMEDIUM_ACTIVE = "tooltip.animus.sigil.remedium.active";
            public static final String SIGIL_REMEDIUM_INACTIVE = "tooltip.animus.sigil.remedium.inactive";
            public static final String SIGIL_REPARARE_FLAVOUR = "tooltip.animus.sigil.reparare.flavour";
            public static final String SIGIL_REPARARE_INFO = "tooltip.animus.sigil.reparare.info";
            public static final String SIGIL_REPARARE_COST = "tooltip.animus.sigil.reparare.cost";
            public static final String SIGIL_REPARARE_ACTIVE = "tooltip.animus.sigil.reparare.active";
            public static final String SIGIL_REPARARE_INACTIVE = "tooltip.animus.sigil.reparare.inactive";
            public static final String SIGIL_TRANSPOSITION_FLAVOUR = "tooltip.animus.sigil.transposition.flavour";
            public static final String SIGIL_TRANSPOSITION_INFO = "tooltip.animus.sigil.transposition.info";
            public static final String SIGIL_TRANSPOSITION_STORED = "tooltip.animus.sigil.transposition.stored";
            public static final String BLOOD_APPLE_FLAVOUR = "tooltip.animus.blood_apple.flavour";
            public static final String BLOOD_APPLE_INFO = "tooltip.animus.blood_apple.info";
            public static final String BLOOD_APPLE_LP = "tooltip.animus.blood_apple.lp";
            public static final String BLOOD_SAPLING_FLAVOUR = "tooltip.animus.blood_sapling.flavour";
            public static final String BLOOD_SAPLING_INFO = "tooltip.animus.blood_sapling.info";
            public static final String PILUM_IRON_FLAVOUR = "tooltip.animus.pilum_iron.flavour";
            public static final String PILUM_IRON_INFO = "tooltip.animus.pilum_iron.info";
            public static final String PILUM_DIAMOND_FLAVOUR = "tooltip.animus.pilum_diamond.flavour";
            public static final String PILUM_DIAMOND_INFO = "tooltip.animus.pilum_diamond.info";
            public static final String BLOOD_CORE_FLAVOUR = "tooltip.animus.blood_core.flavour";
            public static final String BLOOD_CORE_INFO = "tooltip.animus.blood_core.info";
            public static final String BLOOD_CORE_MULTIBLOCK = "tooltip.animus.blood_core.multiblock";
            public static final String ACTIVATION_CRYSTAL_FLAVOUR = "tooltip.animus.activation_crystal.flavour";
            public static final String ACTIVATION_CRYSTAL_INFO = "tooltip.animus.activation_crystal.info";
            public static final String ACTIVATION_CRYSTAL_WARNING = "tooltip.animus.activation_crystal.warning";

            // Bound Pilum Tooltips
            public static final String PILUM_BOUND_TO = "tooltip.animus.pilum_bound.bound_to";
            public static final String PILUM_STATUS_ACTIVATED = "tooltip.animus.pilum_bound.status_activated";
            public static final String PILUM_COST = "tooltip.animus.pilum_bound.cost";
            public static final String PILUM_STATUS_DEACTIVATED = "tooltip.animus.pilum_bound.status_deactivated";
            public static final String PILUM_BEHAVES_DIAMOND = "tooltip.animus.pilum_bound.behaves_diamond";
            public static final String PILUM_TOGGLE = "tooltip.animus.pilum_bound.toggle";
            public static final String PILUM_UNBOUND = "tooltip.animus.pilum_bound.unbound";
            public static final String PILUM_BIND = "tooltip.animus.pilum_bound.bind";

            // Sentient Pilum Tooltips
            public static final String PILUM_SENTIENT_FLAVOUR = "tooltip.animus.pilum_sentient.flavour";
            public static final String PILUM_SENTIENT_INFO = "tooltip.animus.pilum_sentient.info";
            public static final String PILUM_SENTIENT_AOE = "tooltip.animus.pilum_sentient.aoe";

            // Key of Binding Tooltips
            public static final String KEY_HOLD_OFFHAND = "tooltip.animus.key.hold_offhand";
            public static final String KEY_UNBOUND = "tooltip.animus.key.unbound";
            public static final String KEY_CURIO = "tooltip.animus.key.curio";

            // Generic Binding Tooltips
            public static final String BOUND_TO = "tooltip.animus.bound_to";
            public static final String NOT_BOUND = "tooltip.animus.not_bound";
            public static final String UNBOUND_BIND = "tooltip.animus.unbound_bind";

            // Crystallized Demon Will Block Tooltips
            public static final String CRYSTALLIZED_DEMON_WILL_FLAVOUR = "tooltip.animus.crystallized_demon_will.flavour";
            public static final String CRYSTALLIZED_DEMON_WILL_INFO = "tooltip.animus.crystallized_demon_will.info";
            public static final String CRYSTALLIZED_DEMON_WILL_ALTAR = "tooltip.animus.crystallized_demon_will.altar";

            // Free Soul Sigil Tooltips
            public static final String SIGIL_FREE_SOUL_FLAVOUR = "tooltip.animus.sigil.free_soul.flavour";
            public static final String SIGIL_FREE_SOUL_INFO = "tooltip.animus.sigil.free_soul.info";
            public static final String SIGIL_FREE_SOUL_COST = "tooltip.animus.sigil.free_soul.cost";
            public static final String SIGIL_FREE_SOUL_DURATION = "tooltip.animus.sigil.free_soul.duration";
            public static final String SIGIL_FREE_SOUL_DEATH = "tooltip.animus.sigil.free_soul.death";
            public static final String SIGIL_FREE_SOUL_COOLDOWN = "tooltip.animus.sigil.free_soul.cooldown";
            public static final String SIGIL_FREE_SOUL_COOLDOWN_REMAINING = "tooltip.animus.sigil.free_soul.cooldown_remaining";

            // Temporal Dominance Sigil Tooltips
            public static final String TEMPORAL_DOMINANCE_1 = "tooltip.animus.sigil.temporal_dominance.1";
            public static final String TEMPORAL_DOMINANCE_2 = "tooltip.animus.sigil.temporal_dominance.2";
            public static final String TEMPORAL_DOMINANCE_3 = "tooltip.animus.sigil.temporal_dominance.3";
            public static final String TEMPORAL_DOMINANCE_4 = "tooltip.animus.sigil.temporal_dominance.4";
            public static final String EQUIVALENCY_1 = "tooltip.animus.sigil.equivalency.1";
            public static final String EQUIVALENCY_2 = "tooltip.animus.sigil.equivalency.2";
            public static final String EQUIVALENCY_SELECTED = "tooltip.animus.sigil.equivalency.selected";
        }
    }

    public static class Misc {
        private Misc() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String CRAFTING_KEYBINDING = "keybinding_crafting";
        public static final String CRAFTING_LIVING_TERRA_BUCKET = "living_terra_bucket_crafting";
        public static final String DAMAGE_ABSOLUTE = "animus.absolute";
        public static final String FLUID_ANTILIFE = "fluid_antilife";
        public static final String FLUID_LIVING_TERRA = "fluid_living_terra";
    }

    public static class Tags {
        private Tags() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> DISALLOW_CAPTURING =
            net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Mod.MODID, "disallow_capturing")
            );

        public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> DISALLOW_CULLING =
            net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Mod.MODID, "disallow_culling")
            );

        public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> DISALLOW_SACRIFICE =
            net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Mod.MODID, "disallow_sacrifice")
            );

        public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> DISALLOW_LEACH =
            net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Mod.MODID, "disallow_leach")
            );

        public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> DISALLOW_ANTILIFE =
            net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Mod.MODID, "disallow_antilife")
            );

        public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> DISALLOW_REPAIR =
            net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Mod.MODID, "disallow_repair")
            );
public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> DISALLOW_ACCELERATION =            net.minecraft.tags.TagKey.create(                net.minecraft.core.registries.Registries.BLOCK,                ResourceLocation.fromNamespaceAndPath(Mod.MODID, "disallow_acceleration")            );
    }

    public static class Resource {
        private Resource() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final ResourceLocation fluidAntiLifeFlowing = ResourceLocation.fromNamespaceAndPath(Mod.MODID, "block/fluid/antilife_flowing");
        public static final ResourceLocation fluidAntiLifeStill = ResourceLocation.fromNamespaceAndPath(Mod.MODID, "block/fluid/antilife_still");
        public static final ResourceLocation fluidLivingTerraFlowing = ResourceLocation.fromNamespaceAndPath(Mod.MODID, "block/fluid/living_terra_flowing");
        public static final ResourceLocation fluidLivingTerraStill = ResourceLocation.fromNamespaceAndPath(Mod.MODID, "block/fluid/living_terra_still");
    }

    public static class Mod {
        private Mod() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String MODID = "animus";
        public static final String DOMAIN = MODID + ":";
        public static final String NAME = "Animus";
        public static final String VERSION = ModList.get()
            .getModContainerById(MODID)
            .map(mc -> mc.getModInfo().getVersion().toString())
            .orElse("UNKNOWN");
    }

    public static class NBT {
        private NBT() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String CULLING_BUFFER_WILL = "willBuffer";
        public static final String SOUL_DATA = "mobdata";
        public static final String SOUL_ENTITY_ID = "id";
        public static final String SOUL_ENTITY_NAME = "entity";
        public static final String SOUL_NAME = "name";
        public static final String TRANSPOSITION_POS = "pos";
    }

    public static class Rituals {
        private Rituals() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String PERSISTENCE = "ritual_persistence";
        public static final String CULLING = "ritual_culling";
        public static final String ENTROPY = "ritual_entropy";
        public static final String HUNGER = "ritual_hunger";
        public static final String LEACH = "ritual_natures_leach";
        public static final String ELDRITCH = "ritual_eldritch_will";
        public static final String LUNA = "ritual_luna";
        public static final String PEACEFUL_BECKONING = "ritual_peaceful_beckoning";
        public static final String REGRESSION = "ritual_regression";
        public static final String REPARARE = "ritual_reparare";
        public static final String SERENITY = "ritual_serenity";
        public static final String NOLITE_IGNEM = "ritual_nolite_ignem";
        public static final String RELENTLESS_TIDES = "ritual_relentless_tides";
        public static final String SIPHON = "ritual_siphon";
        public static final String SOL = "ritual_sol";
        public static final String SOURCE_VITAEUM = "ritual_source_vitaeum";
        public static final String STEADFAST = "ritual_steadfast_heart";
        public static final String UNMAKING = "ritual_unmaking";
    }

    public static class Sigils {
        private Sigils() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String BUILDER = "builder";
        public static final String CHAINS = "chains";
        public static final String CONSUMPTION = "consumption";
        public static final String LEACH = "leach";
        public static final String STORM = "storm";
        public static final String HEAVENLY_WRATH = "heavenly_wrath";
        public static final String REMEDIUM = "remedium";
        public static final String REPARARE = "reparare";
        public static final String TRANSPOSITION = "transposition";
        public static final String FREE_SOUL = "free_soul";
        public static final String TEMPORAL_DOMINANCE = "temporal_dominance";
        public static final String EQUIVALENCY = "equivalency";
        public static final String BOUNDLESS_NATURE = "boundless_nature";
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Mod.MODID, path);
    }
}
