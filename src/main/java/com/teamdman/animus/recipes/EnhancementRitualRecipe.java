package com.teamdman.animus.recipes;

import com.teamdman.animus.AnimusConfig;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Enhancement
 * Trigger: Amethyst Block on top of Imperfect Ritual Stone
 * Cost: 5000 LP
 * Effect: Enhances all enchantments on mainhand item by 1 level (one-time only)
 */
public class EnhancementRitualRecipe extends ImperfectRitualRecipe {

    public EnhancementRitualRecipe(ResourceLocation id) {
        super(id, "enhancement", Blocks.AMETHYST_BLOCK.defaultBlockState(), 5000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Check if ritual is enabled
        if (!AnimusConfig.imperfectRituals.enhancementEnabled.get()) {
            return false;
        }

        // Check if player is holding an item in mainhand
        ItemStack mainhandItem = player.getMainHandItem();
        if (mainhandItem.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.no_item"),
                true
            );
            return false;
        }

        // Check if item has already been enhanced
        CompoundTag tag = mainhandItem.getOrCreateTag();
        if (tag.getBoolean("AnimusEnhanced")) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.already_enhanced"),
                true
            );
            return false;
        }

        // Check if item has enchantments (required for enhancement to be meaningful)
        if (!mainhandItem.isEnchanted()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.no_enchantments"),
                true
            );
            return false;
        }

        // Mark as enhanced - the +1 level boost is applied dynamically via mixin
        tag.putBoolean("AnimusEnhanced", true);

        // Play success sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.5F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.enhancement.success"),
            true
        );

        return true;
    }


    @Override
    public int getLpCost() {
        return AnimusConfig.imperfectRituals.enhancementCost.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.ENHANCEMENT_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<EnhancementRitualRecipe> {
        @Override
        public EnhancementRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new EnhancementRitualRecipe(recipeId);
        }

        @Override
        public EnhancementRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new EnhancementRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, EnhancementRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
