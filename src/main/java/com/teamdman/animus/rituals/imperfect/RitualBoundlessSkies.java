package com.teamdman.animus.rituals.imperfect;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import wayoftime.bloodmagic.api.ritual.IImperfectRitualStone;
import wayoftime.bloodmagic.common.effect.BMMobEffects;
import wayoftime.bloodmagic.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of Boundless Skies
 * Requires: Ancient Debris on top of Imperfect Ritual Stone
 * Cost: 10000 LP
 * Effect: Grants 15 minutes of Blood Magic flight effect
 */
public class RitualBoundlessSkies extends ImperfectRitual {

    public RitualBoundlessSkies() {
        super(
            Constants.Rituals.BOUNDLESS_SKIES,
            state -> state.is(Blocks.ANCIENT_DEBRIS),
            10000,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.BOUNDLESS_SKIES
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        // Apply 15 minutes (18000 ticks) of Blood Magic flight effect
        player.addEffect(new MobEffectInstance(
            BMMobEffects.FLIGHT,
            18000, // 15 minutes
            0,     // Level 0
            false, // Not ambient
            true,  // Show particles
            true   // Show icon
        ));

        // Play success sound
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDER_DRAGON_FLAP,
            SoundSource.BLOCKS,
            1.0F,
            1.2F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.boundless_skies.success"),
            true
        );

        return true;
    }
}
