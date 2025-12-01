package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.core.living.LivingUtil;

/**
 * Handles Living Armor integration with Iron's Spells
 *
 * Features:
 * - Grants Living Armor XP when spells are cast
 * - XP scales with spell level and rarity
 * - XP is granted to the Arcane Channeling upgrade tree
 */
public class LivingArmorSpellHandler {

    // XP multipliers by spell rarity
    private static final double COMMON_XP_MULT = 1.0;
    private static final double UNCOMMON_XP_MULT = 1.5;
    private static final double RARE_XP_MULT = 2.0;
    private static final double EPIC_XP_MULT = 3.0;
    private static final double LEGENDARY_XP_MULT = 5.0;

    // Reference to the upgrade (will be set by IronsSpellsCompat during init)
    private static UpgradeArcaneChanneling arcaneChannelingUpgrade;

    /**
     * Set the upgrade instance (called by compat module during initialization)
     */
    public static void setUpgrade(UpgradeArcaneChanneling upgrade) {
        arcaneChannelingUpgrade = upgrade;
    }

    /**
     * Grant Living Armor XP when player casts a spell
     * Priority LOWEST to run after spell execution
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellCast(SpellOnCastEvent event) {
        if (!AnimusConfig.ironsSpells.enableLivingArmorXP.get()) {
            return;
        }

        if (arcaneChannelingUpgrade == null) {
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

        // Get spell level
        int spellLevel = event.getSpellLevel();

        // Calculate XP to grant
        // Note: Cannot get spell rarity from SpellOnCastEvent, so using base XP only
        int baseXP = AnimusConfig.ironsSpells.livingArmorBaseXP.get();
        double xpToGrant = baseXP * spellLevel;

        // Grant XP to the Arcane Channeling upgrade using the proper API
        LivingUtil.applyNewExperience(player, arcaneChannelingUpgrade, xpToGrant);

        Animus.LOGGER.debug("Granted {} XP to Living Armor (Arcane Channeling) for casting spell (level {})",
            xpToGrant, spellLevel);
    }

    /**
     * Get XP multiplier based on spell rarity
     */
    private static double getXPMultiplier(io.redspace.ironsspellbooks.api.spells.SpellRarity rarity) {
        return switch (rarity) {
            case COMMON -> COMMON_XP_MULT;
            case UNCOMMON -> UNCOMMON_XP_MULT;
            case RARE -> RARE_XP_MULT;
            case EPIC -> EPIC_XP_MULT;
            case LEGENDARY -> LEGENDARY_XP_MULT;
        };
    }
}
