package com.teamdman.animus.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Regression
 * Trigger: Bookshelf on top of Imperfect Ritual Stone
 * Cost: 3000 LP
 * Effect: Removes the repair cost from the held item (anvil penalty reset)
 */
public class RegressionRitualRecipe extends ImperfectRitualRecipe {

    public RegressionRitualRecipe() {
        super("regression", Blocks.BOOKSHELF.defaultBlockState(), 3000);
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

        // Check if item has repair cost
        Integer repairCost = heldItem.get(DataComponents.REPAIR_COST);
        if (repairCost == null || repairCost <= 0) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.regression.no_cost"),
                true
            );
            return false;
        }

        // Remove repair cost
        heldItem.remove(DataComponents.REPAIR_COST);

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
        private static final MapCodec<RegressionRitualRecipe> CODEC = MapCodec.unit(RegressionRitualRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, RegressionRitualRecipe> STREAM_CODEC =
            StreamCodec.unit(new RegressionRitualRecipe());

        @Override
        public MapCodec<RegressionRitualRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RegressionRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
