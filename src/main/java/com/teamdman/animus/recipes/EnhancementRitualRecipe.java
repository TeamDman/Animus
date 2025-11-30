package com.teamdman.animus.recipes;

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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

import java.util.Map;

/**
 * Imperfect Ritual of Enhancement
 * Trigger: Amethyst Block on top of Imperfect Ritual Stone
 * Cost: 5000 LP
 * Effect: Enhances all enchantments on offhand item by 1 level (one-time only)
 */
public class EnhancementRitualRecipe extends ImperfectRitualRecipe {

    public EnhancementRitualRecipe(ResourceLocation id) {
        super(id, "enhancement", Blocks.AMETHYST_BLOCK.defaultBlockState(), 5000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Check if player is holding an item in offhand
        ItemStack offhandItem = player.getOffhandItem();
        if (offhandItem.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.no_item"),
                true
            );
            return false;
        }

        // Check if item has already been enhanced
        CompoundTag tag = offhandItem.getOrCreateTag();
        if (tag.getBoolean("AnimusEnhanced")) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.already_enhanced"),
                true
            );
            return false;
        }

        // Get enchantments
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(offhandItem);
        if (enchantments.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.no_enchantments"),
                true
            );
            return false;
        }

        // Enhance all enchantments by 1 level
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            enchantments.put(entry.getKey(), entry.getValue() + 1);
        }

        // Apply enhanced enchantments
        EnchantmentHelper.setEnchantments(enchantments, offhandItem);

        // Mark as enhanced
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
