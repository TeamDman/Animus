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
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.recipe.forge.ForgeRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HellfireForgeRecipeBuilder {
    public static final int MAX_INGREDIENTS = 4;

    protected double minSpiritus;
    protected double drainedSpiritus;
    protected List<Ingredient> ingredients = new ArrayList<>();
    protected ItemStackTemplate result;
    protected Optional<SpiritusType> spiritusType = Optional.empty();

    protected HellfireForgeRecipeBuilder(ItemStackTemplate result) {
        this.result = result;
        if (result == null) {
            throw new IllegalArgumentException("ForgeRecipe result cannot be null or empty");
        }
    }

    public static HellfireForgeRecipeBuilder build(ItemLike result) {
        return new HellfireForgeRecipeBuilder(new ItemStackTemplate(result.asItem()));
    }

    public static HellfireForgeRecipeBuilder build(ItemLike result, int count) {
        return new HellfireForgeRecipeBuilder(new ItemStackTemplate(result.asItem(), count));
    }

    public HellfireForgeRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(AnimusRecipeProvider.ingredientOf(tag));
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

    public HellfireForgeRecipeBuilder minSpiritus(double minSpiritus) {
        if (minSpiritus < 0) {
            throw new IllegalArgumentException("minSpiritus cannot be negative");
        }
        this.minSpiritus = minSpiritus;
        return this;
    }

    public HellfireForgeRecipeBuilder drain(double drain) {
        if (drain < 0) {
            throw new IllegalArgumentException("drain cannot be negative");
        }
        this.drainedSpiritus = drain;
        return this;
    }

    public HellfireForgeRecipeBuilder requiredSpiritusType(SpiritusType type) {
        this.spiritusType = Optional.of(type);
        return this;
    }

    public void save(RecipeOutput output, Identifier id) {
        if (ingredients.isEmpty()) {
            throw new IllegalStateException("ForgeRecipe must have at least one ingredient");
        }
        ForgeRecipe recipe = new ForgeRecipe(minSpiritus, drainedSpiritus, ingredients, result, spiritusType);
        output.accept(ResourceKey.create(Registries.RECIPE, id.withPrefix("hellfire_forge/")), recipe, null);
    }
}
