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

        // Register event listeners for Living Armor integration
        registerEventListeners();

        // Register Source Attunement upgrade
        registerSourceAttunementUpgrade();

        Animus.LOGGER.info("Ars Nouveau compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "ars_nouveau";
    }

    /**
     * Register Forge event listeners for Ars Nouveau integration
     */
    private void registerEventListeners() {
        // Living Armor XP from spell casting
        MinecraftForge.EVENT_BUS.register(com.teamdman.animus.compat.arsnouveau.LivingArmorGlyphHandler.class);

        Animus.LOGGER.info("Registered Ars Nouveau event listeners");
    }

    /**
     * Register the Source Attunement Living Armor upgrade
     */
    private void registerSourceAttunementUpgrade() {
        try {
            com.teamdman.animus.compat.arsnouveau.UpgradeSourceAttunement upgrade =
                new com.teamdman.animus.compat.arsnouveau.UpgradeSourceAttunement();

            // Register with Blood Magic's Living Armor system
            wayoftime.bloodmagic.core.LivingArmorRegistrar.registerUpgrade(upgrade);

            // Set the upgrade reference in the handler
            com.teamdman.animus.compat.arsnouveau.LivingArmorGlyphHandler.setUpgrade(upgrade);

            Animus.LOGGER.info("Registered Source Attunement Living Armor upgrade");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Source Attunement upgrade", e);
        }
    }
}
