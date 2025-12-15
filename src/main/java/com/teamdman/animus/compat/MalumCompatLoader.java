package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import net.neoforged.bus.api.IEventBus;

/**
 * Separate loader class for Malum compatibility.
 * This class is ONLY loaded when Malum is present.
 *
 * By isolating the MalumCompat reference to this class,
 * we prevent NoClassDefFoundError when Malum isn't installed.
 * The CompatHandler class can be loaded without triggering class
 * verification of MalumCompat.
 */
public class MalumCompatLoader {

    /**
     * Register Malum compatibility deferred registries.
     * Only call this when malum is confirmed to be loaded.
     */
    public static void registerDeferred(IEventBus modEventBus) {
        MalumCompat.registerDeferred(modEventBus);
        Animus.LOGGER.info("Registered Malum compatibility deferred registries");
    }
}
