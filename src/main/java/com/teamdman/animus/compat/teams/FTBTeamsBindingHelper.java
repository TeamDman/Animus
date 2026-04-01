package com.teamdman.animus.compat.teams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * FTB Teams API calls for team binding checks.
 * This class should ONLY be loaded when FTB Teams is present.
 * All access should go through {@link AnimusSigilBase#isBindingOwner}.
 */
public class FTBTeamsBindingHelper {

    /**
     * Check if a player is a member of the team identified by the given UUID.
     * @param player The player to check
     * @param teamId The UUID that may be a team ID
     * @return true if teamId is a party team and the player is a member
     */
    public static boolean isPlayerOnTeam(ServerPlayer player, UUID teamId) {
        return FTBTeamsAPI.api().getManager()
                .getTeamByID(teamId)
                .filter(team -> !team.isPlayerTeam())
                .map(team -> team.getMembers().contains(player.getUUID()))
                .orElse(false);
    }
}
