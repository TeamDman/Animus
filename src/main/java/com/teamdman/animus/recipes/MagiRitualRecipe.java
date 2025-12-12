package com.teamdman.animus.recipes;

import com.google.gson.JsonObject;
import com.teamdman.animus.compat.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of the Magi
 * Trigger: Source Gem Block on top of Imperfect Ritual Stone (requires Ars Nouveau)
 * Cost: 2500 LP
 * Effect: Grants Mana Regen for 15 minutes
 */
public class MagiRitualRecipe extends ImperfectRitualRecipe {

    // 15 minutes in ticks (15 * 60 * 20)
    private static final int EFFECT_DURATION = 18000;

    private static final ResourceLocation SOURCE_GEM_BLOCK_ID = ResourceLocation.fromNamespaceAndPath("ars_nouveau", "source_gem_block");
    private static final ResourceLocation MANA_REGEN_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("ars_nouveau", "mana_regen");

    public MagiRitualRecipe(ResourceLocation id) {
        // Use air as placeholder - we override matches() to check for the actual Ars Nouveau block
        super(id, "magi", Blocks.AIR.defaultBlockState(), 2500);
    }

    @Override
    public boolean matches(BlockState state) {
        // Only match if Ars Nouveau is loaded and the block is source_gem_block
        if (!CompatHandler.isArsNouveauLoaded()) {
            return false;
        }

        Block sourceGemBlock = ForgeRegistries.BLOCKS.getValue(SOURCE_GEM_BLOCK_ID);
        if (sourceGemBlock == null || sourceGemBlock == Blocks.AIR) {
            return false;
        }

        return state.getBlock() == sourceGemBlock;
    }

    @Override
    public BlockState getTriggerBlock() {
        // Return the actual Ars Nouveau block if loaded, otherwise air
        if (CompatHandler.isArsNouveauLoaded()) {
            Block sourceGemBlock = ForgeRegistries.BLOCKS.getValue(SOURCE_GEM_BLOCK_ID);
            if (sourceGemBlock != null && sourceGemBlock != Blocks.AIR) {
                return sourceGemBlock.defaultBlockState();
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Double-check Ars Nouveau is loaded
        if (!CompatHandler.isArsNouveauLoaded()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.magi.requires_ars"),
                true
            );
            return false;
        }

        // Get Mana Regen effect from Ars Nouveau
        MobEffect manaRegenEffect = ForgeRegistries.MOB_EFFECTS.getValue(MANA_REGEN_EFFECT_ID);
        if (manaRegenEffect == null) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.magi.effect_not_found"),
                true
            );
            return false;
        }

        // Grant Mana Regen for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            manaRegenEffect,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Play a magical sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.2F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.magi.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.MAGI_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<MagiRitualRecipe> {
        @Override
        public MagiRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new MagiRitualRecipe(recipeId);
        }

        @Override
        public MagiRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new MagiRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MagiRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
