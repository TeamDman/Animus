package com.breakinblocks.animusnv.compat.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for Floral Supremacy ritual that handles Botania-specific code.
 * This class should only be loaded when Botania is present to avoid ClassNotFoundException.
 *
 * NOTE: Botania is not yet available for 1.21.1, so this is stubbed out.
 * When Botania is ported, re-enable the actual implementation.
 */
public class FloralSupremacyHelper {

    /**
     * Find all generating flowers within the specified radius of the master position.
     * Also returns positions of mana spreaders for acceleration.
     *
     * STUB: Returns empty result until Botania is ported to 1.21.1
     */
    public static FlowerSearchResult findFlowersAndSpreaders(Level level, BlockPos masterPos, int radius) {
        // Botania not available for 1.21.1 - return empty result
        return new FlowerSearchResult(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Tick all the generating flowers once (extra tick for enchanted soil effect).
     *
     * STUB: Does nothing until Botania is ported to 1.21.1
     */
    public static void tickFlowers(List<Object> flowers) {
        // Botania not available for 1.21.1 - no-op
    }

    /**
     * Result of searching for flowers and spreaders.
     */
    public static class FlowerSearchResult {
        private final List<Object> flowers;
        private final List<BlockPos> spreaderPositions;

        public FlowerSearchResult(List<Object> flowers, List<BlockPos> spreaderPositions) {
            this.flowers = flowers;
            this.spreaderPositions = spreaderPositions;
        }

        public int getFlowerCount() {
            return flowers.size();
        }

        public List<BlockPos> getSpreaderPositions() {
            return spreaderPositions;
        }

        public void tickFlowers() {
            FloralSupremacyHelper.tickFlowers(flowers);
        }
    }
}
