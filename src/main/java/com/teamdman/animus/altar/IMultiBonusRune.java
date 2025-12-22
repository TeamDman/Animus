package com.teamdman.animus.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.altar.AltarUpgrade;
import wayoftime.bloodmagic.altar.IBloodRune;

/**
 * Extended rune interface that supports providing multiple bonuses to the Blood Altar.
 *
 * Standard Blood Magic runes can only return a single BloodRuneType via IBloodRune.
 * This interface allows Animus runes (like Arcane Rune and Rune of Unleashed Nature)
 * to provide multiple bonuses simultaneously, such as:
 * - Multiple rune types at once (e.g., Capacity + Orb + Acceleration)
 * - Fractional rune counts (e.g., 1.35x Capacity bonus)
 * - State-dependent bonuses (e.g., extra bonuses when mana/source is available)
 *
 * This interface is checked by a mixin that modifies AltarUtil.getUpgrades() to
 * apply the multi-bonus upgrades in addition to the standard single-rune bonus.
 */
public interface IMultiBonusRune extends IBloodRune {

    /**
     * Apply all bonuses this rune provides to the given AltarUpgrade.
     * This method is called by the AltarUtil mixin to gather all rune bonuses.
     *
     * Implementation should call upgrades.upgrade(BloodRuneType, count) for each
     * bonus the rune provides. Fractional bonuses should be handled by the
     * implementation (e.g., tracking partial counts across multiple runes).
     *
     * @param level The level/world
     * @param pos The position of the rune
     * @param upgrades The AltarUpgrade to add bonuses to
     */
    void applyMultiBonuses(Level level, BlockPos pos, AltarUpgrade upgrades);

    /**
     * Whether this rune should skip the standard single-rune processing.
     * If true, only applyMultiBonuses() is used and getBloodRune()/getRuneCount() are ignored.
     * If false, both standard processing AND applyMultiBonuses() are applied.
     *
     * Default is true - most multi-bonus runes will handle all their bonuses in applyMultiBonuses().
     *
     * @return true to skip standard processing, false to apply both
     */
    default boolean skipStandardProcessing() {
        return true;
    }
}
