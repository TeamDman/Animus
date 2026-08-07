package com.breakinblocks.animusnv;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Note: Config values are NOT available during DeferredRegister callbacks.
 * Ritual constructors use hardcoded defaults, which can be adjusted via
 * the ritual stone GUI at runtime.
 */
public class AnimusConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static class General {
        public final ModConfigSpec.BooleanValue muteDragon;
        public final ModConfigSpec.BooleanValue muteWither;
        public final ModConfigSpec.BooleanValue canKillBuffedMobs;
        public final ModConfigSpec.IntValue bloodPerApple;

        public General(ModConfigSpec.Builder builder) {
            builder.push("general");

            muteDragon = builder
                .comment("Mute the Ender Dragon death sound")
                .define("muteDragon", false);

            muteWither = builder
                .comment("Mute the Wither spawn sound")
                .define("muteWither", false);

            canKillBuffedMobs = builder
                .comment("Allow rituals to kill mobs with potion effects")
                .define("canKillBuffedMobs", true);

            bloodPerApple = builder
                .comment("Amount of blood to add to altar per Blood Apple")
                .defineInRange("bloodPerApple", 50, 1, 10000);

            builder.pop();
        }
    }

    public static class Rituals {
        public final ModConfigSpec.BooleanValue killBoss;
        public final ModConfigSpec.IntValue bossCost;
        public final ModConfigSpec.BooleanValue cullingKillsTnT;
        public final ModConfigSpec.BooleanValue cullingDebug;
        public final ModConfigSpec.IntValue cullingRange;
        public final ModConfigSpec.IntValue cullingVerticalRange;
        public final ModConfigSpec.IntValue cullingLpPerKill;
        public final ModConfigSpec.IntValue animalLuringCost;
        public final ModConfigSpec.IntValue steadfastHeartRange;
        public final ModConfigSpec.IntValue steadfastHeartRefreshTime;
        public final ModConfigSpec.IntValue steadfastHeartMaxAmplifier;
        public final ModConfigSpec.IntValue naturesLeachRange;
        public final ModConfigSpec.IntValue naturesLeachBaseSpeed;
        public final ModConfigSpec.IntValue naturesLeachLpPerBlock;
        public final ModConfigSpec.IntValue reparareRitualRepairAmount;
        public final ModConfigSpec.IntValue reparareRitualInterval;
        public final ModConfigSpec.IntValue reparareRitualEVPerDamage;
        public final ModConfigSpec.IntValue persistenceChunkRadius;
        public final ModConfigSpec.IntValue persistenceEVPerTick;
        public final ModConfigSpec.IntValue serenityRadius;
        public final ModConfigSpec.IntValue serenityEVPerTick;
        public final ModConfigSpec.IntValue noliteIgnemRadius;
        public final ModConfigSpec.IntValue noliteIgnemEVPerFire;
        public final ModConfigSpec.IntValue relentlessTidesRange;
        public final ModConfigSpec.IntValue relentlessTidesDepth;
        public final ModConfigSpec.IntValue relentlessTidesEVPerPlacement;
        public final ModConfigSpec.IntValue siphonRange;
        public final ModConfigSpec.IntValue siphonDepth;
        public final ModConfigSpec.IntValue siphonEVPerExtraction;
        public final ModConfigSpec.ConfigValue<String> siphonReplacementBlock;
        public final ModConfigSpec.IntValue sourceVitaeumAltarRange;
        public final ModConfigSpec.IntValue sourceVitaeumBaseConversion;
        public final ModConfigSpec.IntValue sourceVitaeumPenaltyRadius;
        public final ModConfigSpec.IntValue sourceVitaeumSourcePerCycle;
        public final ModConfigSpec.IntValue arsVitaePenaltyRadius;
        public final ModConfigSpec.IntValue arsVitaeSourcePerCycle;
        public final ModConfigSpec.IntValue floralSupremacyRadius;
        public final ModConfigSpec.IntValue floralSupremacyEVPerFlower;
        public final ModConfigSpec.IntValue lunaHorizontalRange;
        public final ModConfigSpec.IntValue lunaVerticalRange;
        public final ModConfigSpec.IntValue solHorizontalRange;
        public final ModConfigSpec.IntValue solVerticalRange;
        public final ModConfigSpec.BooleanValue unmakingDisallowEnhanced;
        public final ModConfigSpec.BooleanValue cullingPlayerKillDrops;
        public final ModConfigSpec.DoubleValue cullingSpiritusConsumeChance;
        public final ModConfigSpec.IntValue endlessGreedRange;
        public final ModConfigSpec.IntValue endlessGreedVerticalRange;
        public final ModConfigSpec.IntValue endlessGreedEVPerItem;
        public final ModConfigSpec.IntValue endlessGreedRefreshCost;

        public final ModConfigSpec.IntValue cullingRefreshCost;
        public final ModConfigSpec.IntValue entropyRefreshCost;
        public final ModConfigSpec.IntValue lunaRefreshCost;
        public final ModConfigSpec.IntValue naturesLeachRefreshCost;
        public final ModConfigSpec.IntValue solRefreshCost;
        public final ModConfigSpec.IntValue steadfastHeartRefreshCost;
        public final ModConfigSpec.IntValue unmakingRefreshCost;

        public final ModConfigSpec.IntValue animalLuringRefreshTime;
        public final ModConfigSpec.IntValue cullingRefreshTime;
        public final ModConfigSpec.IntValue entropyRefreshTime;
        public final ModConfigSpec.IntValue lunaRefreshTime;
        public final ModConfigSpec.IntValue noliteIgnemRefreshTime;
        public final ModConfigSpec.IntValue persistenceRefreshTime;
        public final ModConfigSpec.IntValue relentlessTidesRefreshTime;
        public final ModConfigSpec.IntValue serenityRefreshTime;
        public final ModConfigSpec.IntValue siphonRefreshTime;
        public final ModConfigSpec.IntValue solRefreshTime;
        public final ModConfigSpec.IntValue sourceVitaeumRefreshTime;
        public final ModConfigSpec.IntValue arsVitaeRefreshTime;
        public final ModConfigSpec.IntValue unmakingRefreshTime;


        public Rituals(ModConfigSpec.Builder builder) {
            builder.push("rituals");

            killBoss = builder
                .comment("Allow Ritual of Culling to kill boss monsters (Wither, Ender Dragon, etc.)")
                .define("killBoss", true);

            bossCost = builder
                .comment("Extra EV cost for killing boss monsters")
                .defineInRange("bossCost", 25000, 0, 1000000);

            cullingKillsTnT = builder
                .comment("Allow Ritual of Culling to destroy primed TNT")
                .define("CullingKillsTnT", true);

            cullingDebug = builder
                .comment("Enable debug logging for Ritual of Culling")
                .define("CullingDebug", false);

            cullingRange = builder
                .comment("Horizontal range in blocks for Ritual of Culling effect area")
                .defineInRange("cullingRange", 10, 1, 64);

            cullingVerticalRange = builder
                .comment("Vertical range in blocks for Ritual of Culling effect area (extends both above AND below the ritual stone)")
                .defineInRange("cullingVerticalRange", 10, 1, 64);

            cullingLpPerKill = builder
                .comment("DEPRECATED - No longer used. EV values are now determined by NeoVitae's entity_sacrifice_value datamap. " +
                         "Customize via datapacks at: data/<namespace>/data_maps/entity_type/entity_sacrifice_value.json")
                .defineInRange("cullingLpPerKill", 200, 1, 10000);

            animalLuringCost = builder
                .comment("EV cost per mob spawned by Ritual of Animal Luring")
                .defineInRange("animalLuringCost", 1000, 1, 100000);

            steadfastHeartRange = builder
                .comment("Range in blocks for Ritual of Steadfast Heart absorption effect")
                .defineInRange("steadfastHeartRange", 128, 1, 512);

            steadfastHeartRefreshTime = builder
                .comment("Refresh time in ticks for Ritual of Steadfast Heart (20 ticks = 1 second)")
                .defineInRange("steadfastHeartRefreshTime", 60, 1, 6000);

            steadfastHeartMaxAmplifier = builder
                .comment("Maximum absorption amplifier for Ritual of Steadfast Heart (0 = 1 heart, 4 = 5 hearts)")
                .defineInRange("steadfastHeartMaxAmplifier", 4, 0, 10);

            naturesLeachRange = builder
                .comment("Range in blocks for Ritual of Nature's Leach to consume plants")
                .defineInRange("naturesLeachRange", 32, 1, 64);

            naturesLeachBaseSpeed = builder
                .comment("Base refresh time in ticks for Ritual of Nature's Leach (before Spiritus modifier)")
                .defineInRange("naturesLeachBaseSpeed", 80, 1, 6000);

            naturesLeachLpPerBlock = builder
                .comment("Amount of EV gained per block consumed by Ritual of Nature's Leach")
                .defineInRange("naturesLeachLpPerBlock", 50, 1, 1000);

            reparareRitualRepairAmount = builder
                .comment("Maximum damage to repair per item with Ritual of Reparare each interval")
                .defineInRange("reparareRitualRepairAmount", 1, 1, 100);

            reparareRitualInterval = builder
                .comment("Ticks between repair attempts for Ritual of Reparare (20 ticks = 1 second)")
                .defineInRange("reparareRitualInterval", 100, 20, 6000);

            reparareRitualEVPerDamage = builder
                .comment("EV cost per damage point repaired by Ritual of Reparare")
                .defineInRange("reparareRitualEVPerDamage", 50, 1, 1000);

            persistenceChunkRadius = builder
                .comment("Chunk radius for Ritual of Persistence chunk loading (in chunks, not blocks)")
                .defineInRange("persistenceChunkRadius", 3, 1, 16);

            persistenceEVPerTick = builder
                .comment("EV cost per tick for Ritual of Persistence (checked every second / 20 ticks)")
                .defineInRange("persistenceEVPerTick", 100, 1, 10000);

            serenityRadius = builder
                .comment("Radius in blocks for Ritual of Serenity spawn prevention")
                .defineInRange("serenityRadius", 48, 1, 256);

            serenityEVPerTick = builder
                .comment("EV cost per tick for Ritual of Serenity (checked every second / 20 ticks)")
                .defineInRange("serenityEVPerTick", 1, 1, 10000);

            noliteIgnemRadius = builder
                .comment("Radius in blocks for Ritual of Nolite Ignem fire extinguishing")
                .defineInRange("noliteIgnemRadius", 64, 1, 256);

            noliteIgnemEVPerFire = builder
                .comment("EV cost per fire block extinguished by Ritual of Nolite Ignem")
                .defineInRange("noliteIgnemEVPerFire", 10, 1, 1000);

            relentlessTidesRange = builder
                .comment("Horizontal radius in blocks for Ritual of Relentless Tides fluid placement")
                .defineInRange("relentlessTidesRange", 32, 1, 64);

            relentlessTidesDepth = builder
                .comment("Maximum vertical depth in blocks for Ritual of Relentless Tides fluid placement")
                .defineInRange("relentlessTidesDepth", 128, 1, 256);

            relentlessTidesEVPerPlacement = builder
                .comment("EV cost per fluid block placed by Ritual of Relentless Tides")
                .defineInRange("relentlessTidesEVPerPlacement", 50, 1, 1000);

            siphonRange = builder
                .comment("Horizontal radius in blocks for Ritual of Siphon fluid extraction")
                .defineInRange("siphonRange", 32, 1, 64);

            siphonDepth = builder
                .comment("Maximum vertical depth in blocks for Ritual of Siphon fluid extraction")
                .defineInRange("siphonDepth", 128, 1, 256);

            siphonEVPerExtraction = builder
                .comment("EV cost per fluid block extracted by Ritual of Siphon")
                .defineInRange("siphonEVPerExtraction", 50, 1, 1000);

            siphonReplacementBlock = builder
                .comment(
                    "Block to place where fluid is extracted by Ritual of Siphon",
                    "Use format: modid:blockname",
                    "Examples: animus:block_antilife, minecraft:stone, minecraft:cobblestone",
                    "Default: animus:block_antilife"
                )
                .define("siphonReplacementBlock", "animusnv:block_antilife");

            sourceVitaeumAltarRange = builder
                .comment("Radius in blocks to search for Ara Vitaes for Ritual of Source Vitaeum")
                .defineInRange("sourceVitaeumAltarRange", 8, 1, 32);

            sourceVitaeumBaseConversion = builder
                .comment(
                    "Base exchange rate shared by both Source/EV conversion rituals",
                    "Ritual of Source Vitaeum converts X Source into 1 EV",
                    "Ritual of Ars Vitae converts X EV into 1 Source",
                    "Both directions pay the same rate, so round-tripping loses value"
                )
                .defineInRange("sourceVitaeumBaseConversion", 10, 1, 1000);

            sourceVitaeumPenaltyRadius = builder
                .comment("Radius in blocks to check for other Master Ritual Stones (each doubles the conversion cost)")
                .defineInRange("sourceVitaeumPenaltyRadius", 10, 1, 32);

            sourceVitaeumSourcePerCycle = builder
                .comment("Amount of Source to attempt to convert per cycle")
                .defineInRange("sourceVitaeumSourcePerCycle", 100, 10, 10000);

            arsVitaePenaltyRadius = builder
                .comment("Radius in blocks to check for other Master Ritual Stones (each doubles the conversion cost)")
                .defineInRange("arsVitaePenaltyRadius", 10, 1, 32);

            arsVitaeSourcePerCycle = builder
                .comment("Amount of Source to attempt to produce per cycle")
                .defineInRange("arsVitaeSourcePerCycle", 100, 10, 10000);

            floralSupremacyRadius = builder
                .comment("Radius in blocks for Ritual of Floral Supremacy effect area")
                .defineInRange("floralSupremacyRadius", 8, 1, 32);

            floralSupremacyEVPerFlower = builder
                .comment("EV cost per flower supercharged by Ritual of Floral Supremacy")
                .defineInRange("floralSupremacyEVPerFlower", 50, 1, 1000);

            lunaHorizontalRange = builder
                .comment("Horizontal radius in blocks for Ritual of Luna light harvesting")
                .defineInRange("lunaHorizontalRange", 32, 1, 64);

            lunaVerticalRange = builder
                .comment(
                    "Vertical depth below ritual stone for Ritual of Luna light harvesting",
                    "Searches from ritual stone downward to this depth",
                    "Set to -1 to search all blocks down to world bottom",
                    "Default: 64"
                )
                .defineInRange("lunaVerticalRange", 64, -1, 256);

            solHorizontalRange = builder
                .comment("Horizontal radius in blocks for Ritual of Sol light placement")
                .defineInRange("solHorizontalRange", 32, 1, 64);

            solVerticalRange = builder
                .comment(
                    "Vertical depth below ritual stone for Ritual of Sol light placement",
                    "Searches from ritual stone downward to this depth",
                    "Set to -1 to search all blocks down to world bottom",
                    "Default: 64"
                )
                .defineInRange("solVerticalRange", 64, -1, 256);

            unmakingDisallowEnhanced = builder
                .comment(
                    "Disallow items enhanced by the Imperfect Ritual of Enhancement from being processed by the Ritual of Unmaking",
                    "When true, items with the AnimusEnhanced tag will be skipped",
                    "Default: true"
                )
                .define("unmakingDisallowEnhanced", true);

            cullingPlayerKillDrops = builder
                .comment(
                    "Enable player-like kills for Ritual of Culling when raw Spiritus is available",
                    "When enabled and Raw Spiritus is present, mobs are killed as if the ritual owner killed them",
                    "This enables player-only drops like blaze rods from blazes",
                    "Default: true"
                )
                .define("cullingPlayerKillDrops", true);

            cullingSpiritusConsumeChance = builder
                .comment(
                    "Chance (0.0 to 1.0) to consume 1 raw Spiritus when killing a mob with player-like kill",
                    "Only applies when cullingPlayerKillDrops is enabled",
                    "Default: 0.1 (10% chance)"
                )
                .defineInRange("cullingWillConsumeChance", 0.1, 0.0, 1.0);

            builder.comment("Ritual of Endless Greed - Collects mob drops into a container").push("endlessGreed");

            endlessGreedRange = builder
                .comment("Horizontal range in blocks for Ritual of Endless Greed effect area")
                .defineInRange("range", 7, 1, 32);

            endlessGreedVerticalRange = builder
                .comment("Vertical range in blocks for Ritual of Endless Greed effect area")
                .defineInRange("verticalRange", 5, 1, 32);

            endlessGreedEVPerItem = builder
                .comment("EV cost per item collected by Ritual of Endless Greed")
                .defineInRange("lpPerItem", 1, 0, 100);

            endlessGreedRefreshCost = builder
                .comment("Base EV cost per refresh cycle (20 ticks) for Ritual of Endless Greed")
                .defineInRange("refreshCost", 5, 0, 1000);

            builder.pop();

            builder.comment(
                "Upkeep for rituals that run on a timer.",
                "Cost is the EV drained once per refresh, not per game tick.",
                "Time is how many game ticks pass between refreshes (20 ticks = 1 second).",
                "Lowering a refresh time makes that ritual act more often, and therefore cost more EV over time."
            ).push("refresh");

            animalLuringRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Animal Luring")
                .defineInRange("animalLuringTime", 400, 1, 12000);

            cullingRefreshCost = builder
                .comment("EV drained from the owner's Anima per entity culled by Ritual of Culling")
                .defineInRange("cullingCost", 75, 0, 100000);

            cullingRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Culling")
                .defineInRange("cullingTime", 25, 1, 12000);

            entropyRefreshCost = builder
                .comment("EV drained per refresh by Ritual of Entropy")
                .defineInRange("entropyCost", 1, 0, 100000);

            entropyRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Entropy")
                .defineInRange("entropyTime", 1, 1, 12000);

            lunaRefreshCost = builder
                .comment("EV drained per refresh by Ritual of Luna")
                .defineInRange("lunaCost", 1, 0, 100000);

            lunaRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Luna")
                .defineInRange("lunaTime", 5, 1, 12000);

            naturesLeachRefreshCost = builder
                .comment("EV drained per refresh by Ritual of Nature's Leach")
                .defineInRange("naturesLeachCost", 10, 0, 100000);

            noliteIgnemRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Nolite Ignem")
                .defineInRange("noliteIgnemTime", 20, 1, 12000);

            persistenceRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Persistence")
                .defineInRange("persistenceTime", 20, 1, 12000);

            relentlessTidesRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Relentless Tides")
                .defineInRange("relentlessTidesTime", 10, 1, 12000);

            serenityRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Serenity")
                .defineInRange("serenityTime", 20, 1, 12000);

            siphonRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Siphon")
                .defineInRange("siphonTime", 10, 1, 12000);

            solRefreshCost = builder
                .comment("EV drained per refresh by Ritual of Sol")
                .defineInRange("solCost", 1, 0, 100000);

            solRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Sol")
                .defineInRange("solTime", 5, 1, 12000);

            sourceVitaeumRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Source Vitaeum")
                .defineInRange("sourceVitaeumTime", 40, 1, 12000);

            arsVitaeRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Ars Vitae")
                .defineInRange("arsVitaeTime", 40, 1, 12000);

            steadfastHeartRefreshCost = builder
                .comment("EV drained per affected player by Ritual of the Steadfast Heart")
                .defineInRange("steadfastHeartCost", 100, 0, 100000);

            unmakingRefreshCost = builder
                .comment("EV drained per refresh by Ritual of Unmaking")
                .defineInRange("unmakingCost", 0, 0, 100000);

            unmakingRefreshTime = builder
                .comment("Ticks between refreshes for Ritual of Unmaking")
                .defineInRange("unmakingTime", 20, 1, 12000);

            builder.pop();

            builder.pop();
        }
    }

    public static class Sigils {
        public final ModConfigSpec.IntValue antiLifeConsumption;
        public final ModConfigSpec.IntValue antiLifeRange;
        public final ModConfigSpec.IntValue builderRange;
        public final ModConfigSpec.IntValue leachRange;
        public final ModConfigSpec.IntValue stormFishLootMin;
        public final ModConfigSpec.IntValue stormFishLootMax;
        public final ModConfigSpec.IntValue reparareRepairAmount;
        public final ModConfigSpec.IntValue reparareInterval;
        public final ModConfigSpec.IntValue reparareEVPerDamage;
        public final ModConfigSpec.IntValue freeSoulEVCost;
        public final ModConfigSpec.IntValue freeSoulDuration;
        public final ModConfigSpec.IntValue freeSoulCooldown;
        public final ModConfigSpec.IntValue sigilEquivalencyRadius;
        public final ModConfigSpec.IntValue sigilEquivalencyEVCost;
        public final ModConfigSpec.IntValue sigilEquivalencyBlocksPerTick;
        public final ModConfigSpec.DoubleValue monkUnarmedDamage;
        public final ModConfigSpec.IntValue monkEVPerSecond;

        public Sigils(ModConfigSpec.Builder builder) {
            builder.push("sigils");

            antiLifeConsumption = builder
                .comment("EV cost for Sigil of Consumption per block")
                .defineInRange("antiLifeConsumption", 25, 1, 10000);

            antiLifeRange = builder
                .comment("Range of Sigil of Consumption in blocks")
                .defineInRange("antiLifeRange", 8, 1, 64);

            builderRange = builder
                .comment("Range of Sigil of Builder in blocks")
                .defineInRange("builderRange", 64, 1, 256);

            leachRange = builder
                .comment("Range of Sigil of Nature's Leach for consuming blocks in the world (in blocks)")
                .defineInRange("leachRange", 8, 1, 64);

            stormFishLootMin = builder
                .comment(
                    "Minimum number of fishing loot rolls when Sigil of Storm targets water",
                    "Set both min and max to 0 to disable fish spawning"
                )
                .defineInRange("stormFishLootMin", 2, 0, 64);

            stormFishLootMax = builder
                .comment(
                    "Maximum number of fishing loot rolls when Sigil of Storm targets water",
                    "Set both min and max to 0 to disable fish spawning"
                )
                .defineInRange("stormFishLootMax", 5, 0, 64);

            reparareRepairAmount = builder
                .comment("Maximum damage to repair per item with Sigil of Reparare each interval")
                .defineInRange("reparareRepairAmount", 10, 1, 100);

            reparareInterval = builder
                .comment("Ticks between repair attempts for Sigil of Reparare (20 ticks = 1 second)")
                .defineInRange("reparareInterval", 100, 20, 6000);

            reparareEVPerDamage = builder
                .comment("EV cost per damage point repaired by Sigil of Reparare")
                .defineInRange("reparareEVPerDamage", 50, 1, 1000);

            freeSoulEVCost = builder
                .comment("EV cost per use of Sigil of the Free Soul")
                .defineInRange("freeSoulEVCost", 5000, 1, 10000);

            freeSoulDuration = builder
                .comment("Duration in seconds for spectator mode when using Sigil of the Free Soul")
                .defineInRange("freeSoulDuration", 10, 1, 600);

            freeSoulCooldown = builder
                .comment("Cooldown in seconds before Sigil of the Free Soul can prevent death again")
                .defineInRange("freeSoulCooldown", 60, 1, 600);

            sigilEquivalencyRadius = builder
                .comment("Default radius in blocks for Sigil of Equivalency block replacement (min: 1, max: 32)")
                .defineInRange("sigilEquivalencyRadius", 1, 1, 32);

            sigilEquivalencyEVCost = builder
                .comment("EV cost per block replaced by Sigil of Equivalency")
                .defineInRange("sigilEquivalencyEVCost", 1, 1, 10000);

            sigilEquivalencyBlocksPerTick = builder
                .comment("Number of blocks to replace per tick with Sigil of Equivalency (lower = less lag)")
                .defineInRange("sigilEquivalencyBlocksPerTick", 5, 1, 100);

            builder.comment("Sigil of the Monk - unarmed combat enhancement").push("monk");

            monkUnarmedDamage = builder
                .comment(
                    "Bonus unarmed damage when Sigil of the Monk is active",
                    "This damage is added when attacking with empty hands",
                    "Default: 10"
                )
                .defineInRange("unarmedDamage", 10.0, 0.0, 100.0);

            monkEVPerSecond = builder
                .comment(
                    "EV cost per second while Sigil of the Monk is active",
                    "Default: 100 (5 EV per tick × 20 ticks)",
                    "Set to 0 to disable EV drain"
                )
                .defineInRange("lpPerSecond", 100, 0, 10000);

            builder.pop();
            builder.pop();
        }
    }

    public static class HurtCooldown {
        public final ModConfigSpec.EnumValue<Mode> mode;
        public final ModConfigSpec.BooleanValue affectBosses;
        public final ModConfigSpec.BooleanValue affectPlayers;
        public final ModConfigSpec.ConfigValue<List<? extends String>> sources;

        public HurtCooldown(ModConfigSpec.Builder builder) {
            builder.push("hurtCooldown");

            mode = builder
                .comment(
                    "How will the Hurt Cooldown (iframes) be affected",
                    "DISABLED: No changes to vanilla behavior",
                    "WHITELIST: Only listed damage sources have no iframes",
                    "BLACKLIST: All except listed damage sources have no iframes"
                )
                .defineEnum("mode", Mode.BLACKLIST);

            affectBosses = builder
                .comment("If true, bosses will have no iframes")
                .define("affectBosses", false);

            affectPlayers = builder
                .comment("If true, players will have no iframes")
                .define("affectPlayers", false);

            sources = builder
                .comment("List of damage source types for whitelist/blacklist")
                .defineList(
                    "sources",
                    Arrays.asList(
                        "inFire",
                        "inWall",
                        "cactus",
                        "lightningBolt",
                        "lava",
                        "outOfWorld"
                    ),
                    obj -> obj instanceof String
                );

            builder.pop();
        }
    }

    public enum Mode {
        DISABLED,
        WHITELIST,
        BLACKLIST
    }

    public static class BloodCore {
        public final ModConfigSpec.IntValue leafRegrowthSpeed;
        public final ModConfigSpec.IntValue treeSpreadRadius;
        public final ModConfigSpec.IntValue treeSpreadInterval;
        public final ModConfigSpec.BooleanValue debug;

        public BloodCore(ModConfigSpec.Builder builder) {
            builder.push("bloodCore");

            debug = builder
                .comment("Enable debug logging for Blood Core tree spreading")
                .define("debug", false);

            leafRegrowthSpeed = builder
                .comment(
                    "Ticks between leaf regrowth attempts (when spreading is enabled)",
                    "Lower = faster regrowth. Default: 100 (5 seconds)"
                )
                .defineInRange("leafRegrowthSpeed", 100, 20, 6000);

            treeSpreadRadius = builder
                .comment("Radius in blocks for blood tree spreading")
                .defineInRange("treeSpreadRadius", 16, 1, 32);

            treeSpreadInterval = builder
                .comment(
                    "Base interval in ticks between tree spreading attempts",
                    "Default: 600 (30 seconds)"
                )
                .defineInRange("treeSpreadInterval", 600, 200, 12000);

            builder.pop();
        }
    }

    public static class ArsNouveau {
        public final ModConfigSpec.IntValue arcaneRuneDrainAmount;
        public final ModConfigSpec.IntValue arcaneRuneDrainInterval;
        public final ModConfigSpec.BooleanValue enableSentientArmorXP;
        public final ModConfigSpec.IntValue sentientArmorBaseXP;

        public ArsNouveau(ModConfigSpec.Builder builder) {
            builder.push("arsNouveau");

            arcaneRuneDrainAmount = builder
                .comment(
                    "Amount of Source consumed by Arcane Rune per drain cycle",
                    "Default: 20 Source per cycle"
                )
                .defineInRange("arcaneRuneDrainAmount", 20, 1, 10000);

            arcaneRuneDrainInterval = builder
                .comment(
                    "Interval in ticks between Arcane Rune source drain cycles",
                    "Default: 200 ticks (10 seconds)"
                )
                .defineInRange("arcaneRuneDrainInterval", 200, 20, 6000);

            builder.comment("Sentient Armor Integration").push("sentientArmor");

            enableSentientArmorXP = builder
                .comment(
                    "Enable Sentient Armor XP gain from Ars Nouveau spell casting",
                    "Default: true"
                )
                .define("enabled", true);

            sentientArmorBaseXP = builder
                .comment(
                    "Base XP granted to Sentient Armor per glyph cast",
                    "Actual XP = base × number of glyphs in spell",
                    "Default: 5"
                )
                .defineInRange("baseXP", 5, 1, 1000);

            builder.pop();
            builder.pop();
        }
    }

    public static class Botania {
        public final ModConfigSpec.IntValue EVtoManaConversionRate;
        public final ModConfigSpec.IntValue spiritusToManaConversionRate;
        public final ModConfigSpec.IntValue unleashedNatureManaDrain;

        public Botania(ModConfigSpec.Builder builder) {
            builder.push("botania");

            EVtoManaConversionRate = builder
                .comment(
                    "EV to Mana conversion rate",
                    "Used by Sigil of Boundless Nature and other EV-to-mana items",
                    "Default: 2 mana per 1 EV consumed"
                )
                .defineInRange("evToManaConversionRate", 2, 1, 100);

            spiritusToManaConversionRate = builder
                .comment(
                    "Spiritus to Mana conversion rate",
                    "Used by Diabolical Fungi and other will-to-mana generating flowers",
                    "Default: 250 mana per 1 Spiritus consumed"
                )
                .defineInRange("spiritusToManaConversionRate", 250, 1, 1000);

            unleashedNatureManaDrain = builder
                .comment(
                    "Mana consumption rate for Rune of Unleashed Nature",
                    "Mana consumed per second to maintain acceleration bonus",
                    "Default: 10 mana per second"
                )
                .defineInRange("unleashedNatureManaDrain", 10, 1, 1000);

            builder.pop();
        }
    }

    public static class Weapons {
        public final ModConfigSpec.DoubleValue sentientBowSpiritusCost;
        public final ModConfigSpec.IntValue hellforgedBowBaseLpCost;
        public final ModConfigSpec.IntValue hellforgedBowLpPerTick;
        public final ModConfigSpec.IntValue hellforgedBowMaxChargeTicks;
        public final ModConfigSpec.DoubleValue hellforgedBowMaxDamage;
        public final ModConfigSpec.DoubleValue hellforgedBowExecuteThreshold;

        public Weapons(ModConfigSpec.Builder builder) {
            builder.comment("Weapon Settings").push("weapons");

            builder.comment("Sentient Bow - Spiritus powered bow").push("sentientBow");

            sentientBowSpiritusCost = builder
                .comment(
                    "Amount of Spiritus consumed per arrow fired",
                    "Default: 1.0"
                )
                .defineInRange("willCostPerShot", 1.0, 0.0, 100.0);

            builder.pop();

            builder.comment("Hellforged Bow - EV powered bow with charged shots").push("hellforgedBow");

            hellforgedBowBaseLpCost = builder
                .comment(
                    "Base EV cost per shot (before charging)",
                    "Default: 5"
                )
                .defineInRange("baseLpCost", 5, 0, 10000);

            hellforgedBowLpPerTick = builder
                .comment(
                    "EV consumed per tick while charging beyond normal draw",
                    "At max charge (70 ticks), this totals 3500 EV for the charge alone",
                    "Default: 50"
                )
                .defineInRange("lpPerChargeTick", 50, 0, 1000);

            hellforgedBowMaxChargeTicks = builder
                .comment(
                    "Maximum charge time in ticks (20 ticks = 1 second)",
                    "Default: 70 (3.5 seconds)"
                )
                .defineInRange("maxChargeTicks", 70, 20, 200);

            hellforgedBowMaxDamage = builder
                .comment(
                    "Maximum damage at full charge",
                    "Default: 40.0"
                )
                .defineInRange("maxDamage", 40.0, 1.0, 200.0);

            hellforgedBowExecuteThreshold = builder
                .comment(
                    "HP percentage threshold for execute effect (0.15 = 15%)",
                    "At full charge, targets below this HP are instantly killed",
                    "Set to 0 to disable execute",
                    "Default: 0.15"
                )
                .defineInRange("executeThreshold", 0.15, 0.0, 1.0);

            builder.pop();
            builder.pop();
        }
    }

    public static class IronsSpells {
        public final ModConfigSpec.BooleanValue enableEVCasting;
        public final ModConfigSpec.IntValue evPerMana;
        public final ModConfigSpec.BooleanValue requireBloodOrb;
        public final ModConfigSpec.BooleanValue allowHybridCasting;
        public final ModConfigSpec.BooleanValue showEVCostInTooltip;

        public final ModConfigSpec.BooleanValue enableBloodInfusedSpellbook;
        public final ModConfigSpec.IntValue bloodSpellbookTier1EV;
        public final ModConfigSpec.IntValue bloodSpellbookTier2EV;
        public final ModConfigSpec.IntValue bloodSpellbookTier3EV;
        public final ModConfigSpec.IntValue bloodSpellbookTier4EV;
        public final ModConfigSpec.IntValue bloodSpellbookTier5EV;
        public final ModConfigSpec.IntValue bloodSpellbookTier6EV;

        public final ModConfigSpec.BooleanValue enableSigilCrimsonWill;
        public final ModConfigSpec.IntValue crimsonWillEVPerMana;

        public final ModConfigSpec.BooleanValue enableSanguineScrolls;
        public final ModConfigSpec.DoubleValue sanguineScrollEVMultiplier;
        public final ModConfigSpec.DoubleValue sanguineScrollDurabilityMultiplier;
        public final ModConfigSpec.BooleanValue sanguineScrollRequireBloodOrb;

        public final ModConfigSpec.BooleanValue enableSentientArmorXP;
        public final ModConfigSpec.IntValue sentientArmorBaseXP;

        public IronsSpells(ModConfigSpec.Builder builder) {
            builder.push("ironsSpells");

            builder.comment("EV-Powered Spell Casting").push("evCasting");

            enableEVCasting = builder
                .comment(
                    "Enable EV-powered spell casting",
                    "When enabled, spells can consume EV instead of mana",
                    "Default: true"
                )
                .define("enableEVCasting", true);

            evPerMana = builder
                .comment(
                    "EV cost per mana point when casting spells with EV",
                    "Higher values make spell casting more expensive",
                    "Example: 100 means a spell costing 50 mana requires 5,000 EV",
                    "Default: 100 EV per mana"
                )
                .defineInRange("evPerMana", 100, 1, 1000);

            requireBloodOrb = builder
                .comment(
                    "Require a Blood Orb in inventory or curio slot to use EV for spell casting",
                    "If false, any player with EV in their Anima can use it",
                    "Default: true"
                )
                .define("requireBloodOrb", true);

            allowHybridCasting = builder
                .comment(
                    "Allow spells to consume both mana and EV if player doesn't have enough of either",
                    "Example: If a spell costs 100 mana and player has 60 mana + enough EV,",
                    "it will consume 60 mana + EV equivalent of 40 mana",
                    "Default: true"
                )
                .define("allowHybridCasting", true);

            showEVCostInTooltip = builder
                .comment(
                    "Show EV cost equivalent in spell tooltips when hovering over spells",
                    "Default: true"
                )
                .define("showEVCostInTooltip", true);

            builder.pop();

            builder.comment("Blood-Infused Spellbook").push("bloodInfusedSpellbook");

            enableBloodInfusedSpellbook = builder
                .comment(
                    "Enable Blood-Infused Spellbooks",
                    "Default: true"
                )
                .define("enabled", true);

            bloodSpellbookTier1EV = builder
                .comment("EV cost to infuse spellbook to Tier 1 (Weak Blood Orb)")
                .defineInRange("tier1EVCost", 5000, 100, 1000000);

            bloodSpellbookTier2EV = builder
                .comment("EV cost to infuse spellbook to Tier 2 (Apprentice Blood Orb)")
                .defineInRange("tier2EVCost", 10000, 100, 1000000);

            bloodSpellbookTier3EV = builder
                .comment("EV cost to infuse spellbook to Tier 3 (Magician Blood Orb)")
                .defineInRange("tier3EVCost", 25000, 100, 1000000);

            bloodSpellbookTier4EV = builder
                .comment("EV cost to infuse spellbook to Tier 4 (Master Blood Orb)")
                .defineInRange("tier4EVCost", 50000, 100, 1000000);

            bloodSpellbookTier5EV = builder
                .comment("EV cost to infuse spellbook to Tier 5 (Archmage Blood Orb)")
                .defineInRange("tier5EVCost", 100000, 100, 1000000);

            bloodSpellbookTier6EV = builder
                .comment("EV cost to infuse spellbook to Tier 6 (Transcendent Blood Orb)")
                .defineInRange("tier6EVCost", 175000, 100, 1000000);

            builder.pop();

            builder.comment("Sigil of Crimson Will").push("sigilCrimsonWill");

            enableSigilCrimsonWill = builder
                .comment(
                    "Enable Sigil of Crimson Will",
                    "Default: true"
                )
                .define("enabled", true);

            crimsonWillEVPerMana = builder
                .comment(
                    "EV cost per mana point when empowering spells with Crimson Will",
                    "This is in addition to normal spell costs",
                    "Default: 50 EV per mana (half of normal EV casting cost)"
                )
                .defineInRange("evPerMana", 50, 1, 1000);

            builder.pop();

            builder.comment("Sanguine Scrolls").push("sanguineScrolls");

            enableSanguineScrolls = builder
                .comment(
                    "Enable Sanguine Scrolls",
                    "Default: true"
                )
                .define("enabled", true);

            sanguineScrollEVMultiplier = builder
                .comment(
                    "EV cost multiplier for Sanguine Scrolls",
                    "Multiplied with spell mana cost and evPerMana",
                    "Example: 1.5 means spell costs mana × evPerMana × 1.5 EV",
                    "Default: 1.5 (50% more expensive than spellbook casting)"
                )
                .defineInRange("evCostMultiplier", 1.5, 1.0, 5.0);

            sanguineScrollDurabilityMultiplier = builder
                .comment(
                    "Durability multiplier for Sanguine Scrolls",
                    "Base durabilities: Blank=50, Reinforced=100, Imbued=200, Demon=400, Ethereal=600",
                    "Example: 2.0 doubles all durability values",
                    "Default: 1.0"
                )
                .defineInRange("durabilityMultiplier", 1.0, 0.1, 10.0);

            sanguineScrollRequireBloodOrb = builder
                .comment(
                    "Require Blood Orb in inventory to craft Sanguine Scrolls at altar",
                    "Default: false"
                )
                .define("requireBloodOrbToCraft", false);

            builder.pop();

            builder.comment("Sentient Armor Integration").push("sentientArmor");

            enableSentientArmorXP = builder
                .comment(
                    "Enable Sentient Armor XP gain from spell casting",
                    "Default: true"
                )
                .define("enabled", true);

            sentientArmorBaseXP = builder
                .comment(
                    "Base XP granted to Sentient Armor per spell cast",
                    "Actual XP = base × spell level × rarity multiplier",
                    "Rarity multipliers: Common=1.0, Uncommon=1.5, Rare=2.0, Epic=3.0, Legendary=5.0",
                    "Default: 10"
                )
                .defineInRange("baseXP", 10, 1, 1000);

            builder.pop();
            builder.pop();
        }
    }

    public static class SanguineRectifier {
        public final ModConfigSpec.IntValue evPerBlood;
        public final ModConfigSpec.IntValue baseTransferRate;
        public final ModConfigSpec.IntValue tankCapacity;
        public final ModConfigSpec.IntValue searchRange;

        public SanguineRectifier(ModConfigSpec.Builder builder) {
            builder.comment("Sanguine Rectifier (EvilCraft Compat)").push("sanguineRectifier");

            evPerBlood = builder
                .comment("EV consumed per 1mB of EvilCraft blood produced. Set to 0 to disable EV-to-Blood conversion.")
                .defineInRange("evPerBlood", 1, 0, 100);

            baseTransferRate = builder
                .comment("Base transfer rate in mB/tick (scales with altar speed runes)")
                .defineInRange("baseTransferRate", 100, 1, 10000);

            tankCapacity = builder
                .comment("Internal blood tank capacity in mB")
                .defineInRange("tankCapacity", 16000, 1000, 256000);

            searchRange = builder
                .comment("Altar search range in blocks (cube radius)")
                .defineInRange("searchRange", 10, 3, 32);

            builder.pop();
        }
    }

    public static General general;
    public static Rituals rituals;
    public static Sigils sigils;
    public static HurtCooldown hurtCooldown;
    public static BloodCore bloodCore;
    public static ArsNouveau arsNouveau;
    public static Botania botania;
    public static Weapons weapons;
    public static IronsSpells ironsSpells;
    public static SanguineRectifier sanguineRectifier;

    static {
        BUILDER.comment("Animus Configuration").push("animusnv");

        general = new General(BUILDER);
        rituals = new Rituals(BUILDER);
        sigils = new Sigils(BUILDER);
        hurtCooldown = new HurtCooldown(BUILDER);
        bloodCore = new BloodCore(BUILDER);
        arsNouveau = new ArsNouveau(BUILDER);
        botania = new Botania(BUILDER);
        weapons = new Weapons(BUILDER);
        ironsSpells = new IronsSpells(BUILDER);
        sanguineRectifier = new SanguineRectifier(BUILDER);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
