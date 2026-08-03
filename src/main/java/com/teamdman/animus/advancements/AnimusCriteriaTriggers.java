package com.teamdman.animus.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.tile.TileAltar;

public class AnimusCriteriaTriggers {

    private static final double NOTIFY_RADIUS = 16.0;

    public static AltarTierTrigger ALTAR_TIER;

    public static void register() {
        ALTAR_TIER = CriteriaTriggers.register(new AltarTierTrigger());
    }

    public static void onAltarTierFormed(TileAltar altar, int tier) {
        if (ALTAR_TIER == null || altar == null) {
            return;
        }

        Level level = altar.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = altar.getBlockPos();
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= NOTIFY_RADIUS * NOTIFY_RADIUS) {
                ALTAR_TIER.trigger(player, tier);
            }
        }
    }
}
