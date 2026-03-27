package com.breakinblocks.animusnv.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Centralized cleanup for sigil state on player logout.
 * AnimusEventHandler calls cleanupPlayer() on logout, which propagates
 * to all registered trackers and custom handlers.
 */
public final class SigilStateCleanupManager {

    private static final List<SigilStateTracker> trackers = new ArrayList<>();
    private static final List<Consumer<UUID>> customHandlers = new ArrayList<>();

    private SigilStateCleanupManager() {
    }

    public static void register(SigilStateTracker tracker) {
        if (!trackers.contains(tracker)) {
            trackers.add(tracker);
        }
    }

    public static void registerCustomHandler(Consumer<UUID> handler) {
        if (!customHandlers.contains(handler)) {
            customHandlers.add(handler);
        }
    }

    public static void cleanupPlayer(UUID playerId) {
        for (SigilStateTracker tracker : trackers) {
            tracker.cleanup(playerId);
        }

        for (Consumer<UUID> handler : customHandlers) {
            try {
                handler.accept(playerId);
            } catch (Exception e) {
                com.breakinblocks.animusnv.Animus.LOGGER.error(
                    "Error in sigil cleanup handler for player {}: {}",
                    playerId, e.getMessage()
                );
            }
        }
    }

    public static void clearAll() {
        for (SigilStateTracker tracker : trackers) {
            tracker.clearAll();
        }
    }

    public static int getTrackerCount() {
        return trackers.size();
    }

    public static int getCustomHandlerCount() {
        return customHandlers.size();
    }
}
