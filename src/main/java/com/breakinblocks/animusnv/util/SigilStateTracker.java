package com.breakinblocks.animusnv.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;

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

    public SigilStateTracker(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isReady(UUID playerId, long currentTime, int interval) {
        Long lastTime = lastExecutionTimes.get(playerId);
        return lastTime == null || currentTime - lastTime >= interval;
    }

    public boolean isReady(UUID playerId, long currentTime, IntSupplier intervalSupplier) {
        return isReady(playerId, currentTime, intervalSupplier.getAsInt());
    }

    public void updateTime(UUID playerId, long currentTime) {
        lastExecutionTimes.put(playerId, currentTime);
    }

    public boolean checkAndUpdate(UUID playerId, long currentTime, int interval) {
        if (isReady(playerId, currentTime, interval)) {
            updateTime(playerId, currentTime);
            return true;
        }
        return false;
    }

    public Long getLastTime(UUID playerId) {
        return lastExecutionTimes.get(playerId);
    }

    public long getTicksRemaining(UUID playerId, long currentTime, int interval) {
        Long lastTime = lastExecutionTimes.get(playerId);
        if (lastTime == null) {
            return 0;
        }
        long elapsed = currentTime - lastTime;
        return Math.max(0, interval - elapsed);
    }

    public void cleanup(UUID playerId) {
        lastExecutionTimes.remove(playerId);
    }

    public void clearAll() {
        lastExecutionTimes.clear();
    }

    public int getTrackedCount() {
        return lastExecutionTimes.size();
    }
}
