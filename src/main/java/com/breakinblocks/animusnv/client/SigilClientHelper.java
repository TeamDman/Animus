package com.breakinblocks.animusnv.client;

import net.minecraft.client.Minecraft;

import java.util.UUID;

public class SigilClientHelper {

    public static String getOwnerName(UUID ownerId) {
        var minecraft = Minecraft.getInstance();
        var connection = minecraft.getConnection();
        if (connection != null) {
            var playerInfo = connection.getPlayerInfo(ownerId);
            if (playerInfo != null) {
                return playerInfo.getProfile().getName();
            }
        }
        return "Unknown";
    }
}
