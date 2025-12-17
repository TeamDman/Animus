package com.teamdman.animus.rituals.imperfect;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of Neptune's Blessing
 * Requires: Prismarine Block on top of Imperfect Ritual Stone
 * Cost: 2000 LP
 * Effect: Grants Water Breathing and Dolphin's Grace for 15 minutes
 */
public class RitualNeptuneBlessing extends ImperfectRitual {

    // 15 minutes in ticks (15 * 60 * 20)
    private static final int EFFECT_DURATION = 18000;

    public RitualNeptuneBlessing() {
        super(
            Constants.Rituals.NEPTUNE_BLESSING,
            state -> state.is(Blocks.PRISMARINE),
            2000,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.NEPTUNE_BLESSING
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        // Grant Water Breathing for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            MobEffects.WATER_BREATHING,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Grant Dolphin's Grace for 15 minutes (no particles)
        player.addEffect(new MobEffectInstance(
            MobEffects.DOLPHINS_GRACE,
            EFFECT_DURATION,
            0,  // Amplifier 0 = level 1
            false,  // Not ambient
            false,  // No particles
            true    // Show icon
        ));

        // Play ocean ambient sound
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.neptune_blessing.success"),
            true
        );

        return true;
    }
}
