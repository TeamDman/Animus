package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.neovitae.ritual.IMasterRitualStone;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.UUID;

final class RegressionTestSupport {
    private RegressionTestSupport() {}

    static ServerPlayer player(ServerLevel level) {
        return new FakePlayer(level, new GameProfile(UUID.randomUUID(), "Regression")) {
            @Override
            public boolean teleportTo(ServerLevel destination, double x, double y, double z,
                                      Set<Relative> relatives, float yaw, float pitch, boolean resetCamera) {
                setServerLevel(destination);
                snapTo(x, y, z, yaw, pitch);
                return true;
            }
        };
    }

    static IMasterRitualStone stone(GameTestHelper h, BlockPos pos, UUID owner) {
        return (IMasterRitualStone) Proxy.newProxyInstance(
            RegressionTestSupport.class.getClassLoader(),
            new Class<?>[]{IMasterRitualStone.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getWorldObj", "getLevel" -> h.getLevel();
                case "getMasterBlockPos", "getBlockPos" -> pos;
                case "getOwner" -> owner;
                case "stopRitual" -> null;
                default -> throw new UnsupportedOperationException(method.getName());
            });
    }
}
