package com.teamdman.animus.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

/**
 * Blood Sapling - grows into blood trees
 * Note: Tree generation requires world gen features to be set up
 */
public class BlockBloodSapling extends SaplingBlock {

    public BlockBloodSapling() {
        super(new BloodTreeGrower(), BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .strength(0.0F)
            .sound(SoundType.GRASS)
        );
    }

    /**
     * Tree grower for blood trees
     * TODO: Implement proper blood tree feature once world gen is set up
     */
    private static class BloodTreeGrower extends AbstractTreeGrower {
        @Nullable
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            // TODO: Return custom blood tree feature
            // For now, return null to prevent crashes
            // You'll need to create a custom tree feature in world gen
            return null;
        }
    }
}
