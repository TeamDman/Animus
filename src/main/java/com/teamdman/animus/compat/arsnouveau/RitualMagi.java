package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of the Magi
 * Requires: Source Gem Block on top of Imperfect Ritual Stone (Ars Nouveau)
 * Cost: 2500 LP
 * Effect: Grants Mana Regen III for 15 minutes
 */
public class RitualMagi extends ImperfectRitual {

    private static final int EFFECT_DURATION = 18000; // 15 minutes

    public RitualMagi() {
        super(
            Constants.Rituals.MAGI,
            state -> state.is(BlockRegistry.SOURCE_GEM_BLOCK.get()),
            2500,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.MAGI
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        player.addEffect(new MobEffectInstance(
            ModPotions.MANA_REGEN_EFFECT,
            EFFECT_DURATION,
            2,
            false,
            true,
            true
        ));

        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.magi.success"),
            true
        );

        return true;
    }
}
