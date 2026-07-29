package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.Constants;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of the Iron Heart
 * Requires: Arcane Anvil on top of Imperfect Ritual Stone (Iron's Spells)
 * Cost: 2500 EV
 * Effect: Grants Echoing Strikes III for 15 minutes
 */
public class RitualIronHeart extends ImperfectRitual {

    private static final int EFFECT_DURATION = 18000; // 15 minutes

    public RitualIronHeart() {
        super(
            Constants.Rituals.IRON_HEART,
            state -> state.is(BlockRegistry.ARCANE_ANVIL_BLOCK.get()),
            2500,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.IRON_HEART
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide()) {
            return false;
        }

        player.addEffect(new MobEffectInstance(
            MobEffectRegistry.ECHOING_STRIKES,
            EFFECT_DURATION,
            2,
            false,
            true,
            true
        ));

        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.ANVIL_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        player.sendOverlayMessage(
            Component.translatable("ritual.animusnv.iron_heart.success"));

        return true;
    }
}
