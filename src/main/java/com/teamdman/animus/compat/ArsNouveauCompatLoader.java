package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import net.neoforged.bus.api.IEventBus;

/**
 * Separate loader class for Ars Nouveau compatibility.
 * This class is ONLY loaded when Ars Nouveau is present.
 *
 * By isolating the ArsNouveauCompat reference to this class,
 * we prevent NoClassDefFoundError when Ars Nouveau isn't installed.
 * The CompatHandler class can be loaded without triggering class
 * verification of ArsNouveauCompat.
 */
public class ArsNouveauCompatLoader {

    /**
     * Register Ars Nouveau compatibility deferred registries.
     * Only call this when ars_nouveau is confirmed to be loaded.
     */
    public static void registerDeferred(IEventBus modEventBus) {
        ArsNouveauCompat.registerDeferred(modEventBus);
        Animus.LOGGER.debug("Registered Ars Nouveau compatibility deferred registries");
    }
}
