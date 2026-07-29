package com.breakinblocks.animusnv.rituals.imperfect;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of the Warden
 * Requires: Sculk Block on top of Imperfect Ritual Stone
 * Cost: 3000 EV
 * Effect: Grants Obsidian Cloak for 15 minutes
 */
public class RitualWarden extends ImperfectRitual {

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

        if (level.isClientSide()) {
            return false;
        }

        player.addEffect(new MobEffectInstance(
            NVMobEffects.OBSIDIAN_CLOAK,
            EFFECT_DURATION,
            0,
            false,
            false,
            true
        ));

        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_AMBIENT,
            SoundSource.BLOCKS,
            0.5F,
            1.0F
        );

        player.sendOverlayMessage(
            Component.translatable("ritual.animusnv.warden.success"));

        return true;
    }
}
