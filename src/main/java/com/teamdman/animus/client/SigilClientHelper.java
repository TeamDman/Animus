package com.teamdman.animus.client;

import net.minecraft.client.Minecraft;

import java.util.UUID;

/**
 * Client-only helper for sigil operations.
 */
public class SigilClientHelper {

    /**
     * Gets the owner name from the player info cache on the client.
     * @param ownerId The UUID of the owner
     * @return The owner's name, or "Unknown" if not found
     */
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
