package com.teamdman.animus.compat.arsnouveau;

import com.teamdman.animus.Animus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.breakinblocks.neovitae.api.altar.rune.AltarRuneModifiers;
import com.breakinblocks.neovitae.api.event.AltarRuneEvent;

import java.util.List;

/**
 * Event handler for Arcane Rune integration with NeoVitae's altar rune system.
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

    public static void register() {
        NeoForge.EVENT_BUS.register(INSTANCE);
        Animus.LOGGER.debug("Registered Arcane Rune event handler for NeoVitae altar integration");
    }

    @SubscribeEvent
    public void onCalculateStats(AltarRuneEvent.CalculateStats event) {
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

        // NeoVitae already applied +20% per rune from the registry; adjust from that base
        if (poweredCount > 0) {
            modifiers.addConsumptionMod(SPEED_ADJUSTMENT_WITH_SOURCE * poweredCount);
            modifiers.multiplyDislocationMod(1.0f + (DISLOCATION_BONUS_WITH_SOURCE * poweredCount));
        }

        if (unpoweredCount > 0) {
            modifiers.addConsumptionMod(SPEED_ADJUSTMENT_NO_SOURCE * unpoweredCount);
        }

        if (poweredCount > 0 || unpoweredCount > 0) {
            Animus.LOGGER.debug("Applied Arcane Rune bonuses: {} powered, {} unpowered",
                poweredCount, unpoweredCount);
        }
    }
}
