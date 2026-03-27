package com.breakinblocks.animusnv.datagen;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.recipe.forge.ForgeRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HellfireForgeRecipeBuilder {
    public static final int MAX_INGREDIENTS = 4;

    protected double minWill;
    protected double drainedWill;
    protected List<Ingredient> ingredients = new ArrayList<>();
    protected ItemStack result;
    protected Optional<SpiritusType> willType = Optional.empty();

    protected HellfireForgeRecipeBuilder(ItemStack result) {
        this.result = result;
        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("ForgeRecipe result cannot be null or empty");
        }
    }

    public static HellfireForgeRecipeBuilder build(ItemLike result) {
        return new HellfireForgeRecipeBuilder(new ItemStack(result));
    }

    public static HellfireForgeRecipeBuilder build(ItemLike result, int count) {
        return new HellfireForgeRecipeBuilder(new ItemStack(result, count));
    }

    public HellfireForgeRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public HellfireForgeRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public HellfireForgeRecipeBuilder requires(ItemLike item, int quantity) {
        this.requires(Ingredient.of(item), quantity);
        return this;
    }

    public HellfireForgeRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public HellfireForgeRecipeBuilder requires(Ingredient ingredient, int quantity) {
        if (ingredients.size() + quantity > MAX_INGREDIENTS) {
            throw new IllegalStateException("ForgeRecipe cannot have more than " + MAX_INGREDIENTS + " ingredients");
        }
        for (int i = 0; i < quantity; i++) {
            this.ingredients.add(ingredient);
        }
        return this;
    }

    public HellfireForgeRecipeBuilder minWill(double minWill) {
        if (minWill < 0) {
            throw new IllegalArgumentException("minWill cannot be negative");
        }
        this.minWill = minWill;
        return this;
    }

    public HellfireForgeRecipeBuilder drain(double drain) {
        if (drain < 0) {
            throw new IllegalArgumentException("drain cannot be negative");
        }
        this.drainedWill = drain;
        return this;
    }

    public HellfireForgeRecipeBuilder requiredWillType(SpiritusType type) {
        this.willType = Optional.of(type);
        return this;
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        if (ingredients.isEmpty()) {
            throw new IllegalStateException("ForgeRecipe must have at least one ingredient");
        }
        ForgeRecipe recipe = new ForgeRecipe(minWill, drainedWill, ingredients, result, willType);
        output.accept(id.withPrefix("hellfire_forge/"), recipe, null);
    }
}
