package com.breakinblocks.animusnv.util;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Reusable center-outward Chebyshev distance block searcher.
 * Iterates positions in expanding square rings from a center point,
 * checking a configurable number of positions per tick and resuming
 * where it left off on the next call.
 */
public class ChebyshevSearcher {

    private final Map<BlockPos, SearchState> states = new HashMap<>();

    /**
     * Searches outward from the origin, checking up to maxChecksPerTick positions.
     * Returns the first position matching the predicate, or null if none found this tick.
     * Resumes from where it left off on subsequent calls for the same masterPos.
     *
     * @param masterPos        the ritual master position (used as cache key)
     * @param origin           the center position to search from
     * @param horizontalRadius max horizontal distance from origin
     * @param verticalDepth    number of Y levels to check
     * @param yDownward        if true, Y iterates downward from origin; if false, upward (into ground)
     * @param maxChecksPerTick max positions to check per call
     * @param predicate        test for each BlockPos; return true to select it
     * @return the first matching position, or null if batch exhausted
     */
    @Nullable
    public BlockPos search(BlockPos masterPos, BlockPos origin, int horizontalRadius, int verticalDepth,
                           boolean yDownward, int maxChecksPerTick, Predicate<BlockPos> predicate) {
        SearchState state = states.computeIfAbsent(masterPos.immutable(), k -> new SearchState());
        int checks = 0;

        for (int radius = state.radius; radius <= horizontalRadius && checks < maxChecksPerTick; radius++) {
            for (int x = -radius; x <= radius && checks < maxChecksPerTick; x++) {
                if (radius == state.radius && x < state.x) continue;

                for (int z = -radius; z <= radius && checks < maxChecksPerTick; z++) {
                    if (radius == state.radius && x == state.x && z < state.z) continue;

                    // Only check perimeter blocks (skip interior; already checked at smaller radius)
                    if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) continue;

                    int startY = (radius == state.radius && x == state.x && z == state.z) ? state.y : 0;
                    for (int y = startY; y < verticalDepth && checks < maxChecksPerTick; y++) {
                        BlockPos checkPos = yDownward
                                ? origin.offset(x, -y, z)
                                : origin.offset(x, y, z);
                        checks++;

                        state.radius = radius;
                        state.x = x;
                        state.z = z;
                        state.y = y;

                        if (predicate.test(checkPos)) {
                            advanceState(state, radius, verticalDepth);
                            return checkPos;
                        }
                    }
                    state.y = 0;
                }
                state.z = -radius;
            }
        }

        if (state.radius > horizontalRadius) {
            states.remove(masterPos);
        }

        return null;
    }

    private static void advanceState(SearchState state, int radius, int verticalDepth) {
        state.y++;
        if (state.y >= verticalDepth) {
            state.y = 0;
            state.z++;
            if (state.z > radius) {
                state.z = -radius;
                state.x++;
                if (state.x > radius) {
                    state.x = -radius;
                    state.z = -radius;
                    state.radius++;
                }
            }
        }
    }

    public void reset(BlockPos masterPos) {
        states.remove(masterPos);
    }

    private static class SearchState {
        int radius = 0;
        int x = 0;
        int z = 0;
        int y = 0;
    }
}
