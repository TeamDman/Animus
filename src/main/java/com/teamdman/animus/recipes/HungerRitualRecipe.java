package com.teamdman.animus.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import com.teamdman.animus.registry.AnimusRecipeSerializers;

/**
 * Imperfect Ritual of Hunger
 * Trigger: Bone Block on top of Imperfect Ritual Stone
 * Cost: 500 LP
 * Effect: Sets player's food level to 1 and saturation to 10 (makes player very hungry)
 */
public class HungerRitualRecipe extends ImperfectRitualRecipe {

    public HungerRitualRecipe() {
        super("hunger", Blocks.BONE_BLOCK.defaultBlockState(), 500);
    }

    @Override
    public boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player) {
        // Set player to very hungry
        player.getFoodData().setFoodLevel(1);
        player.getFoodData().setSaturation(10.0F);

        // Play sound effect
        level.playSound(
            null,
            stonePos,
            SoundEvents.FIRE_EXTINGUISH,
            SoundSource.BLOCKS,
            0.5F,
            2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.hunger.success"),
            true
        );

        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.HUNGER_RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<HungerRitualRecipe> {
        private static final MapCodec<HungerRitualRecipe> CODEC = MapCodec.unit(HungerRitualRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, HungerRitualRecipe> STREAM_CODEC =
            StreamCodec.unit(new HungerRitualRecipe());

        @Override
        public MapCodec<HungerRitualRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HungerRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
