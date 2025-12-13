package com.teamdman.animus.network;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Payload for sending ghost block rendering data to the client
 */
public record AltarGhostBlocksPayload(Map<BlockPos, ResourceLocation> ghostBlocks, int durationTicks) implements CustomPacketPayload {
    public static final Type<AltarGhostBlocksPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "altar_ghost_blocks")
    );

    public static final StreamCodec<FriendlyByteBuf, AltarGhostBlocksPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AltarGhostBlocksPayload decode(FriendlyByteBuf buf) {
            int size = buf.readInt();
            Map<BlockPos, ResourceLocation> ghostBlocks = new HashMap<>();
            for (int i = 0; i < size; i++) {
                BlockPos pos = buf.readBlockPos();
                ResourceLocation blockId = buf.readResourceLocation();
                ghostBlocks.put(pos, blockId);
            }
            int durationTicks = buf.readInt();
            return new AltarGhostBlocksPayload(ghostBlocks, durationTicks);
        }

        @Override
        public void encode(FriendlyByteBuf buf, AltarGhostBlocksPayload payload) {
            buf.writeInt(payload.ghostBlocks.size());
            for (Map.Entry<BlockPos, ResourceLocation> entry : payload.ghostBlocks.entrySet()) {
                buf.writeBlockPos(entry.getKey());
                buf.writeResourceLocation(entry.getValue());
            }
            buf.writeInt(payload.durationTicks);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
