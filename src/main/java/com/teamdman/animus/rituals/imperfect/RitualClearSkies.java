package com.teamdman.animus.rituals.imperfect;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import wayoftime.bloodmagic.ritual.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of Clear Skies
 * Requires: Glowstone Block on top of Imperfect Ritual Stone
 * Cost: 1000 LP
 * Effect: Clears the weather and stops rain/thunder
 */
public class RitualClearSkies extends ImperfectRitual {

    public RitualClearSkies() {
        super(
            Constants.Rituals.CLEAR_SKIES,
            state -> state.is(Blocks.GLOWSTONE),
            1000,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.CLEAR_SKIES
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            // Clear the weather
            serverLevel.setWeatherParameters(6000, 0, false, false);
        }

        // Play wind sound effect
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDER_DRAGON_FLAP,
            SoundSource.BLOCKS,
            0.5F,
            1.5F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.clear_skies.success"),
            true
        );

        return true;
    }
}
