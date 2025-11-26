package com.teamdman.animus.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Animus Configuration System
 * Uses Forge's modern ForgeConfigSpec for 1.20.1+
 */
public class AnimusConfig {

    // Config Specs
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    // General Settings
    public static class Common {
        public final General general;
        public final Rituals rituals;
        public final Sigils sigils;
        public final HurtCooldown hurtCooldown;
        public final BloodCore bloodCore;

        Common(ForgeConfigSpec.Builder builder) {
            general = new General(builder);
            rituals = new Rituals(builder);
            sigils = new Sigils(builder);
            hurtCooldown = new HurtCooldown(builder);
            bloodCore = new BloodCore(builder);
        }
    }

    public static class General {
        public final ForgeConfigSpec.BooleanValue muteDragon;
        public final ForgeConfigSpec.BooleanValue muteWither;
        public final ForgeConfigSpec.BooleanValue canKillBuffedMobs;
        public final ForgeConfigSpec.IntValue bloodPerApple;

        General(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            muteDragon = builder
                .comment("Mute the Ender Dragon death sound")
                .define("muteDragon", false);

            muteWither = builder
                .comment("Mute the Wither death sound")
                .define("muteWither", false);

            canKillBuffedMobs = builder
                .comment("Allow killing mobs with certain buffs")
                .define("canKillBuffedMobs", false);

            bloodPerApple = builder
                .comment("Amount of LP gained from consuming a Blood Apple")
                .defineInRange("bloodPerApple", 50, 1, 10000);

            builder.pop();
        }
    }

    public static class Rituals {
        public final ForgeConfigSpec.BooleanValue killWither;
        public final ForgeConfigSpec.IntValue witherCost;
        public final ForgeConfigSpec.BooleanValue cullingKillsTnt;
        public final ForgeConfigSpec.IntValue fluxToWillConversionMultiplier;
        public final ForgeConfigSpec.IntValue willRadius;
        public final ForgeConfigSpec.IntValue fluxDrainMax;
        public final ForgeConfigSpec.IntValue eldritchWillSpeed;
        public final ForgeConfigSpec.IntValue eldritchWillCost;
        public final ForgeConfigSpec.BooleanValue cullingDebug;
        public final ForgeConfigSpec.IntValue peaceCost;

        Rituals(ForgeConfigSpec.Builder builder) {
            builder.push("rituals");

            killWither = builder
                .comment("Enable Wither killing in rituals")
                .define("killWither", true);

            witherCost = builder
                .comment("LP cost for Wither-related rituals")
                .defineInRange("witherCost", 25000, 0, 1000000);

            cullingKillsTnt = builder
                .comment("Will the Ritual of Culling destroy primed TNT")
                .define("cullingKillsTnt", true);

            fluxToWillConversionMultiplier = builder
                .comment("How much should each point of flux be multiplied by when converting to demon will (0 for no will generation)")
                .defineInRange("fluxToWillConversionMultiplier", 1, 0, 100);

            willRadius = builder
                .comment("Eldritch Will ritual radius in chunks (0 for single chunk, 1 for 3x3 chunk area)")
                .defineInRange("willRadius", 0, 0, 10);

            fluxDrainMax = builder
                .comment("Maximum amount of flux drained per update for Eldritch Will")
                .defineInRange("fluxDrainMax", 10, 1, 1000);

            eldritchWillSpeed = builder
                .comment("Eldritch Will update speed in ticks")
                .defineInRange("eldritchWillSpeed", 30, 1, 1000);

            eldritchWillCost = builder
                .comment("Eldritch Will LP cost per update")
                .defineInRange("eldritchWillCost", 60, 1, 10000);

            cullingDebug = builder
                .comment("Enable debug logging for Culling ritual")
                .define("cullingDebug", false);

            peaceCost = builder
                .comment("Upkeep LP cost for Ritual of Peace")
                .defineInRange("peaceCost", 1000, 0, 100000);

            builder.pop();
        }
    }

    public static class Sigils {
        public final ForgeConfigSpec.IntValue antimatterConsumption;
        public final ForgeConfigSpec.IntValue antimatterRange;
        public final ForgeConfigSpec.IntValue builderRange;
        public final ForgeConfigSpec.IntValue transpositionMovesUnbreakables;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> leechBlacklist;

        Sigils(ForgeConfigSpec.Builder builder) {
            builder.push("sigils");

            antimatterConsumption = builder
                .comment("LP cost for Sigil of Consumption per use")
                .defineInRange("antimatterConsumption", 25, 0, 10000);

            antimatterRange = builder
                .comment("Range of Sigil of Consumption")
                .defineInRange("antimatterRange", 8, 1, 64);

            builderRange = builder
                .comment("Range of Sigil of the Builder")
                .defineInRange("builderRange", 64, 1, 256);

            transpositionMovesUnbreakables = builder
                .comment(
                    "Determines if the Transposition sigil is allowed to move unbreakable blocks",
                    "0: Never move unbreakable blocks",
                    "1: Allow moving unbreakables, but prevent setting source position to an unbreakable block",
                    "2: Always allow moving unbreakable blocks"
                )
                .defineInRange("transpositionMovesUnbreakables", 1, 0, 2);

            leechBlacklist = builder
                .comment("Block IDs that Sigil of the Leech cannot affect")
                .defineList("leechBlacklist",
                    Arrays.asList("ic2:te", "minecraft:grass"),
                    obj -> obj instanceof String);

            builder.pop();
        }
    }

    public static class HurtCooldown {
        public final ForgeConfigSpec.EnumValue<Mode> mode;
        public final ForgeConfigSpec.BooleanValue affectBosses;
        public final ForgeConfigSpec.BooleanValue affectPlayers;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> sources;

        HurtCooldown(ForgeConfigSpec.Builder builder) {
            builder.push("hurtCooldown");

            mode = builder
                .comment(
                    "How will the Hurt Cooldown (iframes) of vanilla be affected, per damage source",
                    "An empty list on BLACKLIST mode will remove the cooldown for all damage types"
                )
                .defineEnum("mode", Mode.BLACKLIST);

            affectBosses = builder
                .comment("If true, bosses will have no iframes")
                .define("affectBosses", false);

            affectPlayers = builder
                .comment("If true, players will have no iframes")
                .define("affectPlayers", false);

            sources = builder
                .comment("List to be used when evaluating whitelist/blacklist functionality")
                .defineList("sources",
                    Arrays.asList("inFire", "inWall", "cactus", "lightningBolt", "lava", "outOfWorld"),
                    obj -> obj instanceof String);

            builder.pop();
        }
    }

    public static class BloodCore {
        public final ForgeConfigSpec.IntValue treeSpreadInterval;
        public final ForgeConfigSpec.IntValue treeSpreadRadius;
        public final ForgeConfigSpec.IntValue leafRegrowthSpeed;

        BloodCore(ForgeConfigSpec.Builder builder) {
            builder.push("bloodCore");

            treeSpreadInterval = builder
                .comment("Ticks between Blood Tree spreading attempts")
                .defineInRange("treeSpreadInterval", 20, 1, 6000);

            treeSpreadRadius = builder
                .comment("Radius in which Blood Core searches for valid sapling positions")
                .defineInRange("treeSpreadRadius", 8, 1, 32);

            leafRegrowthSpeed = builder
                .comment("Ticks between Blood Leaves regrowth attempts")
                .defineInRange("leafRegrowthSpeed", 10, 1, 6000);

            builder.pop();
        }
    }

    public enum Mode {
        DISABLED,
        WHITELIST,
        BLACKLIST
    }

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();
    }

    /**
     * Register the config with Forge
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }
}
