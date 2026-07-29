package com.breakinblocks.animusnv.network;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.client.AcceleratedBlocksClientData;
import com.breakinblocks.animusnv.client.AltarGhostBlockRenderer;
import com.breakinblocks.animusnv.items.sigils.effects.EquivalencySigilEffect;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class AnimusPayloads {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.Mod.MODID);

        registrar.playToClient(
            AltarGhostBlocksPayload.TYPE,
            AltarGhostBlocksPayload.STREAM_CODEC,
            AnimusPayloads::handleAltarGhostBlocks
        );

        registrar.playToClient(
            AcceleratedBlocksSyncPayload.TYPE,
            AcceleratedBlocksSyncPayload.STREAM_CODEC,
            AnimusPayloads::handleAcceleratedBlocksSync
        );

        registrar.playToServer(
            SigilRadiusPayload.TYPE,
            SigilRadiusPayload.STREAM_CODEC,
            AnimusPayloads::handleSigilRadius
        );

        Animus.LOGGER.debug("Registered Animus network payloads");
    }

    private static void handleAltarGhostBlocks(AltarGhostBlocksPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> AltarGhostBlockRenderer.setGhostBlocks(payload.ghostBlocks(), payload.durationTicks()));
    }

    private static void handleAcceleratedBlocksSync(AcceleratedBlocksSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> AcceleratedBlocksClientData.setAcceleratedBlocks(payload.toDataMap()));
    }

    private static void handleSigilRadius(SigilRadiusPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                ItemStack stack = player.getItemInHand(payload.hand());
                if (stack.is(AnimusItems.SIGIL_EQUIVALENCY.get())) {
                    EquivalencySigilEffect.setRadius(stack, payload.radius());
                }
            }
        });
    }

    public static void sendToPlayer(ServerPlayer player, Object payload) {
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) payload);
    }

    public static void sendToServer(Object payload) {
        ClientPacketDistributor.sendToServer((CustomPacketPayload) payload);
    }
}
