package com.teamdman.animus.datagen;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import wayoftime.bloodmagic.common.recipe.alchemytable.AlchemyTableRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Alchemy Table recipes compatible with Blood Magic NV 1.21.1
 */
public class AlchemyTableRecipeBuilder {
    public static final int MAX_INPUTS = AlchemyTableRecipe.MAX_INPUTS;

    private final ItemStack output;
    private final List<Ingredient> inputs = new ArrayList<>();
    private int syphon = 0;
    private int ticks = 200;
    private int minimumTier = 0;

    private AlchemyTableRecipeBuilder(ItemStack output) {
        if (output == null || output.isEmpty()) {
            throw new IllegalArgumentException("AlchemyTableRecipe output cannot be null or empty");
        }
        this.output = output;
    }

    public static AlchemyTableRecipeBuilder build(ItemLike output) {
        return new AlchemyTableRecipeBuilder(new ItemStack(output));
    }

    public static AlchemyTableRecipeBuilder build(ItemStack output) {
        return new AlchemyTableRecipeBuilder(output);
    }

    public AlchemyTableRecipeBuilder input(ItemLike item) {
        return input(Ingredient.of(item));
    }

    public AlchemyTableRecipeBuilder input(TagKey<Item> tag) {
        return input(Ingredient.of(tag));
    }

    public AlchemyTableRecipeBuilder input(Ingredient ingredient) {
        if (inputs.size() >= MAX_INPUTS) {
            throw new IllegalStateException("AlchemyTableRecipe cannot have more than " + MAX_INPUTS + " inputs");
        }
        this.inputs.add(ingredient);
        return this;
    }

    public AlchemyTableRecipeBuilder syphon(int syphon) {
        if (syphon < 0) {
            throw new IllegalArgumentException("syphon cannot be negative");
        }
        this.syphon = syphon;
        return this;
    }

    public AlchemyTableRecipeBuilder ticks(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("ticks cannot be negative");
        }
        this.ticks = ticks;
        return this;
    }

    public AlchemyTableRecipeBuilder minimumTier(int tier) {
        if (tier < 0) {
            throw new IllegalArgumentException("minimumTier cannot be negative");
        }
        this.minimumTier = tier;
        return this;
    }

    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        if (inputs.isEmpty()) {
            throw new IllegalStateException("AlchemyTableRecipe must have at least one input");
        }
        AlchemyTableRecipe recipe = new AlchemyTableRecipe(inputs, output, syphon, ticks, minimumTier);
        recipeOutput.accept(id, recipe, null);
    }
}
