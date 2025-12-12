package com.teamdman.animus.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Boundless Skies
 * Trigger: Ancient Debris on top of Imperfect Ritual Stone
 * Cost: 10000 LP
 * Effect: Grants 15 minutes of Blood Magic flight effect
 */
public class BoundlessSkiesRitualRecipe extends ImperfectRitualRecipe {

    public BoundlessSkiesRitualRecipe(ResourceLocation id) {
        super(id, "boundless_skies", Blocks.ANCIENT_DEBRIS.defaultBlockState(), 10000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Get the Blood Magic flight effect
        var flightEffect = BuiltInRegistries.MOB_EFFECT.get(
            ResourceLocation.fromNamespaceAndPath("bloodmagic", "flight")
        );

        if (flightEffect == null) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.boundless_skies.no_effect"),
                true
            );
            return false;
        }

        // Apply 15 minutes (18000 ticks) of flight
        player.addEffect(new MobEffectInstance(
            flightEffect,
            18000, // 15 minutes
            0,     // Level 0
            false, // Not ambient
            true,  // Show particles
            true   // Show icon
        ));

        // Play success sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.ENDER_DRAGON_FLAP,
            SoundSource.BLOCKS,
            1.0F,
            1.2F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.boundless_skies.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.BOUNDLESS_SKIES_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<BoundlessSkiesRitualRecipe> {
        @Override
        public BoundlessSkiesRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new BoundlessSkiesRitualRecipe(recipeId);
        }

        @Override
        public BoundlessSkiesRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new BoundlessSkiesRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BoundlessSkiesRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
