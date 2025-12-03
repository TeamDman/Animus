package com.teamdman.animus.jei;

import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.IronsSpellsCompat;
import com.teamdman.animus.registry.AnimusBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;

import java.util.Arrays;

/**
 * JEI Plugin for Animus mod
 * Shows special transformations like Lightning + Life Essence = AntiLife
 */
@JeiPlugin
public class AnimusJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Add info for AntiLife Bucket - explains the lightning transformation
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(com.teamdman.animus.registry.AnimusItems.ANTILIFE_BUCKET.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.antilife.info")
        );

        // Add info for AntiLife block
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusBlocks.BLOCK_ANTILIFE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.antilife_block.info")
        );

        // Add info for Imperfect Ritual Stone - general info
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusBlocks.BLOCK_IMPERFECT_RITUAL_STONE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.imperfect_ritual_stone.info")
        );

        // Ritual of Regression - Bookshelf trigger
        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(Items.BOOKSHELF)),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.imperfect_ritual_stone.regression")
        );

        // Ritual of Hunger - Bone Block trigger
        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(Items.BONE_BLOCK)),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.imperfect_ritual_stone.hunger")
        );

        // Ritual of Enhancement - Amethyst Block trigger
        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(Items.AMETHYST_BLOCK)),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.imperfect_ritual_stone.enhancement")
        );

        // Ritual of Reduction - Bookshelf trigger (NOTE: Conflicts with Regression!)
        // This will append to the bookshelf info
        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(Items.BOOKSHELF)),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.imperfect_ritual_stone.reduction")
        );

        // Ritual of Boundless Skies - Ancient Debris trigger
        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(Items.ANCIENT_DEBRIS)),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.imperfect_ritual_stone.boundless_skies")
        );

        // Sanguine Scrolls (only if Iron's Spellbooks is loaded)
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registerSanguineScrollsJEI(registration);
        }
    }

    /**
     * Register JEI info for Sanguine Scrolls
     * Called only when Iron's Spellbooks is present
     */
    private void registerSanguineScrollsJEI(IRecipeRegistration registration) {
        // Add info for all Sanguine Scroll tiers - this makes them searchable in JEI
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_BLANK.get()),
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_REINFORCED.get()),
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_IMBUED.get()),
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_DEMON.get()),
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_ETHEREAL.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.info")
        );

        // Individual tier info for crafting costs
        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_BLANK.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.blank")
        );

        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_REINFORCED.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.reinforced")
        );

        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_IMBUED.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.imbued")
        );

        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_DEMON.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.demon")
        );

        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_ETHEREAL.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.ethereal")
        );
    }
}
