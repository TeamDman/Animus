package com.breakinblocks.animusnv.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.spiritus.IPlayerSpiritusHandler;

public final class SpiritusTypeHelper {

    private SpiritusTypeHelper() {}

    public static SpiritusType findSpiritusType(Player player) {
        return NeoVitaeAPI.getInstance().getPlayerSpiritusHandler().getLargestSpiritusType(player);
    }

    public static SpiritusType getCurrentType(ItemStack stack) {
        return stack.getOrDefault(NVDataComponents.SPIRITUS_TYPE, SpiritusType.RAW);
    }

    public static void setCurrentType(ItemStack stack, SpiritusType type) {
        stack.set(NVDataComponents.SPIRITUS_TYPE, type);
    }

    public static double getTotalSpiritusOfType(Player player, SpiritusType type) {
        return NeoVitaeAPI.getInstance().getPlayerSpiritusHandler().getTotalSpiritus(type, player);
    }

    public static void drainSpiritusFromPlayer(Player player, SpiritusType type, double amount) {
        NeoVitaeAPI.getInstance().getPlayerSpiritusHandler().consumeSpiritus(type, player, amount);
    }
}
