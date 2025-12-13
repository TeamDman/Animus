package com.teamdman.animus.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Clear Skies
 * Trigger: Glowstone Block on top of Imperfect Ritual Stone
 * Cost: 1000 LP
 * Effect: Clears the weather and stops rain/thunder
 */
public class ClearSkiesRitualRecipe extends ImperfectRitualRecipe {

    public ClearSkiesRitualRecipe() {
        super("clear_skies", Blocks.GLOWSTONE.defaultBlockState(), 1000);
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
        private static final MapCodec<ClearSkiesRitualRecipe> CODEC = MapCodec.unit(ClearSkiesRitualRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, ClearSkiesRitualRecipe> STREAM_CODEC =
            StreamCodec.unit(new ClearSkiesRitualRecipe());

        @Override
        public MapCodec<ClearSkiesRitualRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClearSkiesRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
