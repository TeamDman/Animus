package com.breakinblocks.animusnv.compat.teams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * FTB Teams API calls for team binding checks.
 * This class should ONLY be loaded when FTB Teams is present.
 */
public class FTBTeamsBindingHelper {

    public static boolean isPlayerOnTeam(ServerPlayer player, UUID teamId) {
        return FTBTeamsAPI.api().getManager()
                .getTeamByID(teamId)
                .filter(team -> !team.isPlayerTeam())
                .map(team -> team.getMembers().contains(player.getUUID()))
                .orElse(false);
    }
}
