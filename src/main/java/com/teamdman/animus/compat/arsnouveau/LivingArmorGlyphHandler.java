package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.LivingUpgradeHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import wayoftime.bloodmagic.common.living.LivingHelper;

/**
 * Handles Living Armor integration with Ars Nouveau
 *
 * Features:
 * - Grants Living Armor XP when glyphs/spells are cast
 * - XP is granted to the Source Attunement upgrade tree
 */
public class LivingArmorGlyphHandler {

    /**
     * Register the event handler
     */
    public static void register() {
        NeoForge.EVENT_BUS.register(new LivingArmorGlyphHandler());
        Animus.LOGGER.info("Registered Living Armor Glyph Handler for Ars Nouveau");
    }

    /**
     * Grant Living Armor XP when player casts an Ars Nouveau spell
     * Priority LOWEST to run after spell execution
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCast(SpellCastEvent event) {
        if (!AnimusConfig.arsNouveau.enableLivingArmorXP.get()) {
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

        // Get base XP from config
        int baseXP = AnimusConfig.arsNouveau.livingArmorBaseXP.get();

        // Grant XP to the Source Attunement upgrade
        boolean success = LivingUpgradeHelper.addExperience(
            player,
            SourceAttunementHandler.UPGRADE_ID,
            baseXP
        );

        if (success) {
            Animus.LOGGER.debug("Granted {} XP to Living Armor (Source Attunement) for casting Ars Nouveau spell",
                baseXP);
        }
    }
}
