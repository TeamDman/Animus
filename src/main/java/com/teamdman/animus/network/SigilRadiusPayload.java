package com.teamdman.animus.network;

import com.teamdman.animus.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

/**
 * Payload for syncing Sigil of Equivalency radius from client to server
 */
public record SigilRadiusPayload(InteractionHand hand, int radius) implements CustomPacketPayload {
    public static final Type<SigilRadiusPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "sigil_radius")
    );

    public static final StreamCodec<FriendlyByteBuf, SigilRadiusPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SigilRadiusPayload decode(FriendlyByteBuf buf) {
            InteractionHand hand = buf.readEnum(InteractionHand.class);
            int radius = buf.readInt();
            return new SigilRadiusPayload(hand, radius);
        }

        @Override
        public void encode(FriendlyByteBuf buf, SigilRadiusPayload payload) {
            buf.writeEnum(payload.hand);
            buf.writeInt(payload.radius);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
