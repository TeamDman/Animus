package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.compat.LivingUpgradeHelper;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.breakinblocks.neovitae.common.living.LivingHelper;

/**
 * Handles Living Armor integration with Iron's Spells
 *
 * Features:
 * - Grants Living Armor XP when spells are cast
 * - XP scales with spell level
 * - XP is granted to the Arcane Channeling upgrade tree
 */
public class LivingArmorSpellHandler {

    public static void register() {
        NeoForge.EVENT_BUS.register(new LivingArmorSpellHandler());
        Animus.LOGGER.debug("Registered Living Armor Spell Handler for Iron's Spells");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCast(SpellOnCastEvent event) {
        if (!AnimusConfig.ironsSpells.enableLivingArmorXP.get()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!LivingHelper.hasFullSet(player)) {
            return;
        }

        int spellLevel = event.getSpellLevel();

        int baseXP = AnimusConfig.ironsSpells.livingArmorBaseXP.get();
        float xpToGrant = baseXP * spellLevel;

        boolean success = LivingUpgradeHelper.addExperience(
            player,
            ArcaneChannelingHandler.UPGRADE_ID,
            xpToGrant
        );

        if (success) {
            Animus.LOGGER.debug("Granted {} XP to Living Armor (Arcane Channeling) for casting spell (level {})",
                xpToGrant, spellLevel);
        }
    }
}
