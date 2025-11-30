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
 * Imperfect Ritual of Reduction
 * Trigger: Quartz Block on top of Imperfect Ritual Stone
 * Cost: 1000 LP
 * Effect: Removes enhancement record and downgrades all enchantments by 1 level (min level 1)
 */
public class ReductionRitualRecipe extends ImperfectRitualRecipe {

    public ReductionRitualRecipe(ResourceLocation id) {
        super(id, "reduction", Blocks.QUARTZ_BLOCK.defaultBlockState(), 1000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Check if player is holding an item in mainhand
        ItemStack mainhandItem = player.getMainHandItem();
        if (mainhandItem.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.reduction.no_item"),
                true
            );
            return false;
        }

        // Get enchantments
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(mainhandItem);
        if (enchantments.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.reduction.no_enchantments"),
                true
            );
            return false;
        }

        // Downgrade all enchantments by 1 level (minimum level 1)
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            int newLevel = Math.max(1, entry.getValue() - 1);
            enchantments.put(entry.getKey(), newLevel);
        }

        // Apply downgraded enchantments
        EnchantmentHelper.setEnchantments(enchantments, mainhandItem);

        // Remove enhanced marker if present
        CompoundTag tag = mainhandItem.getOrCreateTag();
        if (tag.contains("AnimusEnhanced")) {
            tag.remove("AnimusEnhanced");
        }

        // Play success sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.GRINDSTONE_USE,
            SoundSource.BLOCKS,
            1.0F,
            0.8F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.reduction.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.REDUCTION_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<ReductionRitualRecipe> {
        @Override
        public ReductionRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new ReductionRitualRecipe(recipeId);
        }

        @Override
        public ReductionRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new ReductionRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ReductionRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
