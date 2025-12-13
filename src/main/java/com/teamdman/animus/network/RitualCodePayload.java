package com.teamdman.animus.network;

import com.teamdman.animus.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Payload for sending ritual code to client for clipboard copying
 */
public record RitualCodePayload(String code) implements CustomPacketPayload {
    public static final Type<RitualCodePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "ritual_code")
    );

    public static final StreamCodec<FriendlyByteBuf, RitualCodePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, RitualCodePayload::code,
        RitualCodePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
