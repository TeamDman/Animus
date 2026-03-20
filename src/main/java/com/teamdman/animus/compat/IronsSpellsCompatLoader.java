package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import net.neoforged.bus.api.IEventBus;

/**
 * Separate loader class for Iron's Spells compatibility.
 * This class is ONLY loaded when Iron's Spells is present.
 *
 * By isolating the IronsSpellsCompat reference to this class,
 * we prevent NoClassDefFoundError when Iron's Spells isn't installed.
 * The CompatHandler class can be loaded without triggering class
 * verification of IronsSpellsCompat.
 */
public class IronsSpellsCompatLoader {

    /**
     * Register Iron's Spells compatibility deferred registries.
     * Only call this when irons_spellbooks is confirmed to be loaded.
     */
    public static void registerDeferred(IEventBus modEventBus) {
        IronsSpellsCompat.registerDeferred(modEventBus);
        Animus.LOGGER.debug("Registered Iron's Spells compatibility deferred registries");
    }
}
