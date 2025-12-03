package com.teamdman.animus.network;

import com.teamdman.animus.client.AcceleratedBlocksClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Packet for syncing accelerated blocks from server to client for rendering
 */
public class AcceleratedBlocksSyncPacket {
    private final Map<BlockPos, AccelerationData> acceleratedBlocks;

    public AcceleratedBlocksSyncPacket(Map<BlockPos, AccelerationData> acceleratedBlocks) {
        this.acceleratedBlocks = acceleratedBlocks;
    }

    public static void encode(AcceleratedBlocksSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.acceleratedBlocks.size());
        for (Map.Entry<BlockPos, AccelerationData> entry : msg.acceleratedBlocks.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            AccelerationData data = entry.getValue();
            buf.writeInt(data.level);
            buf.writeLong(data.expiryTime);
            buf.writeResourceLocation(data.dimension.location());
        }
    }

    public static AcceleratedBlocksSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<BlockPos, AccelerationData> acceleratedBlocks = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            int level = buf.readInt();
            long expiryTime = buf.readLong();
            ResourceLocation dimLocation = buf.readResourceLocation();
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLocation);
            acceleratedBlocks.put(pos, new AccelerationData(level, expiryTime, dimension));
        }
        return new AcceleratedBlocksSyncPacket(acceleratedBlocks);
    }

    public static void handle(AcceleratedBlocksSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> AcceleratedBlocksClientData.setAcceleratedBlocks(msg.acceleratedBlocks)));
        ctx.get().setPacketHandled(true);
    }

    /**
     * Data class for serializing acceleration state
     */
    public static class AccelerationData {
        public final int level;
        public final long expiryTime;
        public final ResourceKey<Level> dimension;

        public AccelerationData(int level, long expiryTime, ResourceKey<Level> dimension) {
            this.level = level;
            this.expiryTime = expiryTime;
            this.dimension = dimension;
        }

        public int getSpeedMultiplier() {
            return 1 << level;
        }
    }
}
