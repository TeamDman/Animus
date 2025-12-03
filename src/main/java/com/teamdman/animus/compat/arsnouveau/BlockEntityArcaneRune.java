package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.source.AbstractSourceMachine;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block Entity for Arcane Rune
 * Receives Source via Ars Nouveau's dominion wand linking system
 * Stores up to 1000 Source internally and consumes it for altar bonuses
 *
 * When Source is available (default: 20 Source consumed every 10 seconds):
 * - Acts as a Speed Rune but 15% faster
 * - Acts as a Dislocation Rune
 *
 * When no Source available:
 * - Acts as a Speed Rune but half as fast
 *
 * Source consumption rate is configurable in AnimusConfig.arsNouveau
 */
public class BlockEntityArcaneRune extends AbstractSourceMachine {
    private static final int MAX_SOURCE = 1000;

    // Timing
    private int tickCounter = 0;

    // Source state
    private boolean hasSource = false;

    public BlockEntityArcaneRune(BlockPos pos, BlockState state) {
        super(com.teamdman.animus.compat.ArsNouveauCompat.ARCANE_RUNE_BE.get(), pos, state);
        setMaxSource(MAX_SOURCE);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Verify the block entity is still valid
        if (level.getBlockEntity(worldPosition) != this) {
            return;
        }

        tickCounter++;

        // Get config values
        int drainInterval = AnimusConfig.arsNouveau.arcaneRuneDrainInterval.get();
        int drainAmount = AnimusConfig.arsNouveau.arcaneRuneDrainAmount.get();

        // Consume source based on config interval
        if (tickCounter >= drainInterval) {
            tickCounter = 0;

            if (getSource() >= drainAmount) {
                // Consume Source
                removeSource(drainAmount);
                hasSource = true;
            } else {
                // Not enough Source
                hasSource = false;
            }

            setChanged();
        }
    }

    /**
     * Get the speed multiplier this rune provides
     * 1.15 when source available (15% faster than speed rune)
     * 0.5 when no source (half speed)
     */
    public float getSpeedMultiplier() {
        return hasSource ? 1.15f : 0.5f;
    }

    /**
     * Check if this rune provides dislocation bonus
     * Only when source is available
     */
    public boolean providesDislocationBonus() {
        return hasSource;
    }

    /**
     * Get whether this rune currently has source
     */
    public boolean hasSource() {
        return hasSource;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        hasSource = tag.getBoolean("hasSource");
        tickCounter = tag.getInt("tickCounter");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("hasSource", hasSource);
        tag.putInt("tickCounter", tickCounter);
    }

    @Override
    public int getTransferRate() {
        return 1000; // Allow fast transfers
    }
}
