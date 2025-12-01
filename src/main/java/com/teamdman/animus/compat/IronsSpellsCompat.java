package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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

    // DeferredRegister for Irons Spells compatibility items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.Mod.MODID);

    // Items
    public static final RegistryObject<Item> BLOOD_INFUSED_SPELLBOOK = ITEMS.register("blood_infused_spellbook",
        com.teamdman.animus.compat.ironsspells.ItemBloodInfusedSpellbook::new);

    public static final RegistryObject<Item> SIGIL_CRIMSON_WILL = ITEMS.register("sigil_crimson_will",
        com.teamdman.animus.compat.ironsspells.ItemSigilCrimsonWill::new);

    // Sanguine Scrolls (one for each slate tier)
    public static final RegistryObject<Item> SANGUINE_SCROLL_BLANK = ITEMS.register("sanguine_scroll_blank",
        () -> new com.teamdman.animus.compat.ironsspells.ItemSanguineScroll(
            com.teamdman.animus.compat.ironsspells.ItemSanguineScroll.SlateType.BLANK));

    public static final RegistryObject<Item> SANGUINE_SCROLL_REINFORCED = ITEMS.register("sanguine_scroll_reinforced",
        () -> new com.teamdman.animus.compat.ironsspells.ItemSanguineScroll(
            com.teamdman.animus.compat.ironsspells.ItemSanguineScroll.SlateType.REINFORCED));

    public static final RegistryObject<Item> SANGUINE_SCROLL_IMBUED = ITEMS.register("sanguine_scroll_imbued",
        () -> new com.teamdman.animus.compat.ironsspells.ItemSanguineScroll(
            com.teamdman.animus.compat.ironsspells.ItemSanguineScroll.SlateType.IMBUED));

    public static final RegistryObject<Item> SANGUINE_SCROLL_DEMON = ITEMS.register("sanguine_scroll_demon",
        () -> new com.teamdman.animus.compat.ironsspells.ItemSanguineScroll(
            com.teamdman.animus.compat.ironsspells.ItemSanguineScroll.SlateType.DEMON));

    public static final RegistryObject<Item> SANGUINE_SCROLL_ETHEREAL = ITEMS.register("sanguine_scroll_ethereal",
        () -> new com.teamdman.animus.compat.ironsspells.ItemSanguineScroll(
            com.teamdman.animus.compat.ironsspells.ItemSanguineScroll.SlateType.ETHEREAL));

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

        // Phase 2: Altar infusion for Blood-Infused Spellbooks
        MinecraftForge.EVENT_BUS.register(com.teamdman.animus.compat.ironsspells.AltarInfusionHandler.class);

        // Phase 2: Sigil of Crimson Will spell empowerment
        MinecraftForge.EVENT_BUS.register(com.teamdman.animus.compat.ironsspells.CrimsonWillSpellHandler.class);

        // Phase 2: Sanguine Scrolls altar infusion
        MinecraftForge.EVENT_BUS.register(com.teamdman.animus.compat.ironsspells.SanguineScrollAltarHandler.class);

        // Phase 5: Living Armor integration
        MinecraftForge.EVENT_BUS.register(com.teamdman.animus.compat.ironsspells.LivingArmorSpellHandler.class);
        registerArcaneChannelingUpgrade();

        Animus.LOGGER.info("Registered Irons Spells event listeners");
    }

    /**
     * Register the Arcane Channeling Living Armor upgrade
     */
    private void registerArcaneChannelingUpgrade() {
        try {
            com.teamdman.animus.compat.ironsspells.UpgradeArcaneChanneling upgrade =
                new com.teamdman.animus.compat.ironsspells.UpgradeArcaneChanneling();

            // Register with Blood Magic's Living Armor system
            wayoftime.bloodmagic.core.LivingArmorRegistrar.registerUpgrade(upgrade);

            // Set the upgrade reference in the handler
            com.teamdman.animus.compat.ironsspells.LivingArmorSpellHandler.setUpgrade(upgrade);

            Animus.LOGGER.info("Registered Arcane Channeling Living Armor upgrade");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Arcane Channeling upgrade", e);
        }
    }
}
