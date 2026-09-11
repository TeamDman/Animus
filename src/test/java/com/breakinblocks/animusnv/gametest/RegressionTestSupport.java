package com.breakinblocks.animusnv.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.UUID;

final class RegressionTestSupport {
    private RegressionTestSupport() {}

    static ServerPlayer player(ServerLevel level) {
        return new FakePlayer(level, new GameProfile(UUID.randomUUID(), "Regression")) {
            // FakePlayer's packet listener intentionally ignores teleport packets.
            @Override
            public void teleportTo(ServerLevel destination, double x, double y, double z, float yaw, float pitch) {
                setServerLevel(destination);
                moveTo(x, y, z, yaw, pitch);
            }
        };
    }
}
