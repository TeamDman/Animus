package com.teamdman.animus.recipes;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Reduction
 * Trigger: Quartz Block on top of Imperfect Ritual Stone
 * Cost: 1000 LP
 * Effect: Removes enhancement record and downgrades all enchantments by 1 level (min level 1)
 */
public class ReductionRitualRecipe extends ImperfectRitualRecipe {

    public ReductionRitualRecipe() {
        super("reduction", Blocks.QUARTZ_BLOCK.defaultBlockState(), 1000);
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

        // Get enchantments using 1.21 API
        ItemEnchantments enchantments = mainhandItem.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.reduction.no_enchantments"),
                true
            );
            return false;
        }

        // Downgrade all enchantments by 1 level (minimum level 1)
        ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(enchantments);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            int currentLevel = enchantments.getLevel(enchantment);
            int newLevel = Math.max(1, currentLevel - 1);
            mutableEnchantments.set(enchantment, newLevel);
        }

        // Apply downgraded enchantments
        mainhandItem.set(DataComponents.ENCHANTMENTS, mutableEnchantments.toImmutable());

        // Remove enhanced marker if present
        mainhandItem.remove(AnimusDataComponents.ANIMUS_ENHANCED.get());

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
        private static final MapCodec<ReductionRitualRecipe> CODEC = MapCodec.unit(ReductionRitualRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, ReductionRitualRecipe> STREAM_CODEC =
            StreamCodec.unit(new ReductionRitualRecipe());

        @Override
        public MapCodec<ReductionRitualRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ReductionRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
