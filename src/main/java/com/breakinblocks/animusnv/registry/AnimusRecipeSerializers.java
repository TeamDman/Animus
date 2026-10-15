package com.breakinblocks.animusnv.registry;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;


public class AnimusRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.Mod.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, Constants.Mod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KeyUnbindingRecipe>> KEY_UNBINDING =
        RECIPE_SERIALIZERS.register("key_unbinding",
            () -> new SimpleCraftingRecipeSerializer<>(KeyUnbindingRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SanguineDynamoRecipe>> SANGUINE_DYNAMO =
        RECIPE_SERIALIZERS.register("sanguine_dynamo", SanguineDynamoRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<SanguineDynamoRecipe>> SANGUINE_DYNAMO_TYPE =
        RECIPE_TYPES.register("sanguine_dynamo",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "sanguine_dynamo")));

}
