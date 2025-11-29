package com.teamdman.animus.network;

import com.mojang.logging.LogUtils;
import com.teamdman.animus.items.sigils.ItemSigilEquivalency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * Packet for syncing Sigil of Equivalency radius from client to server
 */
public class SigilRadiusPacket {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final InteractionHand hand;
    private final int radius;

    public SigilRadiusPacket(InteractionHand hand, int radius) {
        this.hand = hand;
        this.radius = radius;
    }

    public static void encode(SigilRadiusPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeInt(msg.radius);
    }

    public static SigilRadiusPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        int radius = buf.readInt();
        return new SigilRadiusPacket(hand, radius);
    }

    public static void handle(SigilRadiusPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // This runs on the server thread
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getItemInHand(msg.hand);
                if (stack.getItem() instanceof ItemSigilEquivalency sigil) {
                    LOGGER.info("[Sigil of Equivalency] [SERVER] Received radius sync packet: hand={}, radius={}", msg.hand, msg.radius);
                    sigil.setRadiusFromPacket(stack, msg.radius);
                    LOGGER.info("[Sigil of Equivalency] [SERVER] Server-side radius synced to {}", msg.radius);
                } else {
                    LOGGER.warn("[Sigil of Equivalency] [SERVER] Received radius packet but item is not Sigil of Equivalency: {}", stack.getItem());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
