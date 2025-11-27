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
        public final ForgeConfigSpec.IntValue leachRange;
        public final ForgeConfigSpec.IntValue transpositionMovesUnbreakables;

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

            leachRange = builder
                .comment("Range of Sigil of Nature's Leach for consuming blocks in the world (in blocks)")
                .defineInRange("leachRange", 8, 1, 64);

            transpositionMovesUnbreakables = builder
                .comment(
                    "Determines if Sigil of Transposition can move unbreakable blocks",
                    "0: Never move unbreakable blocks",
                    "1: Allow moving, but prevent setting source to unbreakable",
                    "2: Always allow moving unbreakable blocks"
                )
                .defineInRange("transpositionMovesUnbreakables", 1, 0, 2);

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

        public BloodCore(ForgeConfigSpec.Builder builder) {
            builder.push("bloodCore");

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
