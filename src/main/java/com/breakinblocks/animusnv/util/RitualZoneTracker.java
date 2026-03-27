package com.breakinblocks.animusnv.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks active ritual zones per level, providing spatial lookups
 * to determine if a position falls within any active ritual's area.
 * <p>
 * Each ritual class that needs zone tracking should create a static
 * {@code RitualZoneTracker} instance and delegate to it.
 */
public class RitualZoneTracker {
    private final Map<Level, Map<BlockPos, AABB>> activeZones = new HashMap<>();

    /**
     * Registers an active ritual zone.
     *
     * @param level the level the ritual is in
     * @param pos   the master ritual stone position
     * @param range the AABB defining the ritual's effect area
     */
    public void add(Level level, BlockPos pos, AABB range) {
        activeZones.computeIfAbsent(level, k -> new HashMap<>()).put(pos.immutable(), range);
    }

    /**
     * Removes an active ritual zone.
     *
     * @param level the level the ritual is in
     * @param pos   the master ritual stone position
     */
    public void remove(Level level, BlockPos pos) {
        Map<BlockPos, AABB> zones = activeZones.get(level);
        if (zones != null) {
            zones.remove(pos);
            if (zones.isEmpty()) {
                activeZones.remove(level);
            }
        }
    }

    /**
     * Checks whether a block position falls within any active ritual zone in the given level.
     * The position is checked at its center (offset by 0.5 on each axis).
     *
     * @param level the level to check
     * @param pos   the block position to test
     * @return true if the position is inside at least one active zone
     */
    public boolean isInZone(Level level, BlockPos pos) {
        Map<BlockPos, AABB> zones = activeZones.get(level);
        if (zones == null || zones.isEmpty()) {
            return false;
        }

        for (AABB range : zones.values()) {
            if (range.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all tracked zones for a level. Call this on level unload to prevent memory leaks.
     *
     * @param level the level being unloaded
     */
    public void cleanupLevel(Level level) {
        activeZones.remove(level);
    }

    /**
     * Returns the map of active zones for a level, or null if none exist.
     * Useful when callers need to iterate over individual ritual positions and their ranges.
     *
     * @param level the level to query
     * @return the map of positions to AABBs, or null
     */
    @Nullable
    public Map<BlockPos, AABB> getZones(Level level) {
        return activeZones.get(level);
    }
}
