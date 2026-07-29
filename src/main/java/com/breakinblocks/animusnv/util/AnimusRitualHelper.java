package com.breakinblocks.animusnv.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;

import javax.annotation.Nullable;
import java.util.UUID;

public final class AnimusRitualHelper {

    private AnimusRitualHelper() {}

    @Nullable
    public static IAnima getOwnerNetwork(IMasterRitualStone mrs) {
        return NeoVitaeAPI.getInstance().getAnima(mrs.getOwner());
    }

    /**
     * Gets the Anima for a bound item, respecting team bindings.
     * Falls back to player's personal network if item has no binding.
     */
    @Nullable
    public static IAnima getNetworkForBoundItem(Player player, ItemStack stack) {
        Binding binding = stack.get(NVDataComponents.BINDING.get());
        UUID owner = (binding != null && !binding.isEmpty()) ? binding.uuid() : player.getUUID();
        return NeoVitaeAPI.getInstance().getAnima(owner);
    }

    @Nullable
    public static ResourceHandler<ItemResource> getResourceHandler(Level level, BlockPos pos) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, null);
    }

    @Nullable
    public static IItemHandler getItemHandler(Level level, BlockPos pos) {
        ResourceHandler<ItemResource> handler = getResourceHandler(level, pos);
        return handler == null ? null : IItemHandler.of(handler);
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
     * Attempts to drain EV from a network owner's Anima.
     * @return true if the EV was successfully drained
     */
    public static boolean drainEV(Player player, UUID networkOwner, int amount) {
        IAnima network = NeoVitaeAPI.getInstance().getAnima(networkOwner);
        if (network == null || network.getCurrentEV() < amount) {
            return false;
        }
        return network.syphonAndDamage(player, AnimaTicket.create(amount)).success();
    }

    /**
     * Attempts to drain EV from a bound item's network, respecting team bindings.
     * @return true if the EV was successfully drained
     */
    public static boolean drainEVForBoundItem(Player player, ItemStack stack, int amount) {
        IAnima network = getNetworkForBoundItem(player, stack);
        if (network == null || network.getCurrentEV() < amount) {
            return false;
        }
        return network.syphonAndDamage(player, AnimaTicket.create(amount)).success();
    }
}
