package com.breakinblocks.animusnv.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Reusable center-outward Chebyshev distance block searcher.
 * Iterates positions in expanding square rings from a center point,
 * checking a configurable number of positions per tick and resuming
 * where it left off on the next call. Once the whole volume has been
 * swept the cursor wraps back to the centre and sweeps again, so a
 * ritual keeps reacting to blocks that change after it started.
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
     * @param yDownward        if true, Y iterates downward from origin; if false, upward
     * @param maxChecksPerTick max positions to check per call
     * @param predicate        test for each BlockPos; return true to select it
     * @return the first matching position, or null if batch exhausted
     */
    @Nullable
    public BlockPos search(BlockPos masterPos, BlockPos origin, int horizontalRadius, int verticalDepth,
                           boolean yDownward, int maxChecksPerTick, Predicate<BlockPos> predicate) {
        if (horizontalRadius < 0 || verticalDepth < 1) {
            return null;
        }

        SearchState state = states.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        if (state.radius > horizontalRadius || state.level >= verticalDepth) {
            state.reset();
        }

        for (int checks = 0; checks < maxChecksPerTick; checks++) {
            int ringCells = state.radius == 0 ? 1 : 8 * state.radius;

            if (state.cell >= ringCells) {
                state.cell = 0;
                state.level = 0;
                state.radius++;
                if (state.radius > horizontalRadius) {
                    state.reset();
                }
                continue;
            }

            int x = ringOffsetX(state.radius, state.cell);
            int z = ringOffsetZ(state.radius, state.cell);
            int y = state.level;

            state.level++;
            if (state.level >= verticalDepth) {
                state.level = 0;
                state.cell++;
            }

            BlockPos checkPos = yDownward ? origin.offset(x, -y, z) : origin.offset(x, y, z);
            if (predicate.test(checkPos)) {
                return checkPos;
            }
        }

        return null;
    }

    public void reset(BlockPos masterPos) {
        states.remove(masterPos);
    }

    /**
     * Largest horizontal step from the centre that still lands inside the area.
     * The AABB's upper bound is exclusive, so it is pulled in by one before measuring.
     */
    public static int horizontalRadiusOf(AABB area, BlockPos centre) {
        int maxX = Mth.ceil(area.maxX) - 1;
        int maxZ = Mth.ceil(area.maxZ) - 1;
        return Math.max(
            Math.max(centre.getX() - Mth.floor(area.minX), maxX - centre.getX()),
            Math.max(centre.getZ() - Mth.floor(area.minZ), maxZ - centre.getZ())
        );
    }

    /**
     * Number of Y levels from the centre down to the bottom of the area, inclusive.
     */
    public static int downwardDepthOf(AABB area, BlockPos centre) {
        return Math.max(1, centre.getY() - Mth.floor(area.minY) + 1);
    }

    private static int ringOffsetX(int radius, int cell) {
        if (radius == 0) {
            return 0;
        }
        int side = 2 * radius + 1;
        if (cell < side) {
            return -radius + cell;
        }
        if (cell < side * 2) {
            return -radius + (cell - side);
        }
        if (cell < side * 2 + (2 * radius - 1)) {
            return -radius;
        }
        return radius;
    }

    private static int ringOffsetZ(int radius, int cell) {
        if (radius == 0) {
            return 0;
        }
        int side = 2 * radius + 1;
        if (cell < side) {
            return -radius;
        }
        if (cell < side * 2) {
            return radius;
        }
        if (cell < side * 2 + (2 * radius - 1)) {
            return -radius + 1 + (cell - side * 2);
        }
        return -radius + 1 + (cell - side * 2 - (2 * radius - 1));
    }

    private static final class SearchState {
        int radius;
        int cell;
        int level;

        void reset() {
            radius = 0;
            cell = 0;
            level = 0;
        }
    }
}
