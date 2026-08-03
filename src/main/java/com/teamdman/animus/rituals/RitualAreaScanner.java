package com.teamdman.animus.rituals;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.ritual.AreaDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class RitualAreaScanner {

    private static final int MAX_CHECKS_PER_RUN = 4096;

    private final Map<Key, Cursor> cursors = new HashMap<>();

    public BlockPos scan(Level level, BlockPos masterPos, AreaDescriptor range, Predicate<BlockPos> matches) {
        AABB aabb = range.getAABB(masterPos);
        int minX = Mth.floor(aabb.minX);
        int minY = Mth.floor(aabb.minY);
        int minZ = Mth.floor(aabb.minZ);
        int maxX = Mth.ceil(aabb.maxX) - 1;
        int maxY = Mth.ceil(aabb.maxY) - 1;
        int maxZ = Mth.ceil(aabb.maxZ) - 1;

        if (maxX < minX || maxY < minY || maxZ < minZ) {
            return null;
        }

        int centerX = Math.floorDiv(minX + maxX, 2);
        int centerZ = Math.floorDiv(minZ + maxZ, 2);
        int maxRadius = Math.max(
            Math.max(centerX - minX, maxX - centerX),
            Math.max(centerZ - minZ, maxZ - centerZ)
        );
        int height = maxY - minY + 1;

        Key key = new Key(level.dimension().location(), masterPos.immutable());
        Cursor cursor = cursors.computeIfAbsent(key, k -> new Cursor());

        if (cursor.radius > maxRadius) {
            cursor.reset();
        }

        for (int checks = 0; checks < MAX_CHECKS_PER_RUN; checks++) {
            int ringCells = cursor.radius == 0 ? 1 : 8 * cursor.radius;

            if (cursor.cell >= ringCells) {
                cursor.cell = 0;
                cursor.level = 0;
                cursor.radius++;
                if (cursor.radius > maxRadius) {
                    cursor.reset();
                }
                continue;
            }

            int x = centerX + ringOffsetX(cursor.radius, cursor.cell);
            int z = centerZ + ringOffsetZ(cursor.radius, cursor.cell);
            int y = maxY - cursor.level;

            cursor.level++;
            if (cursor.level >= height) {
                cursor.level = 0;
                cursor.cell++;
            }

            if (x < minX || x > maxX || z < minZ || z > maxZ) {
                continue;
            }

            BlockPos candidate = new BlockPos(x, y, z);
            if (matches.test(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    public void forget(Level level, BlockPos masterPos) {
        cursors.remove(new Key(level.dimension().location(), masterPos.immutable()));
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

    private static final class Cursor {
        int radius;
        int cell;
        int level;

        void reset() {
            radius = 0;
            cell = 0;
            level = 0;
        }
    }

    private record Key(ResourceLocation dimension, BlockPos pos) {
        private Key {
            Objects.requireNonNull(dimension);
            Objects.requireNonNull(pos);
        }
    }
}
