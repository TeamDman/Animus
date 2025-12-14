package com.teamdman.animus.rituals.imperfect;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import wayoftime.bloodmagic.common.effect.BMMobEffects;
import wayoftime.bloodmagic.api.ritual.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of the Warden
 * Requires: Sculk Block on top of Imperfect Ritual Stone
 * Cost: 3000 LP
 * Effect: Grants Obsidian Cloak for 15 minutes
 */
public class RitualWarden extends ImperfectRitual {

    // 15 minutes in ticks (15 * 60 * 20)
    private static final int EFFECT_DURATION = 18000;

    public RitualWarden() {
        super(
            Constants.Rituals.WARDEN,
            state -> state.is(Blocks.SCULK),
            3000,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.WARDEN
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        // Grant Obsidian Cloak for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            BMMobEffects.OBSIDIAN_CLOAK,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Play Warden ambient sound
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_AMBIENT,
            SoundSource.BLOCKS,
            0.5F,
            1.0F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.warden.success"),
            true
        );

        return true;
    }
}
