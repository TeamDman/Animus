package com.teamdman.animus.worldgen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

/**
 * Configured features for Animus world generation
 */
public class AnimusConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLOOD_TREE =
        registerKey("blood_tree");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        register(context, BLOOD_TREE, Feature.TREE, createBloodTree().build());
    }

    private static TreeConfiguration.TreeConfigurationBuilder createBloodTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
            BlockStateProvider.simple(AnimusBlocks.BLOCK_BLOOD_WOOD.get()),  // trunk
            new StraightTrunkPlacer(4, 2, 0),  // trunk height: 4-6 blocks
            BlockStateProvider.simple(AnimusBlocks.BLOCK_BLOOD_LEAVES.get()),  // leaves
            new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),  // foliage shape
            new TwoLayersFeatureSize(1, 0, 1)  // minimum space requirements
        ).ignoreVines()
         .decorators(java.util.List.of(BloodCoreDecorator.INSTANCE));  // Add blood core decorator
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
            new ResourceLocation(Constants.Mod.MODID, name));
    }

    private static <FC extends net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration, F extends Feature<FC>>
    void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                  ResourceKey<ConfiguredFeature<?, ?>> key,
                  F feature,
                  FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
