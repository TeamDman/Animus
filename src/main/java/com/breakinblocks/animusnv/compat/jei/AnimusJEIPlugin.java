package com.breakinblocks.animusnv.compat.jei;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.registry.AnimusRecipeSerializers;
import com.breakinblocks.animusnv.client.AnimusClientRecipeCache;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import net.minecraft.core.registries.BuiltInRegistries;
import com.breakinblocks.neovitae.common.block.NVBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JEI Plugin for Animus mod
 * Shows item info and Iron's Spells altar infusion recipes
 *
 * Note: Imperfect Ritual JEI integration is handled by NeoVitae itself.
 */
@JeiPlugin
public class AnimusJEIPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new SanguineDynamoCategory(guiHelper));

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SanguineDynamoCategory.RECIPE_TYPE,
            AnimusClientRecipeCache.get().byType(AnimusRecipeSerializers.SANGUINE_DYNAMO_TYPE.get()).stream()
                .map(RecipeHolder::value)
                .toList());

        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusItems.ANTILIFE_BUCKET.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animusnv.antilife.info")
        );

        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusItems.LIVING_TERRA_BUCKET.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animusnv.living_terra.info")
        );

        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusItems.BLOOD_APPLE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animusnv.blood_apple.info")
        );

        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusBlocks.BLOCK_ANTILIFE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animusnv.antilife_block.info")
        );


        if (ModList.get().isLoaded("evilcraft")) {
            Item rectifier = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "sanguine_rectifier"));
            if (rectifier != null && rectifier != Items.AIR) {
                registration.addIngredientInfo(
                    Arrays.asList(new ItemStack(rectifier)),
                    VanillaTypes.ITEM_STACK,
                    Component.translatable("jei.animusnv.sanguine_rectifier.info")
                );
            }
        }
    }

    private Item getAnimusItem(String name) {
        return BuiltInRegistries.ITEM.getValue(
            Identifier.fromNamespaceAndPath(Constants.Mod.MODID, name));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
            new ItemStack(AnimusBlocks.BLOCK_SANGUINE_DYNAMO.get()),
            SanguineDynamoCategory.RECIPE_TYPE
        );

    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        registration.addAliases(
            VanillaTypes.ITEM_STACK,
            List.of(new ItemStack(AnimusItems.GUIDE_BOOK.get())),
            List.of(
                "jei.animusnv.alias.guide",
                "jei.animusnv.alias.book",
                "jei.animusnv.alias.manual"
            )
        );
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (!ModList.get().isLoaded("malum")) {
            var ingredientManager = jeiRuntime.getIngredientManager();

            ingredientManager.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                List.of(
                    new ItemStack(AnimusItems.RUNIC_SENTIENT_SCYTHE.get()),
                    new ItemStack(AnimusItems.HAND_OF_DEATH.get())
                )
            );

            Animus.LOGGER.debug("JEI: Hidden Malum-dependent items (Malum not loaded)");
        }

    }
}
