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
                .define("canKillBuffedMobs", false);

            bloodPerApple = builder
                .comment("Amount of blood to add to altar per Blood Apple")
                .defineInRange("bloodPerApple", 50, 1, 10000);

            builder.pop();
        }
    }

    // Ritual Configuration
    public static class Rituals {
        public final ForgeConfigSpec.BooleanValue killWither;
        public final ForgeConfigSpec.IntValue witherCost;
        public final ForgeConfigSpec.BooleanValue cullingKillsTnT;
        public final ForgeConfigSpec.BooleanValue cullingDebug;
        public final ForgeConfigSpec.IntValue peaceCost;

        public Rituals(ForgeConfigSpec.Builder builder) {
            builder.push("rituals");

            killWither = builder
                .comment("Allow Ritual of Culling to kill Withers")
                .define("killWither", true);

            witherCost = builder
                .comment("LP cost for killing a Wither")
                .defineInRange("witherCost", 25000, 0, 1000000);

            cullingKillsTnT = builder
                .comment("Allow Ritual of Culling to destroy primed TNT")
                .define("CullingKillsTnT", true);

            cullingDebug = builder
                .comment("Enable debug logging for Ritual of Culling")
                .define("CullingDebug", false);

            peaceCost = builder
                .comment("LP cost per mob spawned by Ritual of Peace")
                .defineInRange("peaceCost", 1000, 1, 100000);

            builder.pop();
        }
    }

    // Sigil Configuration
    public static class Sigils {
        public final ForgeConfigSpec.IntValue antimatterConsumption;
        public final ForgeConfigSpec.IntValue antimatterRange;
        public final ForgeConfigSpec.IntValue builderRange;
        public final ForgeConfigSpec.IntValue transpositionMovesUnbreakables;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> leechBlacklist;

        public Sigils(ForgeConfigSpec.Builder builder) {
            builder.push("sigils");

            antimatterConsumption = builder
                .comment("LP cost for Sigil of Consumption per block")
                .defineInRange("antimatterConsumption", 25, 1, 10000);

            antimatterRange = builder
                .comment("Range of Sigil of Consumption in blocks")
                .defineInRange("antimatterRange", 8, 1, 64);

            builderRange = builder
                .comment("Range of Sigil of Builder in blocks")
                .defineInRange("builderRange", 64, 1, 256);

            transpositionMovesUnbreakables = builder
                .comment(
                    "Determines if Sigil of Transposition can move unbreakable blocks",
                    "0: Never move unbreakable blocks",
                    "1: Allow moving, but prevent setting source to unbreakable",
                    "2: Always allow moving unbreakable blocks"
                )
                .defineInRange("transpositionMovesUnbreakables", 1, 0, 2);

            leechBlacklist = builder
                .comment("Block IDs that Sigil of Leech cannot consume")
                .defineList(
                    "leechBlacklist",
                    Arrays.asList("ic2:te", "minecraft:grass"),
                    obj -> obj instanceof String
                );

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

    // Config instances
    public static General general;
    public static Rituals rituals;
    public static Sigils sigils;
    public static HurtCooldown hurtCooldown;

    static {
        BUILDER.comment("Animus Configuration").push("animus");

        general = new General(BUILDER);
        rituals = new Rituals(BUILDER);
        sigils = new Sigils(BUILDER);
        hurtCooldown = new HurtCooldown(BUILDER);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
