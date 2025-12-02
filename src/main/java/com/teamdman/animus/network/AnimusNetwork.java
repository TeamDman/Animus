package com.teamdman.animus.network;

import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network handler for Animus mod packets
 */
public class AnimusNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(
            packetId++,
            AltarGhostBlocksPacket.class,
            AltarGhostBlocksPacket::encode,
            AltarGhostBlocksPacket::decode,
            AltarGhostBlocksPacket::handle
        );

        CHANNEL.registerMessage(
            packetId++,
            SigilRadiusPacket.class,
            SigilRadiusPacket::encode,
            SigilRadiusPacket::decode,
            SigilRadiusPacket::handle
        );

        CHANNEL.registerMessage(
            packetId++,
            RitualCodePacket.class,
            RitualCodePacket::encode,
            RitualCodePacket::decode,
            RitualCodePacket::handle
        );
    }
}
