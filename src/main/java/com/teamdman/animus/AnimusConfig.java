package com.teamdman.animus;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for Animus mod
 * Uses ForgeConfigSpec for 1.20.1
 */
public class AnimusConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // General Configuration
    public static class General {
        public final ForgeConfigSpec.BooleanValue muteDragon;
        public final ForgeConfigSpec.BooleanValue muteWither;
        public final ForgeConfigSpec.BooleanValue canKillBuffedMobs;
        public final ForgeConfigSpec.IntValue bloodPerApple;

        public General(ForgeConfigSpec.Builder builder) {
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

    // Ritual Configuration
    public static class Rituals {
        public final ForgeConfigSpec.BooleanValue killBoss;
        public final ForgeConfigSpec.IntValue bossCost;
        public final ForgeConfigSpec.BooleanValue cullingKillsTnT;
        public final ForgeConfigSpec.BooleanValue cullingDebug;
        public final ForgeConfigSpec.IntValue cullingRange;
        public final ForgeConfigSpec.IntValue cullingVerticalRange;
        public final ForgeConfigSpec.IntValue cullingLpPerKill;
        public final ForgeConfigSpec.IntValue peacefulBeckoningCost;
        public final ForgeConfigSpec.IntValue steadfastHeartRange;
        public final ForgeConfigSpec.IntValue steadfastHeartRefreshTime;
        public final ForgeConfigSpec.IntValue steadfastHeartMaxAmplifier;
        public final ForgeConfigSpec.IntValue naturesLeachRange;
        public final ForgeConfigSpec.IntValue naturesLeachBaseSpeed;
        public final ForgeConfigSpec.IntValue naturesLeachLpPerBlock;
        public final ForgeConfigSpec.IntValue reparareRitualRepairAmount;
        public final ForgeConfigSpec.IntValue reparareRitualInterval;
        public final ForgeConfigSpec.IntValue reparareRitualLPPerDamage;
        public final ForgeConfigSpec.IntValue persistenceChunkRadius;
        public final ForgeConfigSpec.IntValue persistenceLPPerTick;
        public final ForgeConfigSpec.IntValue serenityRadius;
        public final ForgeConfigSpec.IntValue serenityLPPerTick;
        public final ForgeConfigSpec.IntValue noliteIgnemRadius;
        public final ForgeConfigSpec.IntValue noliteIgnemLPPerFire;
        public final ForgeConfigSpec.IntValue relentlessTidesRange;
        public final ForgeConfigSpec.IntValue relentlessTidesDepth;
        public final ForgeConfigSpec.IntValue relentlessTidesLPPerPlacement;
        public final ForgeConfigSpec.IntValue siphonRange;
        public final ForgeConfigSpec.IntValue siphonDepth;
        public final ForgeConfigSpec.IntValue siphonLPPerExtraction;
        public final ForgeConfigSpec.ConfigValue<String> siphonReplacementBlock;
        public final ForgeConfigSpec.IntValue sourceVitaeumAltarRange;
        public final ForgeConfigSpec.IntValue sourceVitaeumBaseConversion;
        public final ForgeConfigSpec.IntValue sourceVitaeumPenaltyRadius;
        public final ForgeConfigSpec.IntValue sourceVitaeumSourcePerCycle;
        public final ForgeConfigSpec.IntValue floralSupremacyRadius;
        public final ForgeConfigSpec.IntValue floralSupremacyLPPerFlower;
        public final ForgeConfigSpec.IntValue lunaHorizontalRange;
        public final ForgeConfigSpec.IntValue lunaVerticalRange;
        public final ForgeConfigSpec.IntValue solHorizontalRange;
        public final ForgeConfigSpec.IntValue solVerticalRange;

        public Rituals(ForgeConfigSpec.Builder builder) {
            builder.push("rituals");

            killBoss = builder
                .comment("Allow Ritual of Culling to kill boss monsters (Wither, Ender Dragon, etc.)")
                .define("killBoss", true);

            bossCost = builder
                .comment("Extra LP cost for killing boss monsters")
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
                .comment("Amount of LP added to the Blood Altar per entity killed by Ritual of Culling")
                .defineInRange("cullingLpPerKill", 200, 1, 10000);

            peacefulBeckoningCost = builder
                .comment("LP cost per mob spawned by Ritual of Peaceful Beckoning")
                .defineInRange("peacefulBeckoningCost", 1000, 1, 100000);

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
                .comment("Base refresh time in ticks for Ritual of Nature's Leach (before demon will modifier)")
                .defineInRange("naturesLeachBaseSpeed", 80, 1, 6000);

            naturesLeachLpPerBlock = builder
                .comment("Amount of LP gained per block consumed by Ritual of Nature's Leach")
                .defineInRange("naturesLeachLpPerBlock", 50, 1, 1000);

            reparareRitualRepairAmount = builder
                .comment("Maximum damage to repair per item with Ritual of Reparare each interval")
                .defineInRange("reparareRitualRepairAmount", 1, 1, 100);

            reparareRitualInterval = builder
                .comment("Ticks between repair attempts for Ritual of Reparare (20 ticks = 1 second)")
                .defineInRange("reparareRitualInterval", 100, 20, 6000);

            reparareRitualLPPerDamage = builder
                .comment("LP cost per damage point repaired by Ritual of Reparare")
                .defineInRange("reparareRitualLPPerDamage", 50, 1, 1000);

            persistenceChunkRadius = builder
                .comment("Chunk radius for Ritual of Persistence chunk loading (in chunks, not blocks)")
                .defineInRange("persistenceChunkRadius", 3, 1, 16);

            persistenceLPPerTick = builder
                .comment("LP cost per tick for Ritual of Persistence (checked every second / 20 ticks)")
                .defineInRange("persistenceLPPerTick", 100, 1, 10000);

            serenityRadius = builder
                .comment("Radius in blocks for Ritual of Serenity spawn prevention")
                .defineInRange("serenityRadius", 48, 1, 256);

            serenityLPPerTick = builder
                .comment("LP cost per tick for Ritual of Serenity (checked every second / 20 ticks)")
                .defineInRange("serenityLPPerTick", 1, 1, 10000);

            noliteIgnemRadius = builder
                .comment("Radius in blocks for Ritual of Nolite Ignem fire extinguishing")
                .defineInRange("noliteIgnemRadius", 64, 1, 256);

            noliteIgnemLPPerFire = builder
                .comment("LP cost per fire block extinguished by Ritual of Nolite Ignem")
                .defineInRange("noliteIgnemLPPerFire", 10, 1, 1000);

            relentlessTidesRange = builder
                .comment("Horizontal radius in blocks for Ritual of Relentless Tides fluid placement")
                .defineInRange("relentlessTidesRange", 32, 1, 64);

            relentlessTidesDepth = builder
                .comment("Maximum vertical depth in blocks for Ritual of Relentless Tides fluid placement")
                .defineInRange("relentlessTidesDepth", 128, 1, 256);

            relentlessTidesLPPerPlacement = builder
                .comment("LP cost per fluid block placed by Ritual of Relentless Tides")
                .defineInRange("relentlessTidesLPPerPlacement", 50, 1, 1000);

            siphonRange = builder
                .comment("Horizontal radius in blocks for Ritual of Siphon fluid extraction")
                .defineInRange("siphonRange", 32, 1, 64);

            siphonDepth = builder
                .comment("Maximum vertical depth in blocks for Ritual of Siphon fluid extraction")
                .defineInRange("siphonDepth", 128, 1, 256);

            siphonLPPerExtraction = builder
                .comment("LP cost per fluid block extracted by Ritual of Siphon")
                .defineInRange("siphonLPPerExtraction", 50, 1, 1000);

            siphonReplacementBlock = builder
                .comment(
                    "Block to place where fluid is extracted by Ritual of Siphon",
                    "Use format: modid:blockname",
                    "Examples: animus:block_antilife, minecraft:stone, minecraft:cobblestone",
                    "Default: animus:block_antilife"
                )
                .define("siphonReplacementBlock", "animus:block_antilife");

            sourceVitaeumAltarRange = builder
                .comment("Radius in blocks to search for Blood Altars for Ritual of Source Vitaeum")
                .defineInRange("sourceVitaeumAltarRange", 8, 1, 32);

            sourceVitaeumBaseConversion = builder
                .comment("Base conversion rate for Ritual of Source Vitaeum (X Source to 1 LP)")
                .defineInRange("sourceVitaeumBaseConversion", 10, 1, 1000);

            sourceVitaeumPenaltyRadius = builder
                .comment("Radius in blocks to check for other Master Ritual Stones (each doubles the conversion cost)")
                .defineInRange("sourceVitaeumPenaltyRadius", 10, 1, 32);

            sourceVitaeumSourcePerCycle = builder
                .comment("Amount of Source to attempt to convert per cycle")
                .defineInRange("sourceVitaeumSourcePerCycle", 100, 10, 10000);

            floralSupremacyRadius = builder
                .comment("Radius in blocks for Ritual of Floral Supremacy effect area")
                .defineInRange("floralSupremacyRadius", 8, 1, 32);

            floralSupremacyLPPerFlower = builder
                .comment("LP cost per flower supercharged by Ritual of Floral Supremacy")
                .defineInRange("floralSupremacyLPPerFlower", 50, 1, 1000);

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

            builder.pop();
        }
    }

    // Sigil Configuration
    public static class Sigils {
        public final ForgeConfigSpec.IntValue antiLifeConsumption;
        public final ForgeConfigSpec.IntValue antiLifeRange;
        public final ForgeConfigSpec.IntValue builderRange;
        public final ForgeConfigSpec.IntValue leachRange;
        public final ForgeConfigSpec.IntValue stormFishLootMin;
        public final ForgeConfigSpec.IntValue stormFishLootMax;
        public final ForgeConfigSpec.IntValue reparareRepairAmount;
        public final ForgeConfigSpec.IntValue reparareInterval;
        public final ForgeConfigSpec.IntValue reparareLPPerDamage;
        public final ForgeConfigSpec.IntValue freeSoulLPCost;
        public final ForgeConfigSpec.IntValue freeSoulDuration;
        public final ForgeConfigSpec.IntValue freeSoulCooldown;
        public final ForgeConfigSpec.IntValue sigilEquivalencyRadius;
        public final ForgeConfigSpec.IntValue sigilEquivalencyLPCost;
        public final ForgeConfigSpec.IntValue sigilEquivalencyBlocksPerTick;

        public Sigils(ForgeConfigSpec.Builder builder) {
            builder.push("sigils");

            antiLifeConsumption = builder
                .comment("LP cost for Sigil of Consumption per block")
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

            reparareLPPerDamage = builder
                .comment("LP cost per damage point repaired by Sigil of Reparare")
                .defineInRange("reparareLPPerDamage", 50, 1, 1000);

            freeSoulLPCost = builder
                .comment("LP cost per use of Sigil of the Free Soul")
                .defineInRange("freeSoulLPCost", 5000, 1, 10000);

            freeSoulDuration = builder
                .comment("Duration in seconds for spectator mode when using Sigil of the Free Soul")
                .defineInRange("freeSoulDuration", 10, 1, 600);

            freeSoulCooldown = builder
                .comment("Cooldown in seconds before Sigil of the Free Soul can prevent death again")
                .defineInRange("freeSoulCooldown", 60, 1, 600);

            sigilEquivalencyRadius = builder
                .comment("Default radius in blocks for Sigil of Equivalency block replacement (min: 1, max: 32)")
                .defineInRange("sigilEquivalencyRadius", 1, 1, 32);

            sigilEquivalencyLPCost = builder
                .comment("LP cost per block replaced by Sigil of Equivalency")
                .defineInRange("sigilEquivalencyLPCost", 1, 1, 10000);

            sigilEquivalencyBlocksPerTick = builder
                .comment("Number of blocks to replace per tick with Sigil of Equivalency (lower = less lag)")
                .defineInRange("sigilEquivalencyBlocksPerTick", 5, 1, 100);
            builder.pop();
        }
    }

    // Hurt Cooldown (iframes) Configuration
    public static class HurtCooldown {
        public final ForgeConfigSpec.EnumValue<Mode> mode;
        public final ForgeConfigSpec.BooleanValue affectBosses;
        public final ForgeConfigSpec.BooleanValue affectPlayers;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> sources;

        public HurtCooldown(ForgeConfigSpec.Builder builder) {
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

    // Blood Core Configuration
    public static class BloodCore {
        public final ForgeConfigSpec.IntValue leafRegrowthSpeed;
        public final ForgeConfigSpec.IntValue treeSpreadRadius;
        public final ForgeConfigSpec.IntValue treeSpreadInterval;
        public final ForgeConfigSpec.BooleanValue debug;

        public BloodCore(ForgeConfigSpec.Builder builder) {
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

    // Ars Nouveau Compatibility Configuration
    public static class ArsNouveau {
        public final ForgeConfigSpec.IntValue arcaneRuneDrainAmount;
        public final ForgeConfigSpec.IntValue arcaneRuneDrainInterval;
        public final ForgeConfigSpec.BooleanValue enableLivingArmorXP;
        public final ForgeConfigSpec.IntValue livingArmorBaseXP;

        public ArsNouveau(ForgeConfigSpec.Builder builder) {
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

            // Living Armor Integration
            builder.comment("Living Armor Integration").push("livingArmor");

            enableLivingArmorXP = builder
                .comment(
                    "Enable Living Armor XP gain from Ars Nouveau spell casting",
                    "Default: true"
                )
                .define("enabled", true);

            livingArmorBaseXP = builder
                .comment(
                    "Base XP granted to Living Armor per glyph cast",
                    "Actual XP = base × number of glyphs in spell",
                    "Default: 5"
                )
                .defineInRange("baseXP", 5, 1, 1000);

            builder.pop();
            builder.pop();
        }
    }

    // Botania Integration Configuration
    public static class Botania {
        public final ForgeConfigSpec.IntValue LPtoManaConversionRate;
        public final ForgeConfigSpec.IntValue willToManaConversionRate;
        public final ForgeConfigSpec.IntValue unleashedNatureManaDrain;

        public Botania(ForgeConfigSpec.Builder builder) {
            builder.push("botania");

            LPtoManaConversionRate = builder
                .comment(
                    "LP to Mana conversion rate",
                    "Used by Sigil of Boundless Nature and other LP-to-mana items",
                    "Default: 2 mana per 1 LP consumed"
                )
                .defineInRange("LPtoManaConversionRate", 2, 1, 100);

            willToManaConversionRate = builder
                .comment(
                    "Demon Will to Mana conversion rate",
                    "Used by Diabolical Fungi and other will-to-mana generating flowers",
                    "Default: 250 mana per 1 demon will consumed"
                )
                .defineInRange("willToManaConversionRate", 250, 1, 1000);

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

    // Irons Spells n Spellbooks Integration Configuration
    public static class IronsSpells {
        // Phase 1: LP Casting
        public final ForgeConfigSpec.BooleanValue enableLPCasting;
        public final ForgeConfigSpec.IntValue lpPerMana;
        public final ForgeConfigSpec.BooleanValue requireBloodOrb;
        public final ForgeConfigSpec.BooleanValue allowHybridCasting;
        public final ForgeConfigSpec.BooleanValue showLPCostInTooltip;

        // Phase 2: Items
        public final ForgeConfigSpec.BooleanValue enableBloodInfusedSpellbook;
        public final ForgeConfigSpec.IntValue bloodSpellbookTier1LP;
        public final ForgeConfigSpec.IntValue bloodSpellbookTier2LP;
        public final ForgeConfigSpec.IntValue bloodSpellbookTier3LP;
        public final ForgeConfigSpec.IntValue bloodSpellbookTier4LP;
        public final ForgeConfigSpec.IntValue bloodSpellbookTier5LP;
        public final ForgeConfigSpec.IntValue bloodSpellbookTier6LP;

        public final ForgeConfigSpec.BooleanValue enableSigilCrimsonWill;
        public final ForgeConfigSpec.IntValue crimsonWillLPPerMana;

        public final ForgeConfigSpec.BooleanValue enableSanguineScrolls;
        public final ForgeConfigSpec.DoubleValue sanguineScrollLPMultiplier;
        public final ForgeConfigSpec.DoubleValue sanguineScrollDurabilityMultiplier;
        public final ForgeConfigSpec.BooleanValue sanguineScrollRequireBloodOrb;

        // Phase 5: Living Armor Integration
        public final ForgeConfigSpec.BooleanValue enableLivingArmorXP;
        public final ForgeConfigSpec.IntValue livingArmorBaseXP;

        public IronsSpells(ForgeConfigSpec.Builder builder) {
            builder.push("ironsSpells");

            // ===== Phase 1: LP Casting =====
            builder.comment("Phase 1: LP-Powered Spell Casting").push("lpCasting");

            enableLPCasting = builder
                .comment(
                    "Enable LP-powered spell casting",
                    "When enabled, spells can consume Life Points instead of mana",
                    "Default: true"
                )
                .define("enableLPCasting", true);

            lpPerMana = builder
                .comment(
                    "LP cost per mana point when casting spells with LP",
                    "Higher values make spell casting more expensive",
                    "Example: 100 means a spell costing 50 mana requires 5,000 LP",
                    "Default: 100 LP per mana"
                )
                .defineInRange("lpPerMana", 100, 1, 1000);

            requireBloodOrb = builder
                .comment(
                    "Require a Blood Orb in inventory or curio slot to use LP for spell casting",
                    "If false, any player with LP in their soul network can use it",
                    "Default: true"
                )
                .define("requireBloodOrb", true);

            allowHybridCasting = builder
                .comment(
                    "Allow spells to consume both mana and LP if player doesn't have enough of either",
                    "Example: If a spell costs 100 mana and player has 60 mana + enough LP,",
                    "it will consume 60 mana + LP equivalent of 40 mana",
                    "Default: true"
                )
                .define("allowHybridCasting", true);

            showLPCostInTooltip = builder
                .comment(
                    "Show LP cost equivalent in spell tooltips when hovering over spells",
                    "Default: true"
                )
                .define("showLPCostInTooltip", true);

            builder.pop();

            // ===== Phase 2: Blood-Infused Spellbook =====
            builder.comment("Phase 2: Blood-Infused Spellbook").push("bloodInfusedSpellbook");

            enableBloodInfusedSpellbook = builder
                .comment(
                    "Enable Blood-Infused Spellbooks",
                    "Default: true"
                )
                .define("enabled", true);

            bloodSpellbookTier1LP = builder
                .comment("LP cost to infuse spellbook to Tier 1 (Weak Blood Orb)")
                .defineInRange("tier1LPCost", 5000, 100, 1000000);

            bloodSpellbookTier2LP = builder
                .comment("LP cost to infuse spellbook to Tier 2 (Apprentice Blood Orb)")
                .defineInRange("tier2LPCost", 10000, 100, 1000000);

            bloodSpellbookTier3LP = builder
                .comment("LP cost to infuse spellbook to Tier 3 (Magician Blood Orb)")
                .defineInRange("tier3LPCost", 25000, 100, 1000000);

            bloodSpellbookTier4LP = builder
                .comment("LP cost to infuse spellbook to Tier 4 (Master Blood Orb)")
                .defineInRange("tier4LPCost", 50000, 100, 1000000);

            bloodSpellbookTier5LP = builder
                .comment("LP cost to infuse spellbook to Tier 5 (Archmage Blood Orb)")
                .defineInRange("tier5LPCost", 100000, 100, 1000000);

            bloodSpellbookTier6LP = builder
                .comment("LP cost to infuse spellbook to Tier 6 (Transcendent Blood Orb)")
                .defineInRange("tier6LPCost", 250000, 100, 1000000);

            builder.pop();

            // ===== Phase 2: Sigil of Crimson Will =====
            builder.comment("Phase 2: Sigil of Crimson Will").push("sigilCrimsonWill");

            enableSigilCrimsonWill = builder
                .comment(
                    "Enable Sigil of Crimson Will",
                    "Default: true"
                )
                .define("enabled", true);

            crimsonWillLPPerMana = builder
                .comment(
                    "LP cost per mana point when empowering spells with Crimson Will",
                    "This is in addition to normal spell costs",
                    "Default: 50 LP per mana (half of normal LP casting cost)"
                )
                .defineInRange("lpPerMana", 50, 1, 1000);

            builder.pop();

            // ===== Phase 2: Sanguine Scrolls =====
            builder.comment("Phase 2: Sanguine Scrolls").push("sanguineScrolls");

            enableSanguineScrolls = builder
                .comment(
                    "Enable Sanguine Scrolls",
                    "Default: true"
                )
                .define("enabled", true);

            sanguineScrollLPMultiplier = builder
                .comment(
                    "LP cost multiplier for Sanguine Scrolls",
                    "Multiplied with spell mana cost and lpPerMana",
                    "Example: 1.5 means spell costs mana × lpPerMana × 1.5 LP",
                    "Default: 1.5 (50% more expensive than spellbook casting)"
                )
                .defineInRange("lpCostMultiplier", 1.5, 1.0, 5.0);

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

            // ===== Phase 5: Living Armor Integration =====
            builder.comment("Phase 5: Living Armor Integration").push("livingArmor");

            enableLivingArmorXP = builder
                .comment(
                    "Enable Living Armor XP gain from spell casting",
                    "Default: true"
                )
                .define("enabled", true);

            livingArmorBaseXP = builder
                .comment(
                    "Base XP granted to Living Armor per spell cast",
                    "Actual XP = base × spell level × rarity multiplier",
                    "Rarity multipliers: Common=1.0, Uncommon=1.5, Rare=2.0, Epic=3.0, Legendary=5.0",
                    "Default: 10"
                )
                .defineInRange("baseXP", 10, 1, 1000);

            builder.pop();
            builder.pop();
        }
    }

    // Config instances
    public static General general;
    public static Rituals rituals;
    public static Sigils sigils;
    public static HurtCooldown hurtCooldown;
    public static BloodCore bloodCore;
    public static ArsNouveau arsNouveau;
    public static Botania botania;
    public static IronsSpells ironsSpells;

    static {
        BUILDER.comment("Animus Configuration").push("animus");

        general = new General(BUILDER);
        rituals = new Rituals(BUILDER);
        sigils = new Sigils(BUILDER);
        hurtCooldown = new HurtCooldown(BUILDER);
        bloodCore = new BloodCore(BUILDER);
        arsNouveau = new ArsNouveau(BUILDER);
        botania = new Botania(BUILDER);
        ironsSpells = new IronsSpells(BUILDER);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
