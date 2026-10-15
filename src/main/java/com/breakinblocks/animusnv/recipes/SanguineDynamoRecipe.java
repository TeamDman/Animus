package com.breakinblocks.animusnv.recipes;

import com.breakinblocks.animusnv.registry.AnimusRecipeSerializers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public record SanguineDynamoRecipe(Ingredient orb, int fePerEv, int maxEvPerTick) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<SanguineDynamoRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("orb").forGetter(SanguineDynamoRecipe::orb),
            ExtraCodecs.POSITIVE_INT.fieldOf("fe_per_ev").forGetter(SanguineDynamoRecipe::fePerEv),
            ExtraCodecs.POSITIVE_INT.fieldOf("max_ev_per_tick").forGetter(SanguineDynamoRecipe::maxEvPerTick)
    ).apply(instance, SanguineDynamoRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SanguineDynamoRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, SanguineDynamoRecipe::orb,
            ByteBufCodecs.VAR_INT, SanguineDynamoRecipe::fePerEv,
            ByteBufCodecs.VAR_INT, SanguineDynamoRecipe::maxEvPerTick,
            SanguineDynamoRecipe::new);

    public int maxFePerTick() {
        return fePerEv * maxEvPerTick;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return orb.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, orb);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.SANGUINE_DYNAMO.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AnimusRecipeSerializers.SANGUINE_DYNAMO_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<SanguineDynamoRecipe> {
        @Override
        public MapCodec<SanguineDynamoRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SanguineDynamoRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
