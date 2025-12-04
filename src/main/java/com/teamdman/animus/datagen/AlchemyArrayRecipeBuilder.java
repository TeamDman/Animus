package com.teamdman.animus.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AlchemyArrayRecipeBuilder {
    private final Ingredient baseInput;
    private final Ingredient addedInput;
    private final ItemStack output;
    private final ResourceLocation texture;

    private AlchemyArrayRecipeBuilder(Ingredient baseInput, Ingredient addedInput, ItemStack output, ResourceLocation texture) {
        this.baseInput = baseInput;
        this.addedInput = addedInput;
        this.output = output;
        this.texture = texture;
    }

    public static AlchemyArrayRecipeBuilder array(Ingredient baseInput, Ingredient addedInput, ItemStack output, ResourceLocation texture) {
        return new AlchemyArrayRecipeBuilder(baseInput, addedInput, output, texture);
    }

    public static AlchemyArrayRecipeBuilder array(Ingredient baseInput, Ingredient addedInput, ItemStack output, String texture) {
        return array(baseInput, addedInput, output, ResourceLocation.parse(texture));
    }

    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, baseInput, addedInput, output, texture));
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final Ingredient baseInput;
        private final Ingredient addedInput;
        private final ItemStack output;
        private final ResourceLocation texture;

        public Result(ResourceLocation id, Ingredient baseInput, Ingredient addedInput, ItemStack output, ResourceLocation texture) {
            this.id = id;
            this.baseInput = baseInput;
            this.addedInput = addedInput;
            this.output = output;
            this.texture = texture;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("baseinput", baseInput.toJson());
            json.add("addedinput", addedInput.toJson());

            JsonObject outputJson = new JsonObject();
            outputJson.addProperty("item", output.getItem().builtInRegistryHolder().key().location().toString());
            if (output.getCount() > 1) {
                outputJson.addProperty("count", output.getCount());
            }
            json.add("output", outputJson);

            json.addProperty("texture", texture.toString());
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ForgeRegistries.RECIPE_SERIALIZERS.getValue(ResourceLocation.fromNamespaceAndPath("bloodmagic", "array"));
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
