package com.teamdman.animus.jei;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

        // Add info for Imperfect Ritual Stone - shows available rituals
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusBlocks.BLOCK_IMPERFECT_RITUAL_STONE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.imperfect_ritual_stone.info"),
            Component.translatable("jei.animus.imperfect_ritual_stone.regression"),
            Component.translatable("jei.animus.imperfect_ritual_stone.hunger"),
            Component.translatable("jei.animus.imperfect_ritual_stone.enhancement"),
            Component.translatable("jei.animus.imperfect_ritual_stone.reduction"),
            Component.translatable("jei.animus.imperfect_ritual_stone.boundless_skies")
        );
    }
}
