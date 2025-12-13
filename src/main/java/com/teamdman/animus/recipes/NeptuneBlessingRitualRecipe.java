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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Neptune's Blessing
 * Trigger: Prismarine Block on top of Imperfect Ritual Stone
 * Cost: 2000 LP
 * Effect: Grants Water Breathing and Dolphin's Grace for 15 minutes
 */
public class NeptuneBlessingRitualRecipe extends ImperfectRitualRecipe {

    // 15 minutes in ticks (15 * 60 * 20)
    private static final int EFFECT_DURATION = 18000;

    public NeptuneBlessingRitualRecipe() {
        super("neptune_blessing", Blocks.PRISMARINE.defaultBlockState(), 2000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Grant Water Breathing for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            MobEffects.WATER_BREATHING,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Grant Dolphin's Grace for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            MobEffects.DOLPHINS_GRACE,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Play ocean ambient sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.neptune_blessing.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.NEPTUNE_BLESSING_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<NeptuneBlessingRitualRecipe> {
        private static final MapCodec<NeptuneBlessingRitualRecipe> CODEC = MapCodec.unit(NeptuneBlessingRitualRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, NeptuneBlessingRitualRecipe> STREAM_CODEC =
            StreamCodec.unit(new NeptuneBlessingRitualRecipe());

        @Override
        public MapCodec<NeptuneBlessingRitualRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NeptuneBlessingRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
