package com.teamdman.animus.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;

import javax.annotation.Nullable;
import java.util.UUID;

public final class AnimusRitualHelper {

    private AnimusRitualHelper() {}

    /**
     * Gets the soul network for a ritual's owner. Returns null if the network
     * doesn't exist (e.g., server not available, owner never logged in).
     */
    @Nullable
    public static ISoulNetwork getOwnerNetwork(IMasterRitualStone mrs) {
        return NeoVitaeAPI.getInstance().getSoulNetwork(mrs.getOwner());
    }

    /**
     * Gets the item handler capability at the given position, or null if none exists.
     */
    @Nullable
    public static IItemHandler getItemHandler(Level level, BlockPos pos) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
    }

    public static void emitSmokeParticles(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 5; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            level.sendParticles(
                ParticleTypes.SMOKE,
                x, y, z,
                1,
                0.0, 0.05, 0.0,
                0.01
            );
        }
    }

    /**
     * Attempts to drain LP from a player's soul network.
     * @return true if the LP was successfully drained
     */
    public static boolean drainLP(Player player, UUID networkOwner, int amount) {
        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(networkOwner);
        if (network == null || network.getCurrentEssence() < amount) {
            return false;
        }
        return network.syphonAndDamage(player, SoulTicket.create(amount)).success();
    }
}
