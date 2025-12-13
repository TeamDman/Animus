package com.teamdman.animus.compat;

import com.teamdman.animus.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Compatibility module for Iron's Spells mod
 * Provides blood magic scrolls that can cast spells
 * TODO: Implement when Iron's Spells updates to 1.21
 */
public class IronsSpellsCompat implements ICompatModule {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.Mod.MODID);

    // Placeholder items - these will be proper implementations when Iron's Spells updates
    public static final DeferredHolder<Item, Item> SANGUINE_SCROLL_BLANK = ITEMS.register("sanguine_scroll_blank",
        () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANGUINE_SCROLL_REINFORCED = ITEMS.register("sanguine_scroll_reinforced",
        () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANGUINE_SCROLL_IMBUED = ITEMS.register("sanguine_scroll_imbued",
        () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANGUINE_SCROLL_DEMON = ITEMS.register("sanguine_scroll_demon",
        () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SANGUINE_SCROLL_ETHEREAL = ITEMS.register("sanguine_scroll_ethereal",
        () -> new Item(new Item.Properties()));

    @Override
    public void init() {
        // Will initialize Iron's Spells compat when available
    }

    @Override
    public String getModId() {
        return "irons_spellbooks";
    }
}
