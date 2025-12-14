package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.ironsspells.AltarInfusionHandler;
import com.teamdman.animus.compat.ironsspells.ArcaneChannelingHandler;
import com.teamdman.animus.compat.ironsspells.CrimsonWillSpellHandler;
import com.teamdman.animus.compat.ironsspells.ItemBloodInfusedSpellbook;
import com.teamdman.animus.compat.ironsspells.ItemSanguineScroll;
import com.teamdman.animus.compat.ironsspells.ItemSigilCrimsonWill;
import com.teamdman.animus.compat.ironsspells.LivingArmorSpellHandler;
import com.teamdman.animus.compat.ironsspells.RitualArcaneMastery;
import com.teamdman.animus.compat.ironsspells.SanguineScrollAltarHandler;
import com.teamdman.animus.compat.ironsspells.SpellCastingHandler;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import wayoftime.bloodmagic.ritual.Ritual;
import wayoftime.bloodmagic.ritual.RitualRegistry;

/**
 * Compatibility module for Irons Spells n Spellbooks
 * Handles all integration between Animus/Blood Magic and Irons Spells
 *
 * Features:
 * - LP to Mana conversion for spell casting
 * - Blood-Infused Spellbooks (upgradeable at Blood Altar)
 * - Sigil of Crimson Will (demon will spell boost)
 * - Sanguine Scrolls (reusable spell scrolls)
 * - Ritual of Arcane Mastery (upgrades spell scrolls)
 * - Living Armor spell integration (Arcane Channeling upgrade)
 */
public class IronsSpellsCompat implements ICompatModule {

    private static IronsSpellsCompat INSTANCE;

    // DeferredRegister for Irons Spells compatibility items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.Mod.MODID);

    // DeferredRegister for Irons Spells compatibility rituals
    public static final DeferredRegister<Ritual> RITUALS =
        DeferredRegister.create(RitualRegistry.RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    // ===== Rituals =====

    // Ritual of Arcane Mastery - upgrades spell scrolls
    // Using lambda instead of method reference to defer class loading
    public static final DeferredHolder<Ritual, RitualArcaneMastery> ARCANE_MASTERY =
        RITUALS.register(Constants.Rituals.ARCANE_MASTERY, () -> new RitualArcaneMastery());

    // ===== Spellbooks =====

    // Blood-Infused Spellbook - upgradeable at Blood Altar
    // Using lambda instead of method reference to defer class loading
    public static final DeferredHolder<Item, ItemBloodInfusedSpellbook> BLOOD_INFUSED_SPELLBOOK =
        ITEMS.register("blood_infused_spellbook", () -> new ItemBloodInfusedSpellbook());

    // ===== Sigils =====

    // Sigil of Crimson Will - boosts spell power with demon will
    // Using lambda instead of method reference to defer class loading
    public static final DeferredHolder<Item, ItemSigilCrimsonWill> SIGIL_CRIMSON_WILL =
        ITEMS.register("sigil_crimson_will", () -> new ItemSigilCrimsonWill());

    // ===== Sanguine Scrolls =====

    // Sanguine Scrolls - reusable spell scrolls with durability based on slate tier
    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_BLANK =
        ITEMS.register("sanguine_scroll_blank", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.BLANK));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_REINFORCED =
        ITEMS.register("sanguine_scroll_reinforced", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.REINFORCED));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_IMBUED =
        ITEMS.register("sanguine_scroll_imbued", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.IMBUED));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_DEMON =
        ITEMS.register("sanguine_scroll_demon", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.DEMON));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_ETHEREAL =
        ITEMS.register("sanguine_scroll_ethereal", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.ETHEREAL));

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
        RITUALS.register(modEventBus);
        Animus.LOGGER.info("Registered Irons Spells compatibility registries (items, rituals)");
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

        // Register LP-to-mana spell casting handler
        SpellCastingHandler.register();
        Animus.LOGGER.info("Registered Spell Casting Handler (LP to mana)");

        // Register Crimson Will spell power boost handler
        CrimsonWillSpellHandler.register();
        Animus.LOGGER.info("Registered Crimson Will Spell Handler");

        // Register Blood Altar infusion handler for spellbooks
        AltarInfusionHandler.register();
        Animus.LOGGER.info("Registered Altar Infusion Handler (Blood-Infused Spellbooks)");

        // Register Sanguine Scroll creation handler
        SanguineScrollAltarHandler.register();
        Animus.LOGGER.info("Registered Sanguine Scroll Altar Handler");

        // Ritual of Arcane Mastery is registered via DeferredRegister (ARCANE_MASTERY above)
        Animus.LOGGER.info("Registered Ritual of Arcane Mastery");

        Animus.LOGGER.info("Irons Spells n Spellbooks compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "irons_spellbooks";
    }
}
