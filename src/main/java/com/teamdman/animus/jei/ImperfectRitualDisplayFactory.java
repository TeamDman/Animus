package com.teamdman.animus.jei;

import com.teamdman.animus.Animus;
import com.teamdman.animus.recipes.ImperfectRitualRecipe;
import com.teamdman.animus.registry.AnimusRecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create all ImperfectRitualDisplay objects for JEI
 * Derives displays from loaded ImperfectRitualRecipe instances
 */
public class ImperfectRitualDisplayFactory {

    public static List<ImperfectRitualDisplay> createAllDisplays(RecipeManager recipeManager) {
        List<ImperfectRitualDisplay> displays = new ArrayList<>();

        var recipes = recipeManager.getAllRecipesFor(AnimusRecipeTypes.IMPERFECT_RITUAL_TYPE.get());
        for (var recipeHolder : recipes) {
            ImperfectRitualRecipe recipe = recipeHolder;

            // Skip mod-dependent recipes whose trigger block couldn't be resolved
            if (recipe.getTriggerBlock().getBlock() == Blocks.AIR) {
                continue;
            }

            String key = recipe.getRitualKey();
            displays.add(new ImperfectRitualDisplay(
                key,
                recipe.getTriggerBlock(),
                recipe.getLpCost(),
                Component.translatable("jei.animus.ritual." + key + ".name"),
                Component.translatable("jei.animus.ritual." + key + ".desc"),
                recipe.getRequiredMod()
            ));
        }

        Animus.LOGGER.info("JEI: Created {} imperfect ritual displays from recipes", displays.size());
        return displays;
    }
}
