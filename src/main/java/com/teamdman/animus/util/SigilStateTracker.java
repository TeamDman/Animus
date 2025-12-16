package com.teamdman.animus.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for tracking cooldown/interval-based state for sigil effects.
 * Provides thread-safe tracking of last execution times per player.
 *
 * Usage:
 * <pre>
 * public class MySigilEffect implements ISigilEffect {
 *     private static final SigilStateTracker TRACKER = new SigilStateTracker("my_sigil");
 *
 *     public void activeTick(Level level, Player player, ...) {
 *         if (TRACKER.isReady(player.getUUID(), level.getGameTime(), 20)) {
 *             // Do effect
 *             TRACKER.updateTime(player.getUUID(), level.getGameTime());
 *         }
 *     }
 * }
 * </pre>
 */
public class SigilStateTracker {

    private final String name;
    private final Map<UUID, Long> lastExecutionTimes = new ConcurrentHashMap<>();

    /**
     * Create a new tracker with the given name (for debugging purposes).
     *
     * @param name Identifier for this tracker (e.g., sigil name)
     */
    public SigilStateTracker(String name) {
        this.name = name;
        // Register with cleanup manager
        SigilStateCleanupManager.register(this);
    }

    /**
     * Get the tracker name.
     */
    public String getName() {
        return name;
    }

    /**
     * Check if enough time has passed since last execution for this player.
     *
     * @param playerId The player's UUID
     * @param currentTime Current game time (level.getGameTime())
     * @param interval Minimum ticks between executions
     * @return true if ready to execute (first time or interval elapsed)
     */
    public boolean isReady(UUID playerId, long currentTime, int interval) {
        Long lastTime = lastExecutionTimes.get(playerId);
        return lastTime == null || currentTime - lastTime >= interval;
    }

    /**
     * Check if ready using a config-provided interval.
     *
     * @param playerId The player's UUID
     * @param currentTime Current game time
     * @param intervalSupplier Supplier for the interval (e.g., config value)
     * @return true if ready to execute
     */
    public boolean isReady(UUID playerId, long currentTime, java.util.function.IntSupplier intervalSupplier) {
        return isReady(playerId, currentTime, intervalSupplier.getAsInt());
    }

    /**
     * Update the last execution time for a player.
     * Call this after successfully performing the sigil effect.
     *
     * @param playerId The player's UUID
     * @param currentTime Current game time
     */
    public void updateTime(UUID playerId, long currentTime) {
        lastExecutionTimes.put(playerId, currentTime);
    }

    /**
     * Convenience method to check readiness and update time atomically.
     * Returns true and updates time if ready, returns false if not ready.
     *
     * @param playerId The player's UUID
     * @param currentTime Current game time
     * @param interval Minimum ticks between executions
     * @return true if effect should execute (time was updated)
     */
    public boolean checkAndUpdate(UUID playerId, long currentTime, int interval) {
        if (isReady(playerId, currentTime, interval)) {
            updateTime(playerId, currentTime);
            return true;
        }
        return false;
    }

    /**
     * Get the last execution time for a player.
     *
     * @param playerId The player's UUID
     * @return Last execution time, or null if never executed
     */
    public Long getLastTime(UUID playerId) {
        return lastExecutionTimes.get(playerId);
    }

    /**
     * Get ticks remaining until next execution is ready.
     *
     * @param playerId The player's UUID
     * @param currentTime Current game time
     * @param interval Minimum ticks between executions
     * @return Ticks remaining, or 0 if ready now
     */
    public long getTicksRemaining(UUID playerId, long currentTime, int interval) {
        Long lastTime = lastExecutionTimes.get(playerId);
        if (lastTime == null) {
            return 0;
        }
        long elapsed = currentTime - lastTime;
        return Math.max(0, interval - elapsed);
    }

    /**
     * Clean up tracking data for a player.
     * Called automatically by SigilStateCleanupManager on logout.
     *
     * @param playerId The player's UUID
     */
    public void cleanup(UUID playerId) {
        lastExecutionTimes.remove(playerId);
    }

    /**
     * Clear all tracking data.
     * Useful for server shutdown or dimension unload.
     */
    public void clearAll() {
        lastExecutionTimes.clear();
    }

    /**
     * Get the number of tracked players.
     */
    public int getTrackedCount() {
        return lastExecutionTimes.size();
    }
}
