package com.teamdman.animus.recipes;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.compat.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of the Manasteel Soul
 * Trigger: Manasteel Block on top of Imperfect Ritual Stone (requires Botania)
 * Cost: 2500 LP
 * Effect: Grants Emptiness (from Botania) and Speed for 15 minutes, no particles
 */
public class ManasteelSoulRitualRecipe extends ImperfectRitualRecipe {

    // 15 minutes in ticks (15 * 60 * 20)
    private static final int EFFECT_DURATION = 18000;

    private static final ResourceLocation MANASTEEL_BLOCK_ID = ResourceLocation.fromNamespaceAndPath("botania", "manasteel_block");
    private static final ResourceLocation EMPTINESS_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("botania", "emptiness");

    public ManasteelSoulRitualRecipe() {
        // Use air as placeholder - we override matches() to check for the actual Botania block
        super("manasteel_soul", Blocks.AIR.defaultBlockState(), 2500);
    }

    @Override
    public boolean matches(BlockState state) {
        // Only match if Botania is loaded and the block is manasteel_block
        if (!CompatHandler.isBotaniaLoaded()) {
            return false;
        }

        Block manasteelBlock = BuiltInRegistries.BLOCK.getOptional(MANASTEEL_BLOCK_ID).orElse(null);
        if (manasteelBlock == null || manasteelBlock == Blocks.AIR) {
            return false;
        }

        return state.getBlock() == manasteelBlock;
    }

    @Override
    public BlockState getTriggerBlock() {
        // Return the actual Botania block if loaded, otherwise air
        if (CompatHandler.isBotaniaLoaded()) {
            Block manasteelBlock = BuiltInRegistries.BLOCK.getOptional(MANASTEEL_BLOCK_ID).orElse(null);
            if (manasteelBlock != null && manasteelBlock != Blocks.AIR) {
                return manasteelBlock.defaultBlockState();
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Double-check Botania is loaded
        if (!CompatHandler.isBotaniaLoaded()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.manasteel_soul.requires_botania"),
                true
            );
            return false;
        }

        // Get the Emptiness effect from Botania
        Holder<MobEffect> emptinessEffect = BuiltInRegistries.MOB_EFFECT.getHolder(EMPTINESS_EFFECT_ID)
            .orElse(null);

        if (emptinessEffect == null) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.manasteel_soul.effect_not_found"),
                true
            );
            return false;
        }

        // Grant Emptiness for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            emptinessEffect,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Grant Speed for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SPEED,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Play mystical sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.BEACON_ACTIVATE,
            SoundSource.BLOCKS,
            1.0F,
            1.5F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.manasteel_soul.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.MANASTEEL_SOUL_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<ManasteelSoulRitualRecipe> {
        private static final MapCodec<ManasteelSoulRitualRecipe> CODEC = MapCodec.unit(ManasteelSoulRitualRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, ManasteelSoulRitualRecipe> STREAM_CODEC =
            StreamCodec.unit(new ManasteelSoulRitualRecipe());

        @Override
        public MapCodec<ManasteelSoulRitualRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ManasteelSoulRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
