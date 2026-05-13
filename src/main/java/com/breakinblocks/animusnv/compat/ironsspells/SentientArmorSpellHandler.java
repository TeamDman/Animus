package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.compat.SentientUpgradeHelper;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.breakinblocks.neovitae.common.sentient.SentientHelper;

/**
 * Handles Sentient Armor integration with Iron's Spells
 *
 * Features:
 * - Grants Sentient Armor XP when spells are cast
 * - XP scales with spell level
 * - XP is granted to the Arcane Channeling upgrade tree
 */
public class SentientArmorSpellHandler {

    public static void register() {
        NeoForge.EVENT_BUS.register(new SentientArmorSpellHandler());
        Animus.LOGGER.debug("Registered Sentient Armor Spell Handler for Iron's Spells");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCast(SpellOnCastEvent event) {
        if (!AnimusConfig.ironsSpells.enableSentientArmorXP.get()) {
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

        int spellLevel = event.getSpellLevel();

        int baseXP = AnimusConfig.ironsSpells.sentientArmorBaseXP.get();
        float xpToGrant = baseXP * spellLevel;

        boolean success = SentientUpgradeHelper.addExperience(
            player,
            ArcaneChannelingHandler.UPGRADE_ID,
            xpToGrant
        );

        if (success) {
            Animus.LOGGER.debug("Granted {} XP to Sentient Armor (Arcane Channeling) for casting spell (level {})",
                xpToGrant, spellLevel);
        }
    }
}
