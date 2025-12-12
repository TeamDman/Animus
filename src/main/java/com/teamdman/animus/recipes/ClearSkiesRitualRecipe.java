package com.teamdman.animus.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Clear Skies
 * Trigger: Glowstone Block on top of Imperfect Ritual Stone
 * Cost: 1000 LP
 * Effect: Clears the weather and stops rain/thunder
 */
public class ClearSkiesRitualRecipe extends ImperfectRitualRecipe {

    public ClearSkiesRitualRecipe(ResourceLocation id) {
        super(id, "clear_skies", Blocks.GLOWSTONE.defaultBlockState(), 1000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Clear the weather
        level.setWeatherParameters(6000, 0, false, false);

        // Play wind sound effect
        level.playSound(
            null,
            stonePos,
            SoundEvents.ENDER_DRAGON_FLAP,
            SoundSource.BLOCKS,
            0.5F,
            1.5F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.clear_skies.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.CLEAR_SKIES_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<ClearSkiesRitualRecipe> {
        @Override
        public ClearSkiesRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new ClearSkiesRitualRecipe(recipeId);
        }

        @Override
        public ClearSkiesRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new ClearSkiesRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ClearSkiesRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
