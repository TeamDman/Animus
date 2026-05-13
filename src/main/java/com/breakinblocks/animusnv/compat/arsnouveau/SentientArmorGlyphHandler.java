package com.breakinblocks.animusnv.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.compat.SentientUpgradeHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.breakinblocks.neovitae.common.sentient.SentientHelper;

/**
 * Handles Sentient Armor integration with Ars Nouveau
 *
 * Features:
 * - Grants Sentient Armor XP when glyphs/spells are cast
 * - XP is granted to the Source Attunement upgrade tree
 */
public class SentientArmorGlyphHandler {

    public static void register() {
        NeoForge.EVENT_BUS.register(new SentientArmorGlyphHandler());
        Animus.LOGGER.debug("Registered Sentient Armor Glyph Handler for Ars Nouveau");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCast(SpellCastEvent event) {
        if (!AnimusConfig.arsNouveau.enableSentientArmorXP.get()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!SentientHelper.hasFullSet(player)) {
            return;
        }

        int baseXP = AnimusConfig.arsNouveau.sentientArmorBaseXP.get();

        boolean success = SentientUpgradeHelper.addExperience(
            player,
            SourceAttunementHandler.UPGRADE_ID,
            baseXP
        );

        if (success) {
            Animus.LOGGER.debug("Granted {} XP to Sentient Armor (Source Attunement) for casting Ars Nouveau spell",
                baseXP);
        }
    }
}
