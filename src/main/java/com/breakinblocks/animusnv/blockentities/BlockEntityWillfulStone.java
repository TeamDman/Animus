package com.breakinblocks.animusnv.blockentities;

import com.breakinblocks.animusnv.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.storeNullable("Owner", UUIDUtil.CODEC, owner);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.read("Owner", UUIDUtil.CODEC).ifPresent(uuid -> owner = uuid);
    }
}
