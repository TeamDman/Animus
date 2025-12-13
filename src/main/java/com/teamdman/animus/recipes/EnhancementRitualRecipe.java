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
 * Imperfect Ritual of Enhancement
 * Trigger: Amethyst Block on top of Imperfect Ritual Stone
 * Cost: 5000 LP
 * Effect: Enhances all enchantments on mainhand item by 1 level (one-time only)
 */
public class EnhancementRitualRecipe extends ImperfectRitualRecipe {

    public EnhancementRitualRecipe() {
        super("enhancement", Blocks.AMETHYST_BLOCK.defaultBlockState(), 5000);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Check if player is holding an item in mainhand
        ItemStack mainhandItem = player.getMainHandItem();
        if (mainhandItem.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.no_item"),
                true
            );
            return false;
        }

        // Check if item has already been enhanced using data component
        Boolean enhanced = mainhandItem.get(AnimusDataComponents.ANIMUS_ENHANCED.get());
        if (enhanced != null && enhanced) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.already_enhanced"),
                true
            );
            return false;
        }

        // Get enchantments using 1.21 API
        ItemEnchantments enchantments = mainhandItem.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.no_enchantments"),
                true
            );
            return false;
        }

        // Build new enchantments with +1 level each
        ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(enchantments);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            int currentLevel = enchantments.getLevel(enchantment);
            mutableEnchantments.set(enchantment, currentLevel + 1);
        }

        // Apply enhanced enchantments
        mainhandItem.set(DataComponents.ENCHANTMENTS, mutableEnchantments.toImmutable());

        // Mark as enhanced
        mainhandItem.set(AnimusDataComponents.ANIMUS_ENHANCED.get(), true);

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
        private static final MapCodec<EnhancementRitualRecipe> CODEC = MapCodec.unit(EnhancementRitualRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, EnhancementRitualRecipe> STREAM_CODEC =
            StreamCodec.unit(new EnhancementRitualRecipe());

        @Override
        public MapCodec<EnhancementRitualRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EnhancementRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
