package com.breakinblocks.animusnv.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.recipe.alchemyarray.AlchemyArrayRecipe;

public class AlchemyArrayRecipeBuilder {
    private final ItemStackTemplate output;
    private Ingredient baseInput;
    private Ingredient addedInput;
    private Identifier texture;

    private AlchemyArrayRecipeBuilder(ItemStackTemplate output) {
        if (output == null) {
            throw new IllegalArgumentException("AlchemyArrayRecipe output cannot be null or empty");
        }
        this.output = output;
        this.texture = NeoVitae.rl("textures/models/alchemyarrays/sigil.png");
    }

    public static AlchemyArrayRecipeBuilder build(ItemLike output) {
        return new AlchemyArrayRecipeBuilder(new ItemStackTemplate(output.asItem()));
    }

    public static AlchemyArrayRecipeBuilder build(ItemStackTemplate output) {
        return new AlchemyArrayRecipeBuilder(output);
    }

    public AlchemyArrayRecipeBuilder base(ItemLike item) {
        this.baseInput = Ingredient.of(item);
        return this;
    }

    public AlchemyArrayRecipeBuilder base(Ingredient ingredient) {
        this.baseInput = ingredient;
        return this;
    }

    public AlchemyArrayRecipeBuilder added(ItemLike item) {
        this.addedInput = Ingredient.of(item);
        return this;
    }

    public AlchemyArrayRecipeBuilder added(Ingredient ingredient) {
        this.addedInput = ingredient;
        return this;
    }

    public AlchemyArrayRecipeBuilder texture(String path) {
        this.texture = NeoVitae.rl(path);
        return this;
    }

    public AlchemyArrayRecipeBuilder texture(Identifier texture) {
        this.texture = texture;
        return this;
    }

    public void save(RecipeOutput recipeOutput, Identifier id) {
        if (baseInput == null) {
            throw new IllegalStateException("AlchemyArrayRecipe requires a base input");
        }
        if (addedInput == null) {
            throw new IllegalStateException("AlchemyArrayRecipe requires an added input");
        }
        AlchemyArrayRecipe recipe = new AlchemyArrayRecipe(texture, baseInput, addedInput, output);
        recipeOutput.accept(ResourceKey.create(Registries.RECIPE, id), recipe, null);
    }
}
