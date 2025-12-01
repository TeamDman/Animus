package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import net.minecraftforge.common.MinecraftForge;

/**
 * Compatibility module for Ars Nouveau
 * Handles Living Armor integration with Ars Nouveau spell system
 *
 * Features:
 * - Living Armor gains XP from casting glyphs
 * - Source Attunement upgrade tree for Ars Nouveau spellcasters
 */
public class ArsNouveauCompat implements ICompatModule {

    private static ArsNouveauCompat INSTANCE;

    public ArsNouveauCompat() {
        INSTANCE = this;
    }

    public static ArsNouveauCompat getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Ars Nouveau compatibility");

        // TODO: Re-enable Living Armor integration after API research
        // registerEventListeners();
        // registerSourceAttunementUpgrade();

        Animus.LOGGER.info("Ars Nouveau compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "ars_nouveau";
    }
}
