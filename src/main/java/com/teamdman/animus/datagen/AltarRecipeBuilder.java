package com.teamdman.animus.datagen;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import com.breakinblocks.neovitae.common.recipe.bloodaltar.BloodAltarRecipe;

public class AltarRecipeBuilder {
    protected int minTier = 0;
    protected int totalBlood;
    protected int craftingSpeed;
    protected int drainSpeed;
    protected Ingredient input;
    protected ItemStack result;
    protected boolean copyInputComponents = false;

    protected AltarRecipeBuilder(ItemStack result) {
        this.result = result;
        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("AltarRecipe result cannot be null or empty");
        }
    }

    public static AltarRecipeBuilder build(ItemLike result) {
        return new AltarRecipeBuilder(new ItemStack(result, 1));
    }

    public AltarRecipeBuilder minTier(int tier) {
        if (tier < 0) {
            throw new IllegalArgumentException("minTier cannot be negative");
        }
        this.minTier = tier;
        return this;
    }

    public AltarRecipeBuilder bloodNeeded(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("bloodNeeded cannot be negative");
        }
        this.totalBlood = amount;
        return this;
    }

    public AltarRecipeBuilder consumption(int craftingSpeed) {
        if (craftingSpeed < 0) {
            throw new IllegalArgumentException("consumption cannot be negative");
        }
        this.craftingSpeed = craftingSpeed;
        return this;
    }

    public AltarRecipeBuilder drain(int drainSpeed) {
        if (drainSpeed < 0) {
            throw new IllegalArgumentException("drain cannot be negative");
        }
        this.drainSpeed = drainSpeed;
        return this;
    }

    public AltarRecipeBuilder from(ItemLike input) {
        return from(Ingredient.of(input));
    }

    public AltarRecipeBuilder from(TagKey<Item> input) {
        return from(Ingredient.of(input));
    }

    public AltarRecipeBuilder from(Ingredient input) {
        this.input = input;
        return this;
    }

    public AltarRecipeBuilder copyInputComponents() {
        this.copyInputComponents = true;
        return this;
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        if (input == null) {
            throw new IllegalStateException("AltarRecipe requires an input ingredient (use .from())");
        }
        if (totalBlood <= 0) {
            throw new IllegalStateException("AltarRecipe requires bloodNeeded > 0");
        }
        BloodAltarRecipe recipe = new BloodAltarRecipe(input, result, minTier, totalBlood, craftingSpeed, drainSpeed, copyInputComponents);
        output.accept(id.withPrefix("blood_altar/"), recipe, null);
    }
}
