package com.teamdman.animus.blockentities;

import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Block Entity for Willful Stone
 * Stores the UUID of the player who placed the block
 */
public class BlockEntityWillfulStone extends BlockEntity {

    private UUID owner;

    public BlockEntityWillfulStone(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.WILLFUL_STONE.get(), pos, state);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Owner")) {
            owner = tag.getUUID("Owner");
        }
    }
}
