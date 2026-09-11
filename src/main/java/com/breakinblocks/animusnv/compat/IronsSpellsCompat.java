package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.ironsspells.AltarInfusionHandler;
import com.breakinblocks.animusnv.compat.ironsspells.ArcaneChannelingHandler;
import com.breakinblocks.animusnv.compat.ironsspells.CrimsonWillSpellHandler;
import com.breakinblocks.animusnv.compat.ironsspells.ItemBloodInfusedSpellbook;
import com.breakinblocks.animusnv.compat.ironsspells.ItemSanguineScroll;
import com.breakinblocks.neovitae.common.item.sigil.SigilItem;
import com.breakinblocks.neovitae.registry.SigilTypeRegistry;
import net.minecraft.resources.Identifier;
import com.breakinblocks.animusnv.compat.ironsspells.SentientArmorSpellHandler;
import com.breakinblocks.animusnv.compat.ironsspells.RitualArcaneMastery;
import com.breakinblocks.animusnv.compat.ironsspells.RitualIronHeart;
import com.breakinblocks.animusnv.compat.ironsspells.SanguineScrollAltarHandler;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.ritual.RitualRegistry;

/**
 * Compatibility module for Irons Spells n Spellbooks
 * Handles all integration between Animus/NeoVitae and Irons Spells
 *
 * Features:
 * - EV to Mana conversion for spell casting
 * - Blood-Infused Spellbooks (upgradeable at Ara Vitae)
 * - Sigil of Crimson Will (Spiritus spell boost)
 * - Sanguine Scrolls (reusable spell scrolls)
 * - Ritual of Arcane Mastery (upgrades spell scrolls)
 * - Sentient Armor spell integration (Arcane Channeling upgrade)
 */
public class IronsSpellsCompat implements ICompatModule {

    private static IronsSpellsCompat INSTANCE;

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.Mod.MODID);

    public static final DeferredRegister<Ritual> RITUALS =
        DeferredRegister.create(RitualRegistry.RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    public static final DeferredRegister<ImperfectRitual> IMPERFECT_RITUALS =
        DeferredRegister.create(RitualRegistry.IMPERFECT_RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    // Using lambda instead of method reference to defer class loading
    public static final DeferredHolder<Ritual, RitualArcaneMastery> ARCANE_MASTERY =
        RITUALS.register(Constants.Rituals.ARCANE_MASTERY, () -> new RitualArcaneMastery());

    public static final DeferredHolder<ImperfectRitual, RitualIronHeart> IRON_HEART =
        IMPERFECT_RITUALS.register(Constants.Rituals.IRON_HEART, () -> new RitualIronHeart());

    // Using lambda instead of method reference to defer class loading
    public static final DeferredHolder<Item, ItemBloodInfusedSpellbook> BLOOD_INFUSED_SPELLBOOK =
        ITEMS.register("blood_infused_spellbook", () -> new ItemBloodInfusedSpellbook());

    public static final DeferredHolder<Item, SigilItem> SIGIL_CRIMSON_WILL =
        ITEMS.register("sigil_crimson_will", () -> new SigilItem(
            SigilTypeRegistry.key(Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "crimson_will"))
        ));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_RASA =
        ITEMS.register("sanguine_scroll_rasa", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.RASA));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_ROBUR =
        ITEMS.register("sanguine_scroll_robur", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.ROBUR));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_ANIMATA =
        ITEMS.register("sanguine_scroll_animata", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.ANIMATA));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_SPIRITUS =
        ITEMS.register("sanguine_scroll_spiritus", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.SPIRITUS));

    public static final DeferredHolder<Item, ItemSanguineScroll> SANGUINE_SCROLL_AETHEREA =
        ITEMS.register("sanguine_scroll_aetherea", () -> new ItemSanguineScroll(ItemSanguineScroll.SlateType.AETHEREA));

    public IronsSpellsCompat() {
        INSTANCE = this;
    }

    public static IronsSpellsCompat getInstance() {
        return INSTANCE;
    }

    public static void registerDeferred(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        RITUALS.register(modEventBus);
        IMPERFECT_RITUALS.register(modEventBus);
        Animus.LOGGER.debug("Registered Irons Spells compatibility registries (items, rituals, imperfect rituals)");
    }

    @Override
    public void init() {
        Animus.LOGGER.debug("Initializing Irons Spells n Spellbooks compatibility");

        ArcaneChannelingHandler.register();
        Animus.LOGGER.debug("Registered Arcane Channeling Sentient Armor upgrade handler");

        SentientArmorSpellHandler.register();
        Animus.LOGGER.debug("Registered Sentient Armor Spell Handler");

        Animus.LOGGER.debug("Registered Spell Casting Handler (EV to mana)");

        CrimsonWillSpellHandler.register();
        Animus.LOGGER.debug("Registered Crimson Will Spell Handler");

        AltarInfusionHandler.register();
        Animus.LOGGER.debug("Registered Altar Infusion Handler");

        SanguineScrollAltarHandler.register();
        Animus.LOGGER.debug("Registered Sanguine Scroll Altar Handler");

        Animus.LOGGER.debug("Irons Spells n Spellbooks compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "irons_spellbooks";
    }
}
