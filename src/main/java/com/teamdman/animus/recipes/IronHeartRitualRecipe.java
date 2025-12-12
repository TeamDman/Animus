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
 * Imperfect Ritual of the Iron Heart
 * Trigger: Arcane Anvil on top of Imperfect Ritual Stone (requires Iron's Spellbooks)
 * Cost: 3500 LP
 * Effect: Grants Echoing Strikes III for 15 minutes
 */
public class IronHeartRitualRecipe extends ImperfectRitualRecipe {

    // 15 minutes in ticks (15 * 60 * 20)
    private static final int EFFECT_DURATION = 18000;

    private static final ResourceLocation ARCANE_ANVIL_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "arcane_anvil");
    private static final ResourceLocation ECHOING_STRIKES_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "echoing_strikes");

    public IronHeartRitualRecipe(ResourceLocation id) {
        // Use air as placeholder - we override matches() to check for the actual Iron's Spellbooks block
        super(id, "iron_heart", Blocks.AIR.defaultBlockState(), 3500);
    }

    @Override
    public boolean matches(BlockState state) {
        // Only match if Iron's Spellbooks is loaded and the block is arcane_anvil
        if (!CompatHandler.isIronsSpellsLoaded()) {
            return false;
        }

        Block arcaneAnvil = ForgeRegistries.BLOCKS.getValue(ARCANE_ANVIL_ID);
        if (arcaneAnvil == null || arcaneAnvil == Blocks.AIR) {
            return false;
        }

        return state.getBlock() == arcaneAnvil;
    }

    @Override
    public BlockState getTriggerBlock() {
        // Return the actual Iron's Spellbooks block if loaded, otherwise air
        if (CompatHandler.isIronsSpellsLoaded()) {
            Block arcaneAnvil = ForgeRegistries.BLOCKS.getValue(ARCANE_ANVIL_ID);
            if (arcaneAnvil != null && arcaneAnvil != Blocks.AIR) {
                return arcaneAnvil.defaultBlockState();
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Double-check Iron's Spellbooks is loaded
        if (!CompatHandler.isIronsSpellsLoaded()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.iron_heart.requires_irons"),
                true
            );
            return false;
        }

        // Get Echoing Strikes effect from Iron's Spellbooks
        MobEffect echoingStrikesEffect = ForgeRegistries.MOB_EFFECTS.getValue(ECHOING_STRIKES_EFFECT_ID);
        if (echoingStrikesEffect == null) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.iron_heart.effect_not_found"),
                true
            );
            return false;
        }

        // Grant Echoing Strikes III for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            echoingStrikesEffect,
            EFFECT_DURATION,
            2,  // Amplifier 2 = level 3
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Play an anvil sound
        level.playSound(
            null,
            stonePos,
            SoundEvents.ANVIL_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.iron_heart.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.IRON_HEART_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<IronHeartRitualRecipe> {
        @Override
        public IronHeartRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new IronHeartRitualRecipe(recipeId);
        }

        @Override
        public IronHeartRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new IronHeartRitualRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, IronHeartRitualRecipe recipe) {
            // Nothing to write - all values are hardcoded
        }
    }
}
