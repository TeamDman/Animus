package com.breakinblocks.animusnv.blockentities;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * Block Entity for AntiLife blocks
 * Stores information about what block type to seek and consume
 */
public class BlockEntityAntiLife extends BlockEntity {
    private Block seeking = Blocks.AIR;
    private int range = 0;
    private UUID playerUUID = null;

    public BlockEntityAntiLife(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.ANTILIFE.get(), pos, state);
        this.range = AnimusConfig.sigils.antiLifeRange.get();
    }

    public Block getSeeking() {
        return seeking;
    }

    public BlockEntityAntiLife setSeeking(Block seeking) {
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

    public BlockEntityAntiLife setPlayer(Player player) {
        this.playerUUID = player.getUUID();
        setChanged();
        return this;
    }

    public BlockEntityAntiLife setPlayerUUID(UUID uuid) {
        this.playerUUID = uuid;
        setChanged();
        return this;
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putString("seeking", BuiltInRegistries.BLOCK.getKey(seeking).toString());
        tag.putInt("range", range);
        tag.storeNullable("player", UUIDUtil.CODEC, playerUUID);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.seeking = resolveSeeking(tag.getStringOr("seeking", ""));
        this.range = tag.getIntOr("range", 0);
        tag.read("player", UUIDUtil.CODEC).ifPresent(uuid -> this.playerUUID = uuid);
    }

    private static Block resolveSeeking(String seekingId) {
        if (seekingId.startsWith("block.")) {
            return BuiltInRegistries.BLOCK.stream()
                .filter(block -> block.getDescriptionId().equals(seekingId))
                .findFirst()
                .orElse(Blocks.AIR);
        }
        Identifier id = Identifier.tryParse(seekingId);
        return id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.AIR);
    }
}
