package com.teamdman.animus.blockentities;

import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block Entity for Blood Core
 * Handles periodic tree growth/spreading logic
 */
public class BlockEntityBloodCore extends BlockEntity {

    private volatile int delayCounter = 1200; // 1 minute (60 seconds * 20 ticks)
    private volatile boolean spreading = false;
    private volatile boolean removed = false;

    public BlockEntityBloodCore(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.BLOOD_CORE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide || removed) {
            return;
        }

        // Verify the block entity is still valid
        if (level.getBlockEntity(worldPosition) != this) {
            return;
        }

        delayCounter--;
        if (delayCounter <= 0) {
            // TODO: Implement tree growth/spreading logic
            // This would:
            // 1. Check for corrosive will in the chunk
            // 2. Find suitable positions for new blood saplings
            // 3. Place saplings in range
            // 4. Slow timer based on will amount

            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            delayCounter = 1200; // Reset counter
        }
    }

    public boolean isSpreading() {
        return spreading;
    }

    public void setSpreading(boolean spreading) {
        this.spreading = spreading;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("DelayCounter", delayCounter);
        tag.putBoolean("Spreading", spreading);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        delayCounter = tag.getInt("DelayCounter");
        spreading = tag.getBoolean("Spreading");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.removed = true;
        // Clean up any resources here if needed
    }
}
