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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import wayoftime.bloodmagic.potion.BloodMagicPotions;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of the Warden
 * Trigger: Sculk Block on top of Imperfect Ritual Stone
 * Cost: 3000 LP
 * Effect: Grants Obsidian Cloak for 15 minutes
 */
public class WardenRitualRecipe extends ImperfectRitualRecipe {

    // 15 minutes in ticks (15 * 60 * 20)
    private static final int EFFECT_DURATION = 18000;

    public WardenRitualRecipe(ResourceLocation id) {
        super(id, "warden", Blocks.SCULK.defaultBlockState(), 3000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Grant Obsidian Cloak for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            BloodMagicPotions.OBSIDIAN_CLOAK.get(),
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Play Warden ambient sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.WARDEN_AMBIENT,
            SoundSource.BLOCKS,
            0.5F,
            1.0F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.warden.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.WARDEN_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<WardenRitualRecipe> {
        @Override
        public WardenRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new WardenRitualRecipe(recipeId);
        }

        @Override
        public WardenRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new WardenRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WardenRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
