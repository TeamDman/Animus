package com.teamdman.animus.compat.botania;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.BotaniaCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaBlockType;
import vazkii.botania.api.mana.ManaNetworkAction;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;

import java.util.Optional;

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
public class BlockEntityRuneUnleashedNature extends BlockEntity implements ManaPool, SparkAttachable {
    private static final int MAX_MANA = 5000;
    private static final int MANA_PER_SECOND = 10; // 10 mana per second (configurable)
    private static final int TICKS_PER_SECOND = 20;

    // Mana storage
    private int mana = 0;

    // Timing
    private int tickCounter = 0;

    // Active state (has enough mana)
    private boolean isActive = false;

    // Spark attachment
    private ManaSpark attachedSpark = null;

    // Mana network registration tracking
    private boolean registeredWithManaNetwork = false;

    // Forge Capabilities
    private final LazyOptional<ManaPool> manaReceiverCap = LazyOptional.of(() -> this);
    private final LazyOptional<SparkAttachable> sparkAttachableCap = LazyOptional.of(() -> this);

    public BlockEntityRuneUnleashedNature(BlockPos pos, BlockState state) {
        super(BotaniaCompat.RUNE_UNLEASHED_NATURE_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Verify the block entity is still valid
        if (level.getBlockEntity(worldPosition) != this) {
            return;
        }

        // Register with mana network on first tick
        if (!registeredWithManaNetwork) {
            BotaniaAPI.instance().getManaNetworkInstance()
                .fireManaNetworkEvent(this, ManaBlockType.POOL, ManaNetworkAction.ADD);
            registeredWithManaNetwork = true;
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
     * Provides 135% of a normal capacity rune bonus
     */
    public float getCapacityMultiplier() {
        return 1.35f; // 135% of a normal capacity rune
    }

    /**
     * Get the orb effectiveness this rune provides
     * Provides 67.5% of the bonus of a normal Rune of the Orb
     */
    public float getOrbEffectiveness() {
        return 0.675f; // 67.5% of normal Rune of the Orb
    }

    /**
     * Check if this rune provides acceleration bonus
     * Only when mana is available
     */
    public boolean providesAccelerationBonus() {
        return isActive;
    }

    // Botania Mana Receiver Implementation...
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

    @Override
    public boolean isOutputtingPower() {
        return false; // This rune only receives mana, doesn't output
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public Optional<net.minecraft.world.item.DyeColor> getColor() {
        return Optional.empty(); // No color customization for this rune
    }

    @Override
    public void setColor(Optional<net.minecraft.world.item.DyeColor> color) {
        //nah fam we good
    }

    // Botania Spark Attachable Implementation.. Why not? sparks are cool.
    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true; // Allow sparks to be attached
    }

    @Override
    public void attachSpark(ManaSpark spark) {
        attachedSpark = spark;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, MAX_MANA - mana);
    }

    @Override
    public ManaSpark getAttachedSpark() {
        return attachedSpark;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false; // Allow continuous mana transfer from spark network
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            return manaReceiverCap.cast();
        }
        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
            return sparkAttachableCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        manaReceiverCap.invalidate();
        sparkAttachableCap.invalidate();
        if (registeredWithManaNetwork) {
            BotaniaAPI.instance().getManaNetworkInstance()
                .fireManaNetworkEvent(this, ManaBlockType.POOL, ManaNetworkAction.REMOVE);
        }
    }

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
