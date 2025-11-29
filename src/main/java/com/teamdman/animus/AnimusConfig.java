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
                .defineInRange("serenityLPPerTick", 50, 1, 10000);

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

    // Config instances
    public static General general;
    public static Rituals rituals;
    public static Sigils sigils;
    public static HurtCooldown hurtCooldown;
    public static BloodCore bloodCore;

    static {
        BUILDER.comment("Animus Configuration").push("animus");

        general = new General(BUILDER);
        rituals = new Rituals(BUILDER);
        sigils = new Sigils(BUILDER);
        hurtCooldown = new HurtCooldown(BUILDER);
        bloodCore = new BloodCore(BUILDER);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
