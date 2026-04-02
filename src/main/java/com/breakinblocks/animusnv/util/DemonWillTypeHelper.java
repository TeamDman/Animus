package com.breakinblocks.animusnv.util;

import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.will.IPlayerSpiritusHandler;

/**
 * Shared utility methods for Spiritus type management on sentient items.
 * Consolidates duplicated will type logic from ItemSentientBow, ItemHellforgedBow,
 * ItemSpearSentient, and ItemSentientShield.
 */
public final class DemonWillTypeHelper {

    private DemonWillTypeHelper() {
        // Utility class, no instances
    }

    /**
     * Finds the Spiritus type with the highest amount in the player's inventory.
     * Delegates to NeoVitae's own method which correctly includes DEFAULT in the comparison.
     */
    public static SpiritusType findDemonWillType(Player player) {
        return NeoVitaeAPI.getInstance().getPlayerWillHandler().getLargestSpiritusType(player);
    }

    /**
     * Gets the current Spiritus type stored on the given item stack via the
     * SPIRITUS_TYPE data component. Returns DEFAULT if no type is stored or
     * if the stored value is invalid.
     */
    public static SpiritusType getCurrentType(ItemStack stack) {
        String typeStr = stack.get(AnimusDataComponents.SPIRITUS_TYPE.get());
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                return SpiritusType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return SpiritusType.DEFAULT;
            }
        }
        return SpiritusType.DEFAULT;
    }

    /**
     * Sets the current Spiritus type on the given item stack via the
     * SPIRITUS_TYPE data component.
     */
    public static void setCurrentType(ItemStack stack, SpiritusType type) {
        stack.set(AnimusDataComponents.SPIRITUS_TYPE.get(), type.toString());
    }

    /**
     * Gets the total amount of the specified Spiritus type the player has.
     */
    public static double getTotalWillOfType(Player player, SpiritusType type) {
        return NeoVitaeAPI.getInstance().getPlayerWillHandler().getTotalSpiritus(type, player);
    }

    /**
     * Drains (consumes) the specified amount of the given Spiritus type from the player.
     */
    public static void drainWillFromPlayer(Player player, SpiritusType type, double amount) {
        NeoVitaeAPI.getInstance().getPlayerWillHandler().consumeSpiritus(type, player, amount);
    }
}
