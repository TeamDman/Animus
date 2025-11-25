package com.teamdman.animus.blocks;

import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

/**
 * Blood Sapling - grows into blood trees with a blood core at the top
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
     * Generates a simple oak-like tree with blood blocks
     */
    private static class BloodTreeGrower extends AbstractTreeGrower {
        @Nullable
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            // Return null to use custom generation
            return null;
        }

        @Override
        public boolean growTree(ServerLevel level, net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator,
                                BlockPos pos, BlockState state, RandomSource random) {
            // Custom tree generation
            level.removeBlock(pos, false);

            if (!generateBloodTree(level, pos, random)) {
                // If generation fails, replace the sapling
                level.setBlock(pos, state, 4);
                return false;
            }

            return true;
        }

        private boolean generateBloodTree(ServerLevel level, BlockPos pos, RandomSource random) {
            int height = 4 + random.nextInt(3); // 4-6 blocks tall

            // Check if there's enough space
            for (int y = 0; y <= height + 1; y++) {
                BlockPos checkPos = pos.above(y);
                if (!level.isEmptyBlock(checkPos) && y > 0) {
                    return false;
                }
            }

            // Generate trunk
            for (int y = 0; y < height; y++) {
                level.setBlock(pos.above(y), AnimusBlocks.BLOCK_BLOOD_WOOD.get().defaultBlockState(), 2);
            }

            // Replace top log with blood core
            level.setBlock(pos.above(height - 1), AnimusBlocks.BLOCK_BLOOD_CORE.get().defaultBlockState(), 2);

            // Generate leaves (blob shape) - set persistent so they don't decay
            BlockState leaves = AnimusBlocks.BLOCK_BLOOD_LEAVES.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);

            // Top layer
            placeLeaves(level, pos.above(height), leaves);

            // Middle layers (around trunk top)
            for (int y = height - 2; y <= height - 1; y++) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (Math.abs(x) == 2 && Math.abs(z) == 2) continue; // Skip corners
                        BlockPos leafPos = pos.offset(x, y, z);
                        if (level.isEmptyBlock(leafPos) || level.getBlockState(leafPos).is(Blocks.AIR)) {
                            level.setBlock(leafPos, leaves, 2);
                        }
                    }
                }
            }

            // Bottom leaf layer (smaller)
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue; // Skip center (trunk)
                    BlockPos leafPos = pos.offset(x, height - 3, z);
                    if (level.isEmptyBlock(leafPos)) {
                        level.setBlock(leafPos, leaves, 2);
                    }
                }
            }

            return true;
        }

        private void placeLeaves(ServerLevel level, BlockPos center, BlockState leaves) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(z) <= 1) { // Cross pattern
                        BlockPos leafPos = center.offset(x, 0, z);
                        if (level.isEmptyBlock(leafPos)) {
                            level.setBlock(leafPos, leaves, 2);
                        }
                    }
                }
            }
        }
    }
}
