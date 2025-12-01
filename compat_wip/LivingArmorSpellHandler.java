package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.common.item.armor.ItemLivingArmor;
import wayoftime.bloodmagic.core.living.ILivingContainer;
import wayoftime.bloodmagic.core.living.LivingStats;
import wayoftime.bloodmagic.core.living.LivingUpgrade;
import wayoftime.bloodmagic.core.living.LivingUtil;

/**
 * Handles Living Armor integration with Iron's Spells
 *
 * Features:
 * - Grants Living Armor XP when spells are cast
 * - XP scales with spell level and rarity
 * - Supports Arcane Channeling upgrade tree (if registered)
 */
public class LivingArmorSpellHandler {

    // XP multipliers by spell rarity
    private static final double COMMON_XP_MULT = 1.0;
    private static final double UNCOMMON_XP_MULT = 1.5;
    private static final double RARE_XP_MULT = 2.0;
    private static final double EPIC_XP_MULT = 3.0;
    private static final double LEGENDARY_XP_MULT = 5.0;

    /**
     * Grant Living Armor XP when player casts a spell
     * Priority LOWEST to run after spell execution
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellCast(SpellOnCastEvent event) {
        if (!AnimusConfig.ironsSpells.enableLivingArmorXP.get()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // Check if player is wearing Living Armor
        if (!isWearingLivingArmor(player)) {
            return;
        }

        // Get spell info
        AbstractSpell spell = event.getSpell();
        int spellLevel = event.getSpellLevel();

        // Calculate XP to grant
        int baseXP = AnimusConfig.ironsSpells.livingArmorBaseXP.get();
        double rarityMult = getXPMultiplier(spell.getRarity());
        int xpToGrant = (int)(baseXP * spellLevel * rarityMult);

        // Grant XP to Living Armor
        grantLivingArmorXP(player, xpToGrant);

        Animus.LOGGER.debug("Granted {} XP to Living Armor for casting {} (level {})",
            xpToGrant, spell.getDisplayName(null).getString(), spellLevel);
    }

    /**
     * Check if player is wearing any Living Armor pieces
     */
    private static boolean isWearingLivingArmor(Player player) {
        return player.getArmorSlots().iterator().hasNext() &&
               player.getInventory().armor.stream()
                   .anyMatch(stack -> stack.getItem() instanceof ItemLivingArmor);
    }

    /**
     * Grant XP to player's Living Armor
     */
    private static void grantLivingArmorXP(ServerPlayer player, int xp) {
        // Get Living Armor container
        ILivingContainer container = LivingUtil.getLivingContainer(player);
        if (container == null) {
            return;
        }

        // Get current stats
        LivingStats stats = container.getLivingStats();
        if (stats == null) {
            return;
        }

        // Add XP
        int currentXP = stats.getExperience();
        stats.setExperience(currentXP + xp);

        // Sync to client
        LivingUtil.updateLivingContainer(player, container);
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
