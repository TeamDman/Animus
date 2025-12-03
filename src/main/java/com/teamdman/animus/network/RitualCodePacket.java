package com.teamdman.animus.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for sending ritual code to client for clipboard copying
 */
public class RitualCodePacket {
    private final String code;

    public RitualCodePacket(String code) {
        this.code = code;
    }

    public static void encode(RitualCodePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.code);
    }

    public static RitualCodePacket decode(FriendlyByteBuf buf) {
        String code = buf.readUtf();
        return new RitualCodePacket(code);
    }

    public static void handle(RitualCodePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // This runs on the client thread - use DistExecutor to safely access client classes
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                com.teamdman.animus.client.ClipboardClientHelper.setClipboard(msg.code);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
