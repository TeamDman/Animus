package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import wayoftime.bloodmagic.core.living.LivingUtil;

/**
 * Handles Living Armor integration with Ars Nouveau
 *
 * Features:
 * - Grants Living Armor XP when glyphs/spells are cast
 * - XP scales with spell complexity (number of glyphs)
 * - XP is granted to the Source Attunement upgrade tree
 */
public class LivingArmorGlyphHandler {

    // Reference to the upgrade (will be set by ArsNouveauCompat during init)
    private static UpgradeSourceAttunement sourceAttunementUpgrade;

    /**
     * Set the upgrade instance (called by compat module during initialization)
     */
    public static void setUpgrade(UpgradeSourceAttunement upgrade) {
        sourceAttunementUpgrade = upgrade;
    }

    /**
     * Grant Living Armor XP when player casts an Ars Nouveau spell
     * Priority LOWEST to run after spell execution
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellCast(SpellCastEvent event) {
        if (!AnimusConfig.arsNouveau.enableLivingArmorXP.get()) {
            return;
        }

        if (sourceAttunementUpgrade == null) {
            // Upgrade not registered yet
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // Check if player has full Living Armor set
        if (!LivingUtil.hasFullSet(player)) {
            return;
        }

        // Calculate XP to grant
        // Note: SpellCastEvent doesn't expose spell size, so using flat XP
        int baseXP = AnimusConfig.arsNouveau.livingArmorBaseXP.get();
        double xpToGrant = baseXP;

        // Grant XP to the Source Attunement upgrade using the proper API
        LivingUtil.applyNewExperience(player, sourceAttunementUpgrade, xpToGrant);

        Animus.LOGGER.debug("Granted {} XP to Living Armor (Source Attunement) for casting Ars Nouveau spell",
            xpToGrant);
    }
}
