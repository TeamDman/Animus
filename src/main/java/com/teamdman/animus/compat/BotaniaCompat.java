package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;

/**
 * Compatibility module for Botania
 * Handles mana integration with Blood Magic systems
 *
 * Features:
 * - Rune of Unleashed Nature (altar rune powered by mana)
 * - Sigil of Boundless Nature (LP-to-mana conversion)
 * - Diabolical Fungi (demon will-to-mana flower)
 * - Ritual of Floral Supremacy (supercharge Botania flowers)
 */
public class BotaniaCompat implements ICompatModule {

    private static BotaniaCompat INSTANCE;

    public BotaniaCompat() {
        INSTANCE = this;
    }

    public static BotaniaCompat getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Botania compatibility");

        // Botania integration is mostly passive through blocks/items
        // No event listeners needed

        Animus.LOGGER.info("Botania compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "botania";
    }
}
