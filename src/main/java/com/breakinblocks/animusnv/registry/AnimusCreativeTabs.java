package com.breakinblocks.animusnv.registry;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AnimusCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.Mod.MODID);

    // Iron's Spells compat item names - looked up from registry to avoid class loading issues
    private static final String[] IRONS_SPELLS_COMPAT_ITEMS = {
        "blood_infused_spellbook",
        "sigil_crimson_will",
        "sanguine_scroll_rasa",
        "sanguine_scroll_robur",
        "sanguine_scroll_animata",
        "sanguine_scroll_spiritus",
        "sanguine_scroll_aetherea"
    };

    // Ars Nouveau compat item names - looked up from registry to avoid class loading issues
    private static final String[] ARS_NOUVEAU_COMPAT_ITEMS = {
        "arcane_rune"
    };

    // EvilCraft compat item names
    private static final String[] EVILCRAFT_COMPAT_ITEMS = {
        "sanguine_rectifier"
    };

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANIMUS_TAB = CREATIVE_TABS.register("animusnv_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Constants.Mod.MODID))
            .icon(() -> new ItemStack(AnimusItems.BLOOD_APPLE.get()))
            .displayItems((parameters, output) -> {
                AnimusItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));

                // Registry lookup avoids class loading issues with optional mod classes
                if (ModList.get().isLoaded("irons_spellbooks")) {
                    for (String itemName : IRONS_SPELLS_COMPAT_ITEMS) {
                        Item item = BuiltInRegistries.ITEM.getValue(
                            Identifier.fromNamespaceAndPath(Constants.Mod.MODID, itemName));
                        if (item != null && item != Items.AIR) {
                            output.accept(new ItemStack(item));
                        }
                    }
                }

                if (ModList.get().isLoaded("ars_nouveau")) {
                    for (String itemName : ARS_NOUVEAU_COMPAT_ITEMS) {
                        Item item = BuiltInRegistries.ITEM.getValue(
                            Identifier.fromNamespaceAndPath(Constants.Mod.MODID, itemName));
                        if (item != null && item != Items.AIR) {
                            output.accept(new ItemStack(item));
                        }
                    }
                }

                if (ModList.get().isLoaded("evilcraft")) {
                    for (String itemName : EVILCRAFT_COMPAT_ITEMS) {
                        Item item = BuiltInRegistries.ITEM.getValue(
                            Identifier.fromNamespaceAndPath(Constants.Mod.MODID, itemName));
                        if (item != null && item != Items.AIR) {
                            output.accept(new ItemStack(item));
                        }
                    }
                }
            })
            .build()
    );
}
