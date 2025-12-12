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
 * Imperfect Ritual of the Soul Stained Blood
 * Trigger: Block of Hallowed Gold on top of Imperfect Ritual Stone (requires Malum)
 * Cost: 2000 LP
 * Effect: Grants Gaia's Bulwark for 15 minutes
 */
public class SoulStainedBloodRitualRecipe extends ImperfectRitualRecipe {

    // 15 minutes in ticks (15 * 60 * 20)
    private static final int EFFECT_DURATION = 18000;

    private static final ResourceLocation HALLOWED_GOLD_BLOCK_ID = ResourceLocation.fromNamespaceAndPath("malum", "block_of_hallowed_gold");
    private static final ResourceLocation GAIAS_BULWARK_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("malum", "gaias_bulwark");

    public SoulStainedBloodRitualRecipe(ResourceLocation id) {
        // Use air as placeholder - we override matches() to check for the actual Malum block
        super(id, "soul_stained_blood", Blocks.AIR.defaultBlockState(), 2000);
    }

    @Override
    public boolean matches(BlockState state) {
        // Only match if Malum is loaded and the block is block_of_hallowed_gold
        if (!CompatHandler.isMalumLoaded()) {
            return false;
        }

        Block hallowedGoldBlock = ForgeRegistries.BLOCKS.getValue(HALLOWED_GOLD_BLOCK_ID);
        if (hallowedGoldBlock == null || hallowedGoldBlock == Blocks.AIR) {
            return false;
        }

        return state.getBlock() == hallowedGoldBlock;
    }

    @Override
    public BlockState getTriggerBlock() {
        // Return the actual Malum block if loaded, otherwise air
        if (CompatHandler.isMalumLoaded()) {
            Block hallowedGoldBlock = ForgeRegistries.BLOCKS.getValue(HALLOWED_GOLD_BLOCK_ID);
            if (hallowedGoldBlock != null && hallowedGoldBlock != Blocks.AIR) {
                return hallowedGoldBlock.defaultBlockState();
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Double-check Malum is loaded
        if (!CompatHandler.isMalumLoaded()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.soul_stained_blood.requires_malum"),
                true
            );
            return false;
        }

        // Get Gaia's Bulwark effect from Malum
        MobEffect gaiasBulwarkEffect = ForgeRegistries.MOB_EFFECTS.getValue(GAIAS_BULWARK_EFFECT_ID);
        if (gaiasBulwarkEffect == null) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.soul_stained_blood.effect_not_found"),
                true
            );
            return false;
        }

        // Grant Gaia's Bulwark for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            gaiasBulwarkEffect,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Play a soul-themed sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.SOUL_ESCAPE,
            SoundSource.BLOCKS,
            1.0F,
            0.8F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.soul_stained_blood.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.SOUL_STAINED_BLOOD_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<SoulStainedBloodRitualRecipe> {
        @Override
        public SoulStainedBloodRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new SoulStainedBloodRitualRecipe(recipeId);
        }

        @Override
        public SoulStainedBloodRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new SoulStainedBloodRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SoulStainedBloodRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
