package com.teamdman.animus.datagen;

import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class AnimusRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public AnimusRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // Blood Wood can be crafted into planks (if you add planks)
        // For now, just add a simple example recipe

        // Example: Blood Apple crafting
        // You can add your actual recipes here later

        // Shapeless recipes example:
        // ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AnimusItems.ITEM_BLOOD_APPLE.get())
        //     .requires(Items.APPLE)
        //     .requires(Items.REDSTONE)
        //     .unlockedBy("has_apple", has(Items.APPLE))
        //     .save(consumer);
    }
}
