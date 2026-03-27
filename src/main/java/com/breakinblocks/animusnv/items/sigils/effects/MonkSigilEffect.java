package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.registry.AnimusSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;

/**
 * Sigil of the Monk - A toggleable sigil that enhances unarmed combat.
 * <p>
 * When active:
 * - Provides +10 unarmed damage (configurable via attribute modifier)
 * - Grants diamond-level mining speed when mining with empty hands
 * - EV is consumed per action (hit/mine), not passive drain
 * <p>
 * NOTE: For Curios integration, the item must implement ICurioItem.
 * The MonkSigilItem class handles the Curios-specific functionality.
 * This effect handles the toggleable behavior and EV consumption.
 */
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
        if (level.isClientSide) {
            return false;
        }

        // Play ninja_time sound at 50% volume when toggling on
        // Note: The toggle itself is handled by SigilItem, this is just for sound
        // We need to check if it WILL be activated (opposite of current state)
        // But since SigilItem handles the toggle, we can't easily know the state here
        // The sound will be handled by a custom item class or event handler

        return false; // Don't consume EV for toggling
    }

    @Override
    public void activeTick(Level level, Player player, ItemStack stack, int itemSlot, boolean isSelected) {
        // No passive EV drain - EV is consumed per action (hit/mine)
        // This is handled by event handlers that check for active Monk sigil
    }

    /**
     * Consume EV from the player's Anima for a Sigil of the Demon Monk action.
     *
     * @param player The player using the sigil
     * @param amount The amount of EV to consume
     * @return true if EV was successfully consumed, false if not enough EV
     */
    public static boolean consumeLP(Player player, int amount) {
        if (player.level().isClientSide()) {
            return false;
        }

        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (network == null) {
            return false;
        }

        if (network.getCurrentEV() < amount) {
            return false;
        }

        AnimaTicket ticket = AnimaTicket.create(amount);
        network.syphon(ticket);
        return true;
    }

    /**
     * Get the unarmed damage bonus from config.
     */
    public static double getUnarmedDamage() {
        return AnimusConfig.sigils.monkUnarmedDamage.get();
    }

    /**
     * Play the activation sound when the sigil is toggled on.
     */
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
