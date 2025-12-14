package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.LivingUpgradeHelper;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import wayoftime.bloodmagic.common.living.LivingHelper;

/**
 * Handles Living Armor integration with Iron's Spells
 *
 * Features:
 * - Grants Living Armor XP when spells are cast
 * - XP scales with spell level
 * - XP is granted to the Arcane Channeling upgrade tree
 */
public class LivingArmorSpellHandler {

    /**
     * Register the event handler
     */
    public static void register() {
        NeoForge.EVENT_BUS.register(new LivingArmorSpellHandler());
        Animus.LOGGER.info("Registered Living Armor Spell Handler for Iron's Spells");
    }

    /**
     * Grant Living Armor XP when player casts a spell
     * Priority LOWEST to run after spell execution
     */
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

        // Check if player has full Living Armor set
        if (!LivingHelper.hasFullSet(player)) {
            return;
        }

        // Get spell level
        int spellLevel = event.getSpellLevel();

        // Calculate XP to grant
        int baseXP = AnimusConfig.ironsSpells.livingArmorBaseXP.get();
        float xpToGrant = baseXP * spellLevel;

        // Grant XP to the Arcane Channeling upgrade using the data-pack based system
        boolean success = LivingUpgradeHelper.addExperience(
            player,
            ArcaneChannelingHandler.UPGRADE_KEY,
            xpToGrant
        );

        if (success) {
            Animus.LOGGER.debug("Granted {} XP to Living Armor (Arcane Channeling) for casting spell (level {})",
                xpToGrant, spellLevel);
        }
    }
}
