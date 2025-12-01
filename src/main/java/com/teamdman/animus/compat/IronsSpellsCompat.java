package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import net.minecraftforge.common.MinecraftForge;

/**
 * Compatibility module for Irons Spells n Spellbooks
 * Handles all integration between Animus/Blood Magic and Irons Spells
 *
 * Features:
 * - LP to Mana conversion for spell casting
 * - Blood-Infused Spellbooks
 * - Sigils of Spell Invocation
 * - Sanguine Scrolls
 * - Demon Will spell amplification
 * - Ritual of Arcane Mastery
 * - Living Armor spell integration
 * - Alchemical mana potions
 * - Spell-Binding Ritual Stones
 */
public class IronsSpellsCompat implements ICompatModule {

    private static IronsSpellsCompat INSTANCE;

    public IronsSpellsCompat() {
        INSTANCE = this;
    }

    public static IronsSpellsCompat getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Irons Spells n Spellbooks compatibility");

        // Register event listeners for spell casting interception
        registerEventListeners();

        // Initialize items, blocks, and other registries will happen in their respective registry classes
        // This init() is just for event hooks and runtime setup

        Animus.LOGGER.info("Irons Spells n Spellbooks compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "irons_spellbooks";
    }

    /**
     * Register Forge event listeners for spell system integration
     */
    private void registerEventListeners() {
        // Phase 1: LP to Mana conversion
        MinecraftForge.EVENT_BUS.register(com.teamdman.animus.compat.ironsspells.SpellCastingHandler.class);

        // Phase 2: Living Armor integration (TODO)
        // MinecraftForge.EVENT_BUS.addListener(this::onSpellCastForArmor);

        Animus.LOGGER.info("Registered Irons Spells event listeners");
    }
}
