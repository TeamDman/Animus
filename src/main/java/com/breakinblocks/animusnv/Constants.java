package com.breakinblocks.animusnv;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

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
            public static final String DIVINER_MISSING = "text.component.animusnv.diviner.missing";
            public static final String DIVINER_OBSTRUCTED = "text.component.animusnv.diviner.obstructed";
            public static final String TRANSPOSITION_CLEARED = "text.component.animusnv.transposition.cleared";
            public static final String TRANSPOSITION_SET = "text.component.animusnv.transposition.set";
            public static final String TRANSPOSITION_UNMOVABLE = "text.component.animusnv.transposition.unmovable";
            public static final String CHAINS_CAPTURE_FAILED = "text.component.animusnv.chains.capture_failed";
            public static final String HEALING_CANNOT_DROP = "text.component.animusnv.healing.cannot_drop";
            public static final String HEALING_WARNING = "text.component.animusnv.healing.warning";
            public static final String SACRIFICE_TOO_POWERFUL = "text.component.animusnv.sacrifice.too_powerful";
            public static final String ACTIVATION_CRYSTAL_UNBOUND = "text.component.animusnv.activation_crystal.unbound";
            public static final String ACTIVATION_CRYSTAL_SHATTERED = "text.component.animusnv.activation_crystal.shattered";
            public static final String TICKET_PERSISTENCE = "text.component.animusnv.ticket.persistence";
            public static final String TICKET_SOL = "text.component.animusnv.ticket.sol";
            public static final String TICKET_TRANSPOSITION = "text.component.animusnv.ticket.transposition";
            public static final String TICKET_STORM = "text.component.animusnv.ticket.storm";
            public static final String TICKET_ANTILIFE = "text.component.animusnv.ticket.antilife";
            public static final String TICKET_ANIMAL_LURING = "text.component.animusnv.ticket.animal_luring";
            public static final String TICKET_ENTROPY = "text.component.animusnv.ticket.entropy";
            public static final String TICKET_LUNA = "text.component.animusnv.ticket.luna";
            public static final String TICKET_BUILDER = "text.component.animusnv.ticket.builder";
            public static final String TICKET_CULLING = "text.component.animusnv.ticket.culling";
            public static final String TICKET_CONSUMPTION = "text.component.animusnv.ticket.consumption";
            public static final String TICKET_LEACH = "text.component.animusnv.ticket.leach";
            public static final String TICKET_ELDRITCH = "text.component.animusnv.ticket.eldritch";
            public static final String TICKET_UNMAKING = "text.component.animusnv.ticket.unmaking";
            public static final String TICKET_APPLE = "text.component.animusnv.ticket.apple";
            public static final String TICKET_CHAINS = "text.component.animusnv.ticket.chains";
            public static final String TICKET_STEADFAST = "text.component.animusnv.ticket.steadfast";
            public static final String TICKET_HEAVENLY_WRATH = "text.component.animusnv.ticket.heavenly_wrath";
            public static final String TICKET_REMEDIUM = "text.component.animusnv.ticket.remedium";
            public static final String TICKET_REPARARE = "text.component.animusnv.ticket.reparare";
            public static final String TICKET_SERENITY = "text.component.animusnv.ticket.serenity";
            public static final String TICKET_NOLITE_IGNEM = "text.component.animusnv.ticket.nolite_ignem";
            public static final String TICKET_RELENTLESS_TIDES = "text.component.animusnv.ticket.relentless_tides";
            public static final String TICKET_SIPHON = "text.component.animusnv.ticket.siphon";
            public static final String TICKET_SOURCE_VITAEUM = "text.component.animusnv.ticket.source_vitaeum";
            public static final String TICKET_FREE_SOUL = "text.component.animusnv.ticket.free_soul";
            public static final String TICKET_TEMPORAL_DOMINANCE = "text.component.animusnv.ticket.temporal_dominance";
            public static final String TICKET_EQUIVALENCY = "text.component.animusnv.ticket.equivalency";
            public static final String TICKET_FLORAL_SUPREMACY = "text.component.animusnv.ticket.floral_supremacy";
            public static final String EQUIVALENCY_CLEARED = "text.component.animusnv.equivalency.cleared";
            public static final String EQUIVALENCY_ALREADY_SELECTED = "text.component.animusnv.equivalency.already_selected";
            public static final String EQUIVALENCY_MAX_SELECTED = "text.component.animusnv.equivalency.max_selected";
            public static final String EQUIVALENCY_NO_BLOCKS = "text.component.animusnv.equivalency.no_blocks";
            public static final String EQUIVALENCY_ADDED = "text.component.animusnv.equivalency.added";
            public static final String EQUIVALENCY_NO_SELECTION = "text.component.animusnv.equivalency.no_selection";
            public static final String EQUIVALENCY_NO_MATCHES = "text.component.animusnv.equivalency.no_matches";
            public static final String EQUIVALENCY_NO_EV = "text.component.animusnv.equivalency.no_ev";
            public static final String EQUIVALENCY_STARTED = "text.component.animusnv.equivalency.started";
            public static final String EQUIVALENCY_RADIUS = "text.component.animusnv.equivalency.radius";

            public static final String FREE_SOUL_ALREADY_SPECTATOR = "text.component.animusnv.free_soul.already_spectator";
            public static final String FREE_SOUL_NO_EV = "text.component.animusnv.free_soul.no_ev";
            public static final String FREE_SOUL_ACTIVATED = "text.component.animusnv.free_soul.activated";
            public static final String FREE_SOUL_SAVED = "text.component.animusnv.free_soul.saved";
            public static final String FREE_SOUL_ON_COOLDOWN = "text.component.animusnv.free_soul.on_cooldown";
            public static final String FREE_SOUL_RETURNING = "text.component.animusnv.free_soul.returning";
            public static final String FREE_SOUL_EXPIRED = "text.component.animusnv.free_soul.expired";

            public static final String REMEDIUM_ACTIVATED = "text.component.animusnv.remedium.activated";
            public static final String REMEDIUM_DEACTIVATED = "text.component.animusnv.remedium.deactivated";
            public static final String REMEDIUM_NO_EV = "text.component.animusnv.remedium.no_ev";

            public static final String REPARARE_ACTIVATED = "text.component.animusnv.reparare.activated";
            public static final String REPARARE_DEACTIVATED = "text.component.animusnv.reparare.deactivated";
            public static final String REPARARE_NO_EV = "text.component.animusnv.reparare.no_ev";

            public static final String KEY_CANNOT_BIND_KEY = "text.component.animusnv.key.cannot_bind_key";
            public static final String KEY_ITEM_BOUND = "text.component.animusnv.key.item_bound";

            public static final String BLOOD_CORE_SPREADING_ENABLED = "text.component.animusnv.blood_core.spreading_enabled";
            public static final String BLOOD_CORE_SPREADING_DISABLED = "text.component.animusnv.blood_core.spreading_disabled";

            public static final String SPEAR_BOUND_SUCCESS = "text.component.animusnv.spear.bound_success";
            public static final String SPEAR_ACTIVATED = "text.component.animusnv.spear.activated";
            public static final String SPEAR_DEACTIVATED = "text.component.animusnv.spear.deactivated";
            public static final String SPEAR_NO_EV_THROW = "text.component.animusnv.spear.no_ev_throw";
            public static final String SPEAR_NO_EV_ATTACK = "text.component.animusnv.spear.no_ev_attack";

            public static final String DIVINER_ALTAR_INFO = "text.component.animusnv.diviner.altar_info";
            public static final String DIVINER_ESSENCE_INFO = "text.component.animusnv.diviner.essence_info";
            public static final String DIVINER_TIER_INFO = "text.component.animusnv.diviner.tier_info";

            public static final String TEMPORAL_NO_TILE = "text.component.animusnv.temporal.no_tile";
            public static final String TEMPORAL_DISALLOWED = "text.component.animusnv.temporal.disallowed";
            public static final String TEMPORAL_GAG_ACTIVE = "text.component.animusnv.temporal.gag_active";
            public static final String TEMPORAL_NO_EV = "text.component.animusnv.temporal.no_ev";
            public static final String TEMPORAL_ACTIVATED = "text.component.animusnv.temporal.activated";

            public static final String TICKET_MONK = "text.component.animusnv.ticket.monk";
            public static final String TICKET_MONK_EXECUTE = "text.component.animusnv.ticket.monk_execute";
            public static final String MONK_NO_EV = "text.component.animusnv.monk.no_ev";

        }

        public static class Tooltips {
            private Tooltips() {
                throw new UnsupportedOperationException("Utility class");
            }

            public static final String DIVINER_FIRST = "tooltip.animusnv.diviner.first";
            public static final String DIVINER_SECOND = "tooltip.animusnv.diviner.second";
            public static final String DIVINER_THIRD = "tooltip.animusnv.diviner.third";
            public static final String HEALING_FLAVOUR = "tooltip.animusnv.healing.flavour";
            public static final String HEALING_INFO = "tooltip.animusnv.healing.info";
            public static final String HEALING_RATE = "tooltip.animusnv.healing.rate";
            public static final String HEALING_PERMANENT = "tooltip.animusnv.healing.permanent";
            public static final String SPEAR_FIRST = "tooltip.animusnv.spear_bound.first";
            public static final String SPEAR_SECOND = "tooltip.animusnv.spear_bound.second";
            public static final String KEY = "tooltip.animusnv.key";
            public static final String OWNER = "tooltip.animusnv.currentOwner";
            public static final String SIGIL_BUILDER_FLAVOUR = "tooltip.animusnv.sigil.builder.flavour";
            public static final String SIGIL_BUILDER_INFO = "tooltip.animusnv.sigil.builder.info";
            public static final String SIGIL_CHAINS_FLAVOUR = "tooltip.animusnv.sigil.chains.flavour";
            public static final String SIGIL_CHAINS_INFO = "tooltip.animusnv.sigil.chains.info";
            public static final String SIGIL_CONSUMPTION_FLAVOUR = "tooltip.animusnv.sigil.consumption.flavour";
            public static final String SIGIL_CONSUMPTION_INFO = "tooltip.animusnv.sigil.consumption.info";
            public static final String SIGIL_LEACH_FLAVOUR = "tooltip.animusnv.sigil.leach.flavour";
            public static final String SIGIL_LEACH_INFO = "tooltip.animusnv.sigil.leach.info";
            public static final String SIGIL_STORM_FLAVOUR = "tooltip.animusnv.sigil.storm.flavour";
            public static final String SIGIL_STORM_INFO = "tooltip.animusnv.sigil.storm.info";
            public static final String SIGIL_HEAVENLY_WRATH_FLAVOUR = "tooltip.animusnv.sigil.heavenly_wrath.flavour";
            public static final String SIGIL_HEAVENLY_WRATH_INFO = "tooltip.animusnv.sigil.heavenly_wrath.info";
            public static final String SIGIL_REMEDIUM_FLAVOUR = "tooltip.animusnv.sigil.remedium.flavour";
            public static final String SIGIL_REMEDIUM_INFO = "tooltip.animusnv.sigil.remedium.info";
            public static final String SIGIL_REMEDIUM_COST = "tooltip.animusnv.sigil.remedium.cost";
            public static final String SIGIL_REMEDIUM_ACTIVE = "tooltip.animusnv.sigil.remedium.active";
            public static final String SIGIL_REMEDIUM_INACTIVE = "tooltip.animusnv.sigil.remedium.inactive";
            public static final String SIGIL_REPARARE_FLAVOUR = "tooltip.animusnv.sigil.reparare.flavour";
            public static final String SIGIL_REPARARE_INFO = "tooltip.animusnv.sigil.reparare.info";
            public static final String SIGIL_REPARARE_COST = "tooltip.animusnv.sigil.reparare.cost";
            public static final String SIGIL_REPARARE_ACTIVE = "tooltip.animusnv.sigil.reparare.active";
            public static final String SIGIL_REPARARE_INACTIVE = "tooltip.animusnv.sigil.reparare.inactive";
            public static final String SIGIL_TRANSPOSITION_FLAVOUR = "tooltip.animusnv.sigil.transposition.flavour";
            public static final String SIGIL_TRANSPOSITION_INFO = "tooltip.animusnv.sigil.transposition.info";
            public static final String SIGIL_TRANSPOSITION_STORED = "tooltip.animusnv.sigil.transposition.stored";
            public static final String BLOOD_APPLE_FLAVOUR = "tooltip.animusnv.blood_apple.flavour";
            public static final String BLOOD_APPLE_INFO = "tooltip.animusnv.blood_apple.info";
            public static final String BLOOD_APPLE_EV = "tooltip.animusnv.blood_apple.ev";
            public static final String BLOOD_SAPLING_FLAVOUR = "tooltip.animusnv.blood_sapling.flavour";
            public static final String BLOOD_SAPLING_INFO = "tooltip.animusnv.blood_sapling.info";
            public static final String BLOOD_ORB_TRANSCENDENT_FLAVOUR = "tooltip.animusnv.blood_orb_transcendent.flavour";
            public static final String BLOOD_ORB_TRANSCENDENT_INFO = "tooltip.animusnv.blood_orb_transcendent.info";
            public static final String SPEAR_IRON_FLAVOUR = "tooltip.animusnv.spear_iron.flavour";
            public static final String SPEAR_IRON_INFO = "tooltip.animusnv.spear_iron.info";
            public static final String SPEAR_DIAMOND_FLAVOUR = "tooltip.animusnv.spear_diamond.flavour";
            public static final String SPEAR_DIAMOND_INFO = "tooltip.animusnv.spear_diamond.info";
            public static final String BLOOD_CORE_FLAVOUR = "tooltip.animusnv.blood_core.flavour";
            public static final String BLOOD_CORE_INFO = "tooltip.animusnv.blood_core.info";
            public static final String BLOOD_CORE_MULTIBLOCK = "tooltip.animusnv.blood_core.multiblock";
            public static final String ACTIVATION_CRYSTAL_FLAVOUR = "tooltip.animusnv.activation_crystal.flavour";
            public static final String ACTIVATION_CRYSTAL_INFO = "tooltip.animusnv.activation_crystal.info";
            public static final String ACTIVATION_CRYSTAL_WARNING = "tooltip.animusnv.activation_crystal.warning";

            public static final String SPEAR_BOUND_TO = "tooltip.animusnv.spear_bound.bound_to";
            public static final String SPEAR_STATUS_ACTIVATED = "tooltip.animusnv.spear_bound.status_activated";
            public static final String SPEAR_COST = "tooltip.animusnv.spear_bound.cost";
            public static final String SPEAR_STATUS_DEACTIVATED = "tooltip.animusnv.spear_bound.status_deactivated";
            public static final String SPEAR_BEHAVES_DIAMOND = "tooltip.animusnv.spear_bound.behaves_diamond";
            public static final String SPEAR_TOGGLE = "tooltip.animusnv.spear_bound.toggle";
            public static final String SPEAR_UNBOUND = "tooltip.animusnv.spear_bound.unbound";
            public static final String SPEAR_BIND = "tooltip.animusnv.spear_bound.bind";

            public static final String SPEAR_SENTIENT_FLAVOUR = "tooltip.animusnv.spear_sentient.flavour";
            public static final String SPEAR_SENTIENT_INFO = "tooltip.animusnv.spear_sentient.info";
            public static final String SPEAR_SENTIENT_AOE = "tooltip.animusnv.spear_sentient.aoe";

            public static final String SENTIENT_SHIELD_FLAVOUR = "tooltip.animusnv.sentient_shield.flavour";
            public static final String SENTIENT_SHIELD_SPIRITUS_BONUS = "tooltip.animusnv.sentient_shield.spiritus_bonus";

            public static final String KEY_HOLD_OFFHAND = "tooltip.animusnv.key.hold_offhand";
            public static final String KEY_UNBOUND = "tooltip.animusnv.key.unbound";
            public static final String KEY_CURIO = "tooltip.animusnv.key.curio";

            public static final String BOUND_TO = "tooltip.animusnv.bound_to";
            public static final String NOT_BOUND = "tooltip.animusnv.not_bound";
            public static final String UNBOUND_BIND = "tooltip.animusnv.unbound_bind";

            public static final String CRYSTALLIZED_SPIRITUS_FLAVOUR = "tooltip.animusnv.crystallized_spiritus.flavour";
            public static final String CRYSTALLIZED_SPIRITUS_INFO = "tooltip.animusnv.crystallized_spiritus.info";
            public static final String CRYSTALLIZED_SPIRITUS_ALTAR = "tooltip.animusnv.crystallized_spiritus.altar";

            public static final String SIGIL_FREE_SOUL_FLAVOUR = "tooltip.animusnv.sigil.free_soul.flavour";
            public static final String SIGIL_FREE_SOUL_INFO = "tooltip.animusnv.sigil.free_soul.info";
            public static final String SIGIL_FREE_SOUL_COST = "tooltip.animusnv.sigil.free_soul.cost";
            public static final String SIGIL_FREE_SOUL_DURATION = "tooltip.animusnv.sigil.free_soul.duration";
            public static final String SIGIL_FREE_SOUL_DEATH = "tooltip.animusnv.sigil.free_soul.death";
            public static final String SIGIL_FREE_SOUL_COOLDOWN = "tooltip.animusnv.sigil.free_soul.cooldown";
            public static final String SIGIL_FREE_SOUL_COOLDOWN_REMAINING = "tooltip.animusnv.sigil.free_soul.cooldown_remaining";

            public static final String TEMPORAL_DOMINANCE_1 = "tooltip.animusnv.sigil.temporal_dominance.1";
            public static final String TEMPORAL_DOMINANCE_2 = "tooltip.animusnv.sigil.temporal_dominance.2";
            public static final String TEMPORAL_DOMINANCE_3 = "tooltip.animusnv.sigil.temporal_dominance.3";
            public static final String TEMPORAL_DOMINANCE_4 = "tooltip.animusnv.sigil.temporal_dominance.4";
            public static final String EQUIVALENCY_1 = "tooltip.animusnv.sigil.equivalency.1";
            public static final String EQUIVALENCY_2 = "tooltip.animusnv.sigil.equivalency.2";
            public static final String EQUIVALENCY_SELECTED = "tooltip.animusnv.sigil.equivalency.selected";

            public static final String SIGIL_MONK_FLAVOUR = "tooltip.animusnv.sigil.monk.flavour";
            public static final String SIGIL_MONK_INFO = "tooltip.animusnv.sigil.monk.info";
            public static final String SIGIL_MONK_DAMAGE = "tooltip.animusnv.sigil.monk.damage";
            public static final String SIGIL_MONK_MINING = "tooltip.animusnv.sigil.monk.mining";
            public static final String SIGIL_MONK_KILL_BONUS = "tooltip.animusnv.sigil.monk.kill_bonus";
            public static final String SIGIL_MONK_SPIRITUS_SNARE = "tooltip.animusnv.sigil.monk.soul_snare";
            public static final String SIGIL_MONK_SPIRITUS_BONUS = "tooltip.animusnv.sigil.monk.spiritus_bonus";
            public static final String SIGIL_MONK_EXECUTE = "tooltip.animusnv.sigil.monk.execute";
            public static final String SIGIL_MONK_COST = "tooltip.animusnv.sigil.monk.cost";
            public static final String SIGIL_MONK_ACTIVE = "tooltip.animusnv.sigil.monk.active";
            public static final String SIGIL_MONK_INACTIVE = "tooltip.animusnv.sigil.monk.inactive";

            public static final String SENTIENT_BOW_FLAVOUR = "tooltip.animusnv.sentient_bow.flavour";
            public static final String SENTIENT_BOW_INFO = "tooltip.animusnv.sentient_bow.info";
            public static final String SENTIENT_BOW_SPIRITUS_DROPS = "tooltip.animusnv.sentient_bow.spiritus_drops";

            public static final String HELLFORGED_BOW_FLAVOUR = "tooltip.animusnv.hellforged_bow.flavour";
            public static final String HELLFORGED_BOW_INFO = "tooltip.animusnv.hellforged_bow.info";
            public static final String HELLFORGED_BOW_EV_COST = "tooltip.animusnv.hellforged_bow.ev_cost";
            public static final String HELLFORGED_BOW_CHARGE = "tooltip.animusnv.hellforged_bow.charge";
            public static final String HELLFORGED_BOW_EXECUTE = "tooltip.animusnv.hellforged_bow.execute";
        }
    }

    public static class Misc {
        private Misc() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String CRAFTING_KEYBINDING = "keybinding_crafting";
        public static final String CRAFTING_LIVING_TERRA_BUCKET = "living_terra_bucket_crafting";
        public static final String DAMAGE_ABSOLUTE = "animusnv.absolute";
        public static final String FLUID_ANTILIFE = "fluid_antilife";
        public static final String FLUID_LIVING_TERRA = "fluid_living_terra";
    }

    public static class Tags {
        private Tags() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final TagKey<EntityType<?>> DISALLOW_CAPTURING =
            TagKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Mod.MODID, "disallow_capturing")
            );

        public static final TagKey<EntityType<?>> DISALLOW_CULLING =
            TagKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Mod.MODID, "disallow_culling")
            );

        public static final TagKey<EntityType<?>> DISALLOW_SACRIFICE =
            TagKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Mod.MODID, "disallow_sacrifice")
            );

        public static final TagKey<EntityType<?>> DISALLOW_EXECUTE =
            TagKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Mod.MODID, "disallow_execute")
            );

        public static final TagKey<Block> DISALLOW_LEACH =
            TagKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Mod.MODID, "disallow_leach")
            );

        public static final TagKey<Block> DISALLOW_ANTILIFE =
            TagKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Mod.MODID, "disallow_antilife")
            );

        public static final TagKey<Item> DISALLOW_REPAIR =
            TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(Mod.MODID, "disallow_repair")
            );

        public static final TagKey<Item> WILLFUL_STONES =
            TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(Mod.MODID, "willful_stones")
            );

        public static final TagKey<Item> MALUM_SOUL_SHATTER_CAPABLE_WEAPON =
            TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("malum", "soul_shatter_capable_weapon")
            );

        public static final TagKey<Item> MALUM_ENCHANTABLE_REBOUND =
            TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("malum", "enchantable/rebound")
            );

        public static final TagKey<Item> MALUM_ENCHANTABLE_ASCENSION =
            TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("malum", "enchantable/ascension")
            );

        public static final TagKey<Block> DISALLOW_ACCELERATION =
            TagKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Mod.MODID, "disallow_acceleration")
            );
    }

    public static class Resource {
        private Resource() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final Identifier fluidAntiLifeFlowing = Identifier.fromNamespaceAndPath(Mod.MODID, "block/fluid/antilife_flowing");
        public static final Identifier fluidAntiLifeStill = Identifier.fromNamespaceAndPath(Mod.MODID, "block/fluid/antilife_still");
        public static final Identifier fluidLivingTerraFlowing = Identifier.fromNamespaceAndPath(Mod.MODID, "block/fluid/living_terra_flowing");
        public static final Identifier fluidLivingTerraStill = Identifier.fromNamespaceAndPath(Mod.MODID, "block/fluid/living_terra_still");
    }

    public static class Mod {
        private Mod() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String MODID = "animusnv";
        public static final String DOMAIN = MODID + ":";
        public static final String NAME = "AnimusNV";
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
        public static final String FLORAL_SUPREMACY = "ritual_floral_supremacy";
        public static final String HUNGER = "ritual_hunger";
        public static final String LEACH = "ritual_natures_leach";
        public static final String ELDRITCH = "ritual_eldritch_will";
        public static final String LUNA = "ritual_luna";
        public static final String ANIMAL_LURING = "ritual_animal_luring";
        public static final String REGRESSION = "ritual_regression";
        public static final String REPARARE = "ritual_reparare";
        public static final String SERENITY = "ritual_serenity";
        public static final String NOLITE_IGNEM = "ritual_nolite_ignem";
        public static final String RELENTLESS_TIDES = "ritual_relentless_tides";
        public static final String SIPHON = "ritual_siphon";
        public static final String SOL = "ritual_sol";
        public static final String SOURCE_VITAEUM = "ritual_source_vitaeum";
        public static final String ARS_VITAE = "ritual_ars_vitae";
        public static final String STEADFAST = "ritual_steadfast_heart";
        public static final String UNMAKING = "ritual_unmaking";
        public static final String ARCANE_MASTERY = "ritual_arcane_mastery";

        public static final String ENHANCEMENT = "ritual_enhancement";
        public static final String REDUCTION = "ritual_reduction";
        public static final String BOUNDLESS_SKIES = "ritual_boundless_skies";
        public static final String CLEAR_SKIES = "ritual_clear_skies";
        public static final String NEPTUNE_BLESSING = "ritual_neptune_blessing";
        public static final String WARDEN = "ritual_warden";

        public static final String MAGI = "ritual_magi";                         // Ars Nouveau
        public static final String IRON_HEART = "ritual_iron_heart";             // Iron's Spells
        public static final String SOUL_STAINED_BLOOD = "ritual_soul_stained_blood"; // Malum
        public static final String MANASTEEL_SOUL = "ritual_manasteel_soul";     // Botania (future)
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
        public static final String MONK = "monk";
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(Mod.MODID, path);
    }
}
