package com.teamdman.animus.network;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Payload for syncing accelerated blocks from server to client for rendering
 */
public record AcceleratedBlocksSyncPayload(Map<BlockPos, AccelerationEntry> acceleratedBlocks) implements CustomPacketPayload {
    public static final Type<AcceleratedBlocksSyncPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "accelerated_blocks_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, AcceleratedBlocksSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AcceleratedBlocksSyncPayload decode(FriendlyByteBuf buf) {
            int size = buf.readInt();
            Map<BlockPos, AccelerationEntry> acceleratedBlocks = new HashMap<>();
            for (int i = 0; i < size; i++) {
                BlockPos pos = buf.readBlockPos();
                int level = buf.readInt();
                long expiryTime = buf.readLong();
                ResourceLocation dimLocation = buf.readResourceLocation();
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLocation);
                acceleratedBlocks.put(pos, new AccelerationEntry(level, expiryTime, dimension));
            }
            return new AcceleratedBlocksSyncPayload(acceleratedBlocks);
        }

        @Override
        public void encode(FriendlyByteBuf buf, AcceleratedBlocksSyncPayload payload) {
            buf.writeInt(payload.acceleratedBlocks.size());
            for (Map.Entry<BlockPos, AccelerationEntry> entry : payload.acceleratedBlocks.entrySet()) {
                buf.writeBlockPos(entry.getKey());
                AccelerationEntry data = entry.getValue();
                buf.writeInt(data.level);
                buf.writeLong(data.expiryTime);
                buf.writeResourceLocation(data.dimension.location());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Convert to the format used by AcceleratedBlocksClientData
     */
    public Map<BlockPos, AccelerationData> toDataMap() {
        Map<BlockPos, AccelerationData> result = new HashMap<>();
        for (Map.Entry<BlockPos, AccelerationEntry> entry : acceleratedBlocks.entrySet()) {
            AccelerationEntry e = entry.getValue();
            result.put(entry.getKey(), new AccelerationData(e.level, e.expiryTime, e.dimension));
        }
        return result;
    }

    /**
     * Data record for serializing acceleration state
     */
    public record AccelerationEntry(int level, long expiryTime, ResourceKey<Level> dimension) {
        public int getSpeedMultiplier() {
            return 1 << level;
        }
    }

    /**
     * Data record for client-side acceleration data
     */
    public record AccelerationData(int level, long expiryTime, ResourceKey<Level> dimension) {
        public int getSpeedMultiplier() {
            return 1 << level;
        }
    }
}
