package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.source.AbstractSourceMachine;
import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.ArsNouveauCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    private static final int MAX_SOURCE_CAPACITY = 1000;

    private int tickCounter = 0;
    private boolean hasSource = false;

    private static Method setMaxSourceMethod;
    private static Field maxSourceField;
    private static boolean reflectionInitialized = false;

    public BlockEntityArcaneRune(BlockPos pos, BlockState state) {
        super(ArsNouveauCompat.ARCANE_RUNE_BE.get(), pos, state);
        // Set max source capacity using reflection for cross-version compatibility
        initMaxSourceReflection();
        setMaxSourceValue(MAX_SOURCE_CAPACITY);
    }

    private static void initMaxSourceReflection() {
        if (reflectionInitialized) return;
        reflectionInitialized = true;

        try {
            setMaxSourceMethod = AbstractSourceMachine.class.getMethod("setMaxSource", int.class);
            return;
        } catch (NoSuchMethodException e) {
            // Fall back to field access
        }

        try {
            maxSourceField = AbstractSourceMachine.class.getDeclaredField("maxSource");
            maxSourceField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Animus.LOGGER.error("Could not find maxSource field or setMaxSource method in AbstractSourceMachine");
        }
    }

    private void setMaxSourceValue(int value) {
        try {
            if (setMaxSourceMethod != null) {
                setMaxSourceMethod.invoke(this, value);
            } else if (maxSourceField != null) {
                maxSourceField.setInt(this, value);
            }
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to set maxSource value: {}", e.getMessage());
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (level.getBlockEntity(worldPosition) != this) {
            return;
        }

        tickCounter++;

        int drainInterval = AnimusConfig.arsNouveau.arcaneRuneDrainInterval.get();
        int drainAmount = AnimusConfig.arsNouveau.arcaneRuneDrainAmount.get();

        if (tickCounter >= drainInterval) {
            tickCounter = 0;

            if (getSource() >= drainAmount) {
                removeSource(drainAmount);
                hasSource = true;
            } else {
                hasSource = false;
            }

            setChanged();
        }
    }

    public float getSpeedMultiplier() {
        return hasSource ? 1.20f : 0.675f;
    }

    public boolean providesDislocationBonus() {
        return hasSource;
    }

    public boolean hasSource() {
        return hasSource;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        hasSource = tag.getBoolean("hasSource");
        tickCounter = tag.getInt("tickCounter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("hasSource", hasSource);
        tag.putInt("tickCounter", tickCounter);
    }

    @Override
    public int getTransferRate() {
        return 1000; // Allow fast transfers
    }
}
