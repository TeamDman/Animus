package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.items.ItemReagent;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.ritual.Ritual;

import java.lang.reflect.Field;
import java.util.Map;

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

    public static final RegistryObject<Item> REAGENT_CRIMSON_WILL = ITEMS.register("reagentcrimsonwill",
            ItemReagent::new);

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

    private static boolean ritualRegistered = false;

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Irons Spells n Spellbooks compatibility");

        // Register event listeners for spell casting interception
        registerEventListeners();

        // Register Ritual of Arcane Mastery during server start (after Blood Magic's Patchouli registration)
        // This avoids the "multiblock already registered" conflict
        MinecraftForge.EVENT_BUS.register(RitualRegistrationHandler.class);

        // Initialize items, blocks, and other registries will happen in their respective registry classes
        // This init() is just for event hooks and runtime setup

        Animus.LOGGER.info("Irons Spells n Spellbooks compatibility initialized successfully");
    }

    /**
     * Handler class for delayed ritual registration
     */
    public static class RitualRegistrationHandler {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            if (!ritualRegistered) {
                registerRitualDelayed();
                ritualRegistered = true;
            }
        }
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

    /**
     * Register Ritual of Arcane Mastery using reflection
     * Blood Magic doesn't expose a public API for programmatic ritual registration,
     * so we need to access the private maps in RitualManager.
     *
     * This is called during ServerStartingEvent to ensure it runs AFTER Blood Magic's
     * Patchouli multiblock registration (which happens during FMLLoadCompleteEvent).
     */
    @SuppressWarnings("unchecked")
    private static void registerRitualDelayed() {
        try {
            Ritual ritual = new com.teamdman.animus.compat.ironsspells.RitualArcaneMastery();
            String ritualId = Constants.Rituals.ARCANE_MASTERY;

            // Get the private 'rituals' map from RitualManager
            Field ritualsField = BloodMagic.RITUAL_MANAGER.getClass().getDeclaredField("rituals");
            ritualsField.setAccessible(true);
            Map<String, Ritual> rituals = (Map<String, Ritual>) ritualsField.get(BloodMagic.RITUAL_MANAGER);

            // Get the private 'ritualsReverse' map from RitualManager
            Field ritualsReverseField = BloodMagic.RITUAL_MANAGER.getClass().getDeclaredField("ritualsReverse");
            ritualsReverseField.setAccessible(true);
            Map<Ritual, String> ritualsReverse = (Map<Ritual, String>) ritualsReverseField.get(BloodMagic.RITUAL_MANAGER);

            // Get the private 'sortedRituals' list from RitualManager
            Field sortedRitualsField = BloodMagic.RITUAL_MANAGER.getClass().getDeclaredField("sortedRituals");
            sortedRitualsField.setAccessible(true);
            java.util.List<Ritual> sortedRituals = (java.util.List<Ritual>) sortedRitualsField.get(BloodMagic.RITUAL_MANAGER);

            // Register the ritual
            rituals.put(ritualId, ritual);
            ritualsReverse.put(ritual, ritualId);
            sortedRituals.add(ritual);

            Animus.LOGGER.info("Registered Ritual of Arcane Mastery with Blood Magic");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Ritual of Arcane Mastery", e);
        }
    }
}