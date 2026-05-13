package com.breakinblocks.animusnv.util;

import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.will.IPlayerSpiritusHandler;

public final class SpiritusTypeHelper {

    private SpiritusTypeHelper() {}

    public static SpiritusType findSpiritusType(Player player) {
        return NeoVitaeAPI.getInstance().getPlayerWillHandler().getLargestSpiritusType(player);
    }

    public static SpiritusType getCurrentType(ItemStack stack) {
        String typeStr = stack.get(AnimusDataComponents.SPIRITUS_TYPE.get());
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                return SpiritusType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return SpiritusType.RAW;
            }
        }
        return SpiritusType.RAW;
    }

    public static void setCurrentType(ItemStack stack, SpiritusType type) {
        stack.set(AnimusDataComponents.SPIRITUS_TYPE.get(), type.toString());
    }

    public static double getTotalSpiritusOfType(Player player, SpiritusType type) {
        return NeoVitaeAPI.getInstance().getPlayerWillHandler().getTotalSpiritus(type, player);
    }

    public static void drainSpiritusFromPlayer(Player player, SpiritusType type, double amount) {
        NeoVitaeAPI.getInstance().getPlayerWillHandler().consumeSpiritus(type, player, amount);
    }
}
