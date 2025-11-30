package com.teamdman.animus.blockentities;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.mana.ManaReceiver;

/**
 * Rune of Unleashed Nature - A hybrid Blood Magic altar rune powered by Botania mana
 *
 * Features:
 * - Acts as a Capacity Rune (increases LP storage)
 * - Acts as a Rune of the Orb at half effectiveness (increases LP drain efficiency)
 * - When charged with mana (>=10 mana), also acts as an Acceleration Rune
 * - Can receive mana via Botania sparks
 * - Internal mana buffer: 5000 mana
 * - Consumption rate: 10 mana per second (20 ticks)
 *
 * This rune bridges Blood Magic and Botania, offering powerful bonuses when both
 * magical systems work in harmony.
 */
public class BlockEntityRuneUnleashedNature extends BlockEntity implements ManaReceiver {
    private static final int MAX_MANA = 5000;
    private static final int MANA_PER_SECOND = 10; // 10 mana per second (configurable)
    private static final int TICKS_PER_SECOND = 20;

    // Mana storage
    private int mana = 0;

    // Timing
    private int tickCounter = 0;

    // Active state (has enough mana)
    private boolean isActive = false;

    public BlockEntityRuneUnleashedNature(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.RUNE_UNLEASHED_NATURE.get(), pos, state);
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

        // Get config value for mana consumption
        int manaConsumption = AnimusConfig.botania.unleashedNatureManaDrain.get();

        // Consume mana every second (20 ticks)
        if (tickCounter >= TICKS_PER_SECOND) {
            tickCounter = 0;

            if (mana >= manaConsumption) {
                // Consume mana
                mana -= manaConsumption;
                isActive = true;
                setChanged();
            } else {
                // Not enough mana - deactivate acceleration bonus
                isActive = false;
                setChanged();
            }
        }
    }

    /**
     * Check if this rune is active (has enough mana for acceleration bonus)
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get the capacity multiplier this rune provides
     * Always provides capacity bonus
     */
    public float getCapacityMultiplier() {
        return 1.0f; // Acts as a normal capacity rune
    }

    /**
     * Get the orb effectiveness this rune provides
     * Provides half the bonus of a normal Rune of the Orb
     */
    public float getOrbEffectiveness() {
        return 0.5f; // Half of normal Rune of the Orb
    }

    /**
     * Check if this rune provides acceleration bonus
     * Only when mana is available
     */
    public boolean providesAccelerationBonus() {
        return isActive;
    }

    // ===========================================
    // Botania Mana Receiver Implementation
    // ===========================================

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return mana >= MAX_MANA;
    }

    @Override
    public void receiveMana(int manaToReceive) {
        mana = Math.min(mana + manaToReceive, MAX_MANA);
        setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    // ===========================================
    // NBT Serialization
    // ===========================================

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mana = tag.getInt("mana");
        isActive = tag.getBoolean("isActive");
        tickCounter = tag.getInt("tickCounter");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("mana", mana);
        tag.putBoolean("isActive", isActive);
        tag.putInt("tickCounter", tickCounter);
    }
}
