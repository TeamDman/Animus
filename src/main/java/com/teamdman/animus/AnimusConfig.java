package com.teamdman.animus;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class AnimusConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Config values will be defined here
    // Example:
    // public static final ForgeConfigSpec.BooleanValue EXAMPLE_BOOLEAN;
    // public static final ForgeConfigSpec.IntValue EXAMPLE_INT;

    static {
        BUILDER.push("Animus Configuration");

        // Config definitions go here
        // EXAMPLE_BOOLEAN = BUILDER.comment("An example boolean config")
        //     .define("exampleBoolean", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
