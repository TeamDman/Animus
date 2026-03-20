package com.teamdman.animus;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

/**
 * Values loaded at startup - available during registry, requires restart to change.
 */
public class AnimusStartupConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static class RitualRanges {
        public final ModConfigSpec.IntValue cullingHorizontalRange;
        public final ModConfigSpec.IntValue cullingVerticalRange;
        public final ModConfigSpec.IntValue endlessGreedHorizontalRange;
        public final ModConfigSpec.IntValue endlessGreedVerticalRange;
        public final ModConfigSpec.IntValue serenityRadius;
        public final ModConfigSpec.IntValue noliteIgnemRadius;
        public final ModConfigSpec.IntValue relentlessTidesRange;
        public final ModConfigSpec.IntValue relentlessTidesDepth;
        public final ModConfigSpec.IntValue siphonRange;
        public final ModConfigSpec.IntValue siphonDepth;
        public final ModConfigSpec.IntValue persistenceChunkRadius;

        public RitualRanges(ModConfigSpec.Builder builder) {
            builder.comment("Ritual Area Ranges",
                "These values define the initial default ranges for rituals.",
                "Can be adjusted via ritual stone GUI at runtime.",
                "Requires game restart to take effect.").push("ritualRanges");

            builder.push("culling");
            cullingHorizontalRange = builder
                .comment("Horizontal range in blocks for Ritual of Culling effect area")
                .defineInRange("horizontalRange", 10, 1, 64);
            cullingVerticalRange = builder
                .comment("Vertical range in blocks (extends both above AND below the ritual stone)")
                .defineInRange("verticalRange", 10, 1, 64);
            builder.pop();

            builder.push("endlessGreed");
            endlessGreedHorizontalRange = builder
                .comment("Horizontal range in blocks for Ritual of Endless Greed effect area")
                .defineInRange("horizontalRange", 7, 1, 32);
            endlessGreedVerticalRange = builder
                .comment("Vertical range in blocks for Ritual of Endless Greed effect area")
                .defineInRange("verticalRange", 5, 1, 32);
            builder.pop();

            builder.push("serenity");
            serenityRadius = builder
                .comment("Radius in blocks for Ritual of Serenity spawn prevention")
                .defineInRange("radius", 48, 1, 256);
            builder.pop();

            builder.push("noliteIgnem");
            noliteIgnemRadius = builder
                .comment("Radius in blocks for Ritual of Nolite Ignem fire extinguishing")
                .defineInRange("radius", 64, 1, 256);
            builder.pop();

            builder.push("relentlessTides");
            relentlessTidesRange = builder
                .comment("Horizontal radius in blocks for Ritual of Relentless Tides fluid placement")
                .defineInRange("horizontalRange", 32, 1, 64);
            relentlessTidesDepth = builder
                .comment("Maximum vertical depth in blocks for fluid placement")
                .defineInRange("verticalDepth", 128, 1, 256);
            builder.pop();

            builder.push("siphon");
            siphonRange = builder
                .comment("Horizontal radius in blocks for Ritual of Siphon fluid extraction")
                .defineInRange("horizontalRange", 32, 1, 64);
            siphonDepth = builder
                .comment("Maximum vertical depth in blocks for fluid extraction")
                .defineInRange("verticalDepth", 128, 1, 256);
            builder.pop();

            builder.push("persistence");
            persistenceChunkRadius = builder
                .comment("Chunk radius for Ritual of Persistence chunk loading (in chunks, not blocks)")
                .defineInRange("chunkRadius", 3, 1, 16);
            builder.pop();

            builder.pop();
        }
    }

    public static RitualRanges ritualRanges;

    static {
        BUILDER.comment("Animus Startup Configuration",
            "These values are loaded at startup and available during mod registration.",
            "Changes require a game restart to take effect.").push("startup");

        ritualRanges = new RitualRanges(BUILDER);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, SPEC, "animus-startup.toml");
        System.out.println("Animus: Registered startup config");
    }
}
