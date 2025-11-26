package com.teamdman.animus.blockentities;

import com.teamdman.animus.config.AnimusConfig;
import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Block Entity for Antimatter blocks
 * Stores information about what block type to seek and consume
 */
public class BlockEntityAntimatter extends BlockEntity {
    private Block seeking = Blocks.AIR;
    private int range = 0;
    private UUID playerUUID = null;

    public BlockEntityAntimatter(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.ANTIMATTER.get(), pos, state);
        this.range = AnimusConfig.COMMON.sigils.antimatterRange.get();
    }

    public Block getSeeking() {
        return seeking;
    }

    public BlockEntityAntimatter setSeeking(Block seeking) {
        this.seeking = seeking;
        setChanged();
        return this;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
        setChanged();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public BlockEntityAntimatter setPlayer(Player player) {
        this.playerUUID = player.getUUID();
        setChanged();
        return this;
    }

    public BlockEntityAntimatter setPlayerUUID(UUID uuid) {
        this.playerUUID = uuid;
        setChanged();
        return this;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("seeking", seeking.getDescriptionId());
        tag.putInt("range", range);
        if (playerUUID != null) {
            tag.putUUID("player", playerUUID);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // Try to get the block - if it fails, default to AIR
        String seekingId = tag.getString("seeking");
        this.seeking = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(
            net.minecraft.resources.ResourceLocation.tryParse(seekingId.replace("block.", ""))
        ).orElse(Blocks.AIR);

        this.range = tag.getInt("range");
        if (tag.hasUUID("player")) {
            this.playerUUID = tag.getUUID("player");
        }
    }
}
