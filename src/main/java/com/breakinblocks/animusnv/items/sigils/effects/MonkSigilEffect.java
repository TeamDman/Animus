package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.registry.AnimusSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;

public record MonkSigilEffect() implements ISigilEffect {
    public static final MapCodec<MonkSigilEffect> CODEC = MapCodec.unit(MonkSigilEffect::new);

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean isToggleable() {
        return true;
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return false;
        }

        return false;
    }

    @Override
    public void activeTick(Level level, Player player, ItemStack stack, int itemSlot, boolean isSelected) {
    }

    public static boolean consumeEV(Player player, ItemStack sigilStack, int amount) {
        if (player.level().isClientSide()) {
            return false;
        }
        return AnimusRitualHelper.drainEVForBoundItem(player, sigilStack, amount);
    }

    public static double getUnarmedDamage() {
        return AnimusConfig.sigils.monkUnarmedDamage.get();
    }

    public static void playActivationSound(Level level, Player player) {
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                AnimusSounds.NINJA_TIME.get(),
                SoundSource.PLAYERS,
                0.5f,
                1.0f
        );
    }
}
