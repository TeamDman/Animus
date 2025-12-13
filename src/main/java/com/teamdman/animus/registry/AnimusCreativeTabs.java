package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.IronsSpellsCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AnimusCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.Mod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANIMUS_TAB = CREATIVE_TABS.register("animus_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Constants.Mod.MODID))
            .icon(() -> new ItemStack(AnimusItems.BLOOD_APPLE.get()))
            .displayItems((parameters, output) -> {
                // Add all items to the creative tab
                AnimusItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));

                // Add Iron's Spellbooks compat items if loaded
                if (ModList.get().isLoaded("irons_spellbooks")) {
                    IronsSpellsCompat.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                }
            })
            .build()
    );
}
