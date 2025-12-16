package com.teamdman.animus.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Centralized manager for cleaning up sigil state when players log out.
 * Sigil effects and trackers register their cleanup handlers here,
 * and AnimusEventHandler calls cleanupPlayer() on logout.
 *
 * This eliminates the need for each sigil effect to have its own
 * onPlayerLogout() static method called from the event handler.
 */
public final class SigilStateCleanupManager {

    private static final List<SigilStateTracker> trackers = new ArrayList<>();
    private static final List<Consumer<UUID>> customHandlers = new ArrayList<>();

    private SigilStateCleanupManager() {
        // Utility class - no instantiation
    }

    /**
     * Register a SigilStateTracker for automatic cleanup.
     * Called automatically when a SigilStateTracker is created.
     *
     * @param tracker The tracker to register
     */
    public static void register(SigilStateTracker tracker) {
        if (!trackers.contains(tracker)) {
            trackers.add(tracker);
        }
    }

    /**
     * Register a custom cleanup handler for more complex cleanup needs.
     * Use this for sigil effects that need to do more than just clear a map.
     *
     * @param handler Consumer that receives the player UUID on logout
     */
    public static void registerCustomHandler(Consumer<UUID> handler) {
        if (!customHandlers.contains(handler)) {
            customHandlers.add(handler);
        }
    }

    /**
     * Clean up all registered state for a player.
     * Call this from the player logout event handler.
     *
     * @param playerId The UUID of the player logging out
     */
    public static void cleanupPlayer(UUID playerId) {
        // Clean up all registered trackers
        for (SigilStateTracker tracker : trackers) {
            tracker.cleanup(playerId);
        }

        // Run all custom handlers
        for (Consumer<UUID> handler : customHandlers) {
            try {
                handler.accept(playerId);
            } catch (Exception e) {
                // Log but don't crash - other handlers should still run
                com.teamdman.animus.Animus.LOGGER.error(
                    "Error in sigil cleanup handler for player {}: {}",
                    playerId, e.getMessage()
                );
            }
        }
    }

    /**
     * Clear all state for all players.
     * Useful for server shutdown.
     */
    public static void clearAll() {
        for (SigilStateTracker tracker : trackers) {
            tracker.clearAll();
        }
    }

    /**
     * Get the number of registered trackers.
     * Useful for debugging.
     */
    public static int getTrackerCount() {
        return trackers.size();
    }

    /**
     * Get the number of registered custom handlers.
     * Useful for debugging.
     */
    public static int getCustomHandlerCount() {
        return customHandlers.size();
    }
}
