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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Regression
 * Trigger: Bookshelf on top of Imperfect Ritual Stone
 * Cost: 3000 LP
 * Effect: Removes the repair cost from the held item (anvil penalty reset)
 */
public class RegressionRitualRecipe extends ImperfectRitualRecipe {

    public RegressionRitualRecipe(ResourceLocation id) {
        super(id, "regression", Blocks.BOOKSHELF.defaultBlockState(), 3000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Check if player is holding an item
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.regression.no_item"),
                true
            );
            return false;
        }

        // Remove repair cost from item
        CompoundTag tag = heldItem.getOrCreateTag();
        if (!tag.contains("RepairCost")) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.regression.no_cost"),
                true
            );
            return false;
        }

        tag.remove("RepairCost");

        // Play success sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.regression.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.REGRESSION_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<RegressionRitualRecipe> {
        @Override
        public RegressionRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new RegressionRitualRecipe(recipeId);
        }

        @Override
        public RegressionRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new RegressionRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RegressionRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
