package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.common.item.armor.ItemLivingArmor;
import wayoftime.bloodmagic.core.living.ILivingContainer;
import wayoftime.bloodmagic.core.living.LivingStats;
import wayoftime.bloodmagic.core.living.LivingUtil;

/**
 * Handles Living Armor integration with Ars Nouveau
 *
 * Features:
 * - Grants Living Armor XP when glyphs/spells are cast
 * - XP scales with spell complexity (number of glyphs)
 * - Supports Source Attunement upgrade tree (if registered)
 */
public class LivingArmorGlyphHandler {

    /**
     * Grant Living Armor XP when player casts an Ars Nouveau spell
     * Priority LOWEST to run after spell execution
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellCast(SpellCastEvent event) {
        if (!AnimusConfig.arsNouveau.enableLivingArmorXP.get()) {
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

        // Get spell complexity (number of glyphs)
        int glyphCount = event.getSpell().getSpellSize();

        // Calculate XP to grant based on spell complexity
        int baseXP = AnimusConfig.arsNouveau.livingArmorBaseXP.get();
        int xpToGrant = baseXP * glyphCount;

        // Grant XP to Living Armor
        grantLivingArmorXP(player, xpToGrant);

        Animus.LOGGER.debug("Granted {} XP to Living Armor for casting Ars Nouveau spell ({} glyphs)",
            xpToGrant, glyphCount);
    }

    /**
     * Check if player is wearing any Living Armor pieces
     */
    private static boolean isWearingLivingArmor(ServerPlayer player) {
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
}
