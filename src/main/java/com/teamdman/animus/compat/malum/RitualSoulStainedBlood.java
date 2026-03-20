package com.teamdman.animus.compat.malum;

import com.teamdman.animus.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of the Soul-Stained Blood
 * Requires: Block of Hallowed Gold on top of Imperfect Ritual Stone (Malum)
 * Cost: 3000 LP
 * Effect: Grants Stone Ward for 15 minutes
 *
 * Lore: The sacred gold, hallowed by spirits, resonates with the blood magic
 * ritual stone, surrounding the caster in an oaken carapace that increases defenses.
 */
public class RitualSoulStainedBlood extends ImperfectRitual {

    private static final int EFFECT_DURATION = 18000; // 15 minutes
    private static final ResourceLocation HALLOWED_GOLD_BLOCK = ResourceLocation.fromNamespaceAndPath("malum", "block_of_hallowed_gold");
    private static final ResourceLocation STONE_WARD = ResourceLocation.fromNamespaceAndPath("malum", "stone_ward");

    public RitualSoulStainedBlood() {
        super(
            Constants.Rituals.SOUL_STAINED_BLOOD,
            state -> {
                Block block = BuiltInRegistries.BLOCK.get(HALLOWED_GOLD_BLOCK);
                return state.is(block);
            },
            3000,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SOUL_STAINED_BLOOD
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(STONE_WARD)
            .orElse(null);

        if (effect == null) {
            // Send to chat (false) so it doesn't get overwritten by NeoVitae's action bar message
            player.displayClientMessage(
                Component.translatable("ritual.animus.soul_stained_blood.no_effect"),
                false
            );
            return false;
        }

        player.addEffect(new MobEffectInstance(
            effect,
            EFFECT_DURATION,
            0,
            false,
            true,
            true
        ));

        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.SOUL_ESCAPE,
            SoundSource.BLOCKS,
            1.0F,
            0.8F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.soul_stained_blood.success"),
            true
        );

        return true;
    }
}
