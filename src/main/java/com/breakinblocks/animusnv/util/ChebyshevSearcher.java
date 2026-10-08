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

        int minY = yDownward ? origin.getY() - verticalDepth + 1 : origin.getY();
        AABB area = new AABB(origin.getX() - horizontalRadius, minY, origin.getZ() - horizontalRadius,
            origin.getX() + horizontalRadius + 1, minY + verticalDepth, origin.getZ() + horizontalRadius + 1);
        return search(masterPos, origin, area, yDownward, maxChecksPerTick, predicate);
    }

    /** Searches the exact selected box, from its top downward and nearest horizontal column outward. */
    @Nullable
    public BlockPos search(BlockPos masterPos, AABB area, int maxChecksPerTick, Predicate<BlockPos> predicate) {
        return search(masterPos, masterPos, area, true, maxChecksPerTick, predicate);
    }

    @Nullable
    private BlockPos search(BlockPos masterPos, BlockPos origin, AABB area, boolean yDownward,
                            int maxChecksPerTick, Predicate<BlockPos> predicate) {
        int minX = Mth.floor(area.minX);
        int minY = Mth.floor(area.minY);
        int minZ = Mth.floor(area.minZ);
        int maxX = Mth.ceil(area.maxX) - 1;
        int maxY = Mth.ceil(area.maxY) - 1;
        int maxZ = Mth.ceil(area.maxZ) - 1;
        if (maxX < minX || maxY < minY || maxZ < minZ) {
            reset(masterPos);
            return null;
        }

        BlockPos center = new BlockPos(Mth.clamp(origin.getX(), minX, maxX),
            yDownward ? maxY : minY, Mth.clamp(origin.getZ(), minZ, maxZ));
        int horizontalRadius = horizontalRadiusOf(area, center);
        int verticalDepth = maxY - minY + 1;

        SearchState state = states.computeIfAbsent(masterPos.immutable(), k -> new SearchState());
        if (!area.equals(state.area) || !center.equals(state.center) || yDownward != state.yDownward) {
            state.area = area;
            state.center = center;
            state.yDownward = yDownward;
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

            int x = center.getX() + ringOffsetX(state.radius, state.cell);
            int z = center.getZ() + ringOffsetZ(state.radius, state.cell);
            if (x < minX || x > maxX || z < minZ || z > maxZ) {
                state.cell++;
                state.level = 0;
                continue;
            }
            int y = state.level;

            state.level++;
            if (state.level >= verticalDepth) {
                state.level = 0;
                state.cell++;
            }

            BlockPos checkPos = new BlockPos(x, yDownward ? maxY - y : minY + y, z);
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
     * Radius of the smallest square around the centre that encloses the area's horizontal bounds.
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
        AABB area;
        BlockPos center;
        boolean yDownward;
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
