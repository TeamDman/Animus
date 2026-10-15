package com.breakinblocks.animusnv.registry;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.recipes.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;


public class AnimusRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.Mod.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, Constants.Mod.MODID);

    private static final MapCodec<KeyUnbindingRecipe> KEY_UNBINDING_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(r -> CraftingBookCategory.MISC)
    ).apply(builder, KeyUnbindingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, KeyUnbindingRecipe> KEY_UNBINDING_STREAM_CODEC = StreamCodec.composite(
        CraftingBookCategory.STREAM_CODEC, r -> CraftingBookCategory.MISC,
        KeyUnbindingRecipe::new
    );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KeyUnbindingRecipe>> KEY_UNBINDING =
        RECIPE_SERIALIZERS.register("key_unbinding",
            () -> new RecipeSerializer<>(KEY_UNBINDING_CODEC, KEY_UNBINDING_STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SanguineDynamoRecipe>> SANGUINE_DYNAMO =
        RECIPE_SERIALIZERS.register("sanguine_dynamo",
            () -> new RecipeSerializer<>(SanguineDynamoRecipe.CODEC, SanguineDynamoRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeType<?>, RecipeType<SanguineDynamoRecipe>> SANGUINE_DYNAMO_TYPE =
        RECIPE_TYPES.register("sanguine_dynamo",
            () -> RecipeType.simple(Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "sanguine_dynamo")));

}
