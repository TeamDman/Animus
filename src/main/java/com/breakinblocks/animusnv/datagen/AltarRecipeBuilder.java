package com.breakinblocks.animusnv.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import com.breakinblocks.neovitae.common.recipe.aravitae.AraVitaeRecipe;

public class AltarRecipeBuilder {
    protected int minTier = 0;
    protected int totalBlood;
    protected int craftingSpeed;
    protected int drainSpeed;
    protected Ingredient input;
    protected ItemStackTemplate result;
    protected boolean copyInputComponents = false;

    protected AltarRecipeBuilder(ItemStackTemplate result) {
        this.result = result;
        if (result == null) {
            throw new IllegalArgumentException("AltarRecipe result cannot be null or empty");
        }
    }

    public static AltarRecipeBuilder build(ItemLike result) {
        return new AltarRecipeBuilder(new ItemStackTemplate(result.asItem(), 1));
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
        return from(AnimusRecipeProvider.ingredientOf(input));
    }

    public AltarRecipeBuilder from(Ingredient input) {
        this.input = input;
        return this;
    }

    public AltarRecipeBuilder copyInputComponents() {
        this.copyInputComponents = true;
        return this;
    }

    public void save(RecipeOutput output, Identifier id) {
        if (input == null) {
            throw new IllegalStateException("AltarRecipe requires an input ingredient (use .from())");
        }
        if (totalBlood <= 0) {
            throw new IllegalStateException("AltarRecipe requires bloodNeeded > 0");
        }
        AraVitaeRecipe recipe = new AraVitaeRecipe(input, result, minTier, totalBlood, craftingSpeed, drainSpeed, copyInputComponents);
        output.accept(ResourceKey.create(Registries.RECIPE, id.withPrefix("ara_vitae/")), recipe, null);
    }
}
