package com.teamdman.animus.network;

import com.teamdman.animus.client.AltarGhostBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Packet for sending ghost block rendering data to the client
 */
public class AltarGhostBlocksPacket {
    private final Map<BlockPos, ResourceLocation> ghostBlocks;
    private final int durationTicks;

    public AltarGhostBlocksPacket(Map<BlockPos, ResourceLocation> ghostBlocks, int durationTicks) {
        this.ghostBlocks = ghostBlocks;
        this.durationTicks = durationTicks;
    }

    public static void encode(AltarGhostBlocksPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.ghostBlocks.size());
        for (Map.Entry<BlockPos, ResourceLocation> entry : msg.ghostBlocks.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeResourceLocation(entry.getValue());
        }
        buf.writeInt(msg.durationTicks);
    }

    public static AltarGhostBlocksPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<BlockPos, ResourceLocation> ghostBlocks = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            ResourceLocation blockId = buf.readResourceLocation();
            ghostBlocks.put(pos, blockId);
        }
        int durationTicks = buf.readInt();
        return new AltarGhostBlocksPacket(ghostBlocks, durationTicks);
    }

    public static void handle(AltarGhostBlocksPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AltarGhostBlockRenderer.setGhostBlocks(msg.ghostBlocks, msg.durationTicks)));
        ctx.get().setPacketHandled(true);
    }
}
