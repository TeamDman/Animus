package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.ironsspells.ArcaneChannelingHandler;
import com.teamdman.animus.compat.ironsspells.LivingArmorSpellHandler;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

/**
 * Compatibility module for Irons Spells n Spellbooks
 * Handles all integration between Animus/Blood Magic and Irons Spells
 *
 * Features (TODO - most disabled pending 1.21 API updates):
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

    // DeferredRegister for Irons Spells compatibility items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.Mod.MODID);

    // TODO: Items disabled - need to port to 1.21 data component system
    // ItemBloodInfusedSpellbook - Iron's Spells API changes + data components
    // ItemSigilCrimsonWill - DistExecutor removed + attribute API changes
    // ItemSanguineScroll - data components

    public IronsSpellsCompat() {
        INSTANCE = this;
    }

    public static IronsSpellsCompat getInstance() {
        return INSTANCE;
    }

    /**
     * Register the DeferredRegister to the mod event bus
     * This must be called early, during mod construction
     */
    public static void registerDeferred(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        Animus.LOGGER.info("Registered Irons Spells compatibility item registry");
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Irons Spells n Spellbooks compatibility");

        // Register Arcane Channeling Living Armor upgrade event handlers
        // The upgrade itself is defined in data/animus/bloodmagic/living_upgrades/arcane_channeling.json
        ArcaneChannelingHandler.register();
        Animus.LOGGER.info("Registered Arcane Channeling Living Armor upgrade handler");

        // Register Living Armor XP handler for spell casting
        LivingArmorSpellHandler.register();
        Animus.LOGGER.info("Registered Living Armor Spell Handler");

        // TODO: Other event handlers disabled pending API updates:
        // - SpellCastingHandler - Blood Magic SoulNetwork API changes
        // - AltarInfusionHandler - BloodAltarTile API changes
        // - CrimsonWillSpellHandler - Attribute API changes (Holder<Attribute>)
        // - SanguineScrollAltarHandler - BloodAltarTile API changes
        // - RitualArcaneMastery - RitualManager API may have changed

        Animus.LOGGER.info("Irons Spells n Spellbooks compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "irons_spellbooks";
    }
}
