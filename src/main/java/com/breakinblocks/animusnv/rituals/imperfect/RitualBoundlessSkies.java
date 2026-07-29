package com.breakinblocks.animusnv.rituals.imperfect;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone;
import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of Boundless Skies
 * Requires: Ancient Debris on top of Imperfect Ritual Stone
 * Cost: 10000 EV
 * Effect: Grants 15 minutes of NeoVitae flight effect
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

        if (level.isClientSide()) {
            return false;
        }

        player.addEffect(new MobEffectInstance(
            NVMobEffects.FLIGHT,
            18000,
            0,
            false,
            true,
            true
        ));

        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDER_DRAGON_FLAP,
            SoundSource.BLOCKS,
            1.0F,
            1.2F
        );

        player.sendOverlayMessage(
            Component.translatable("ritual.animusnv.boundless_skies.success"));

        return true;
    }
}
