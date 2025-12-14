package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;

/**
 * Compatibility module for Malum
 * Handles spirit harvesting integration for weapons
 *
 * Features:
 * - Spirit harvesting for sentient weapons
 * - Enchantment compatibility checks
 */
public class MalumCompat implements ICompatModule {

    private static MalumCompat INSTANCE;

    public MalumCompat() {
        INSTANCE = this;
    }

    public static MalumCompat getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Malum compatibility");

        // Malum integration is passive - no event listeners needed
        // Integration happens through direct API calls from weapon items

        Animus.LOGGER.info("Malum compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "malum";
    }
}
