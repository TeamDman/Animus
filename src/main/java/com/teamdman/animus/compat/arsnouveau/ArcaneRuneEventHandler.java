package com.teamdman.animus.compat.arsnouveau;

import com.teamdman.animus.Animus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import wayoftime.bloodmagic.api.altar.rune.AltarRuneModifiers;
import wayoftime.bloodmagic.api.event.AltarRuneEvent;

import java.util.List;

/**
 * Event handler for Arcane Rune integration with Blood Magic's altar rune system.
 *
 * The Arcane Rune provides dynamic bonuses based on Ars Nouveau Source availability:
 *
 * When Source is available (consuming 20 Source every 10 seconds):
 * - Speed bonus: +35% consumption speed (1.35x multiplier)
 * - Dislocation bonus: +35% fluid I/O rate
 *
 * When no Source is available:
 * - Speed penalty: -15% consumption speed (0.85x multiplier)
 * - No dislocation change
 */
public class ArcaneRuneEventHandler {

    private static final ArcaneRuneEventHandler INSTANCE = new ArcaneRuneEventHandler();

    // Speed modifier when Source is available (35% bonus total)
    // Base gives +20%, we want +35%, so add +15% on top
    private static final float SPEED_ADJUSTMENT_WITH_SOURCE = 0.15f;

    // Speed modifier when no Source (want -15% speed, i.e. 85% of normal)
    // Base gives +20%, we want -15%, so subtract 35% (0.20 + 0.15 = 0.35)
    private static final float SPEED_ADJUSTMENT_NO_SOURCE = -0.35f;

    // Dislocation modifier when Source is available (35% bonus per rune)
    private static final float DISLOCATION_BONUS_WITH_SOURCE = 0.35f;

    private ArcaneRuneEventHandler() {}

    /**
     * Register this event handler with the NeoForge event bus
     */
    public static void register() {
        NeoForge.EVENT_BUS.register(INSTANCE);
        Animus.LOGGER.info("Registered Arcane Rune event handler for Blood Magic altar integration");
    }

    /**
     * Handle the CalculateStats event to apply Arcane Rune bonuses.
     *
     * This event fires after all runes have been gathered and the base modifiers
     * have been calculated. We can modify the AltarRuneModifiers here to apply
     * our dynamic bonuses based on Source availability.
     *
     * Uses Blood Magic's new API that exposes rune block entities directly,
     * eliminating the need to rescan the altar structure.
     */
    @SubscribeEvent
    public void onCalculateStats(AltarRuneEvent.CalculateStats event) {
        // Blood Magic already scanned the altar - just filter the results!
        List<BlockEntityArcaneRune> arcaneRunes = event.getRuneBlockEntities(BlockEntityArcaneRune.class);

        if (arcaneRunes.isEmpty()) {
            return;
        }

        AltarRuneModifiers modifiers = event.getModifiers();

        int poweredCount = 0;
        int unpoweredCount = 0;

        for (BlockEntityArcaneRune rune : arcaneRunes) {
            if (rune.hasSource()) {
                poweredCount++;
            } else {
                unpoweredCount++;
            }
        }

        // Apply speed bonuses/penalties
        // Note: Blood Magic already applied +20% per Arcane Rune from the registry.
        // We need to adjust from that base.

        if (poweredCount > 0) {
            // Powered runes: Add +15% speed on top of base +20% = +35% total
            modifiers.addConsumptionMod(SPEED_ADJUSTMENT_WITH_SOURCE * poweredCount);
            // Add dislocation bonus (35% per rune)
            modifiers.multiplyDislocationMod(1.0f + (DISLOCATION_BONUS_WITH_SOURCE * poweredCount));
        }

        if (unpoweredCount > 0) {
            // Unpowered runes: We want -15% speed instead of +20% (base)
            // Subtract 35% from base to get -15% total
            modifiers.addConsumptionMod(SPEED_ADJUSTMENT_NO_SOURCE * unpoweredCount);
        }

        if (poweredCount > 0 || unpoweredCount > 0) {
            Animus.LOGGER.debug("Applied Arcane Rune bonuses: {} powered, {} unpowered",
                poweredCount, unpoweredCount);
        }
    }
}
