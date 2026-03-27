package com.breakinblocks.animusnv.datagen;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.recipe.alchemyarray.AlchemyArrayRecipe;

public class AlchemyArrayRecipeBuilder {
    private final ItemStack output;
    private Ingredient baseInput;
    private Ingredient addedInput;
    private ResourceLocation texture;

    private AlchemyArrayRecipeBuilder(ItemStack output) {
        if (output == null || output.isEmpty()) {
            throw new IllegalArgumentException("AlchemyArrayRecipe output cannot be null or empty");
        }
        this.output = output;
        this.texture = NeoVitae.rl("textures/models/alchemyarrays/sigil.png");
    }

    public static AlchemyArrayRecipeBuilder build(ItemLike output) {
        return new AlchemyArrayRecipeBuilder(new ItemStack(output));
    }

    public static AlchemyArrayRecipeBuilder build(ItemStack output) {
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

    public AlchemyArrayRecipeBuilder texture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        if (baseInput == null) {
            throw new IllegalStateException("AlchemyArrayRecipe requires a base input");
        }
        if (addedInput == null) {
            throw new IllegalStateException("AlchemyArrayRecipe requires an added input");
        }
        AlchemyArrayRecipe recipe = new AlchemyArrayRecipe(texture, baseInput, addedInput, output);
        recipeOutput.accept(id, recipe, null);
    }
}
