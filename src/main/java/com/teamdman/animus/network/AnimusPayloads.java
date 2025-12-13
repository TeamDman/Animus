package com.teamdman.animus.network;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.client.AcceleratedBlocksClientData;
import com.teamdman.animus.client.AltarGhostBlockRenderer;
import com.teamdman.animus.client.ClipboardClientHelper;
import com.teamdman.animus.items.sigils.ItemSigilEquivalency;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Network handler for Animus mod packets using NeoForge 1.21 payload system
 */
public class AnimusPayloads {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.Mod.MODID);

        // Server to client packets
        registrar.playToClient(
            AltarGhostBlocksPayload.TYPE,
            AltarGhostBlocksPayload.STREAM_CODEC,
            AnimusPayloads::handleAltarGhostBlocks
        );

        registrar.playToClient(
            RitualCodePayload.TYPE,
            RitualCodePayload.STREAM_CODEC,
            AnimusPayloads::handleRitualCode
        );

        registrar.playToClient(
            AcceleratedBlocksSyncPayload.TYPE,
            AcceleratedBlocksSyncPayload.STREAM_CODEC,
            AnimusPayloads::handleAcceleratedBlocksSync
        );

        // Client to server packets
        registrar.playToServer(
            SigilRadiusPayload.TYPE,
            SigilRadiusPayload.STREAM_CODEC,
            AnimusPayloads::handleSigilRadius
        );

        Animus.LOGGER.info("Registered Animus network payloads");
    }

    private static void handleAltarGhostBlocks(AltarGhostBlocksPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> AltarGhostBlockRenderer.setGhostBlocks(payload.ghostBlocks(), payload.durationTicks()));
    }

    private static void handleRitualCode(RitualCodePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClipboardClientHelper.setClipboard(payload.code()));
    }

    private static void handleAcceleratedBlocksSync(AcceleratedBlocksSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> AcceleratedBlocksClientData.setAcceleratedBlocks(payload.toDataMap()));
    }

    private static void handleSigilRadius(SigilRadiusPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                ItemStack stack = player.getItemInHand(payload.hand());
                if (stack.getItem() instanceof ItemSigilEquivalency sigil) {
                    sigil.setRadiusFromPacket(stack, payload.radius());
                }
            }
        });
    }

    // Helper methods for sending packets
    public static void sendToPlayer(ServerPlayer player, Object payload) {
        PacketDistributor.sendToPlayer(player, (net.minecraft.network.protocol.common.custom.CustomPacketPayload) payload);
    }

    public static void sendToServer(Object payload) {
        PacketDistributor.sendToServer((net.minecraft.network.protocol.common.custom.CustomPacketPayload) payload);
    }
}
