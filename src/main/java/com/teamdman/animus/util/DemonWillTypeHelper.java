package com.teamdman.animus.util;

import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.will.IPlayerDemonWillHandler;

/**
 * Shared utility methods for demon will type management on sentient items.
 * Consolidates duplicated will type logic from ItemSentientBow, ItemHellforgedBow,
 * ItemSpearSentient, and ItemSentientShield.
 */
public final class DemonWillTypeHelper {

    private DemonWillTypeHelper() {
        // Utility class, no instances
    }

    /**
     * Finds the demon will type with the highest amount in the player's inventory,
     * excluding DEFAULT. Returns DEFAULT if the player has no non-default will.
     */
    public static EnumWillType findDemonWillType(Player player) {
        IPlayerDemonWillHandler playerWill = NeoVitaeAPI.getInstance().getPlayerWillHandler();
        EnumWillType highestType = EnumWillType.DEFAULT;
        double highestAmount = 0;

        for (EnumWillType type : EnumWillType.values()) {
            double amount = playerWill.getTotalDemonWill(type, player);
            if (type != EnumWillType.DEFAULT && amount > highestAmount) {
                highestType = type;
                highestAmount = amount;
            }
        }

        return highestType;
    }

    /**
     * Gets the current demon will type stored on the given item stack via the
     * DEMON_WILL_TYPE data component. Returns DEFAULT if no type is stored or
     * if the stored value is invalid.
     */
    public static EnumWillType getCurrentType(ItemStack stack) {
        String typeStr = stack.get(AnimusDataComponents.DEMON_WILL_TYPE.get());
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                return EnumWillType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return EnumWillType.DEFAULT;
            }
        }
        return EnumWillType.DEFAULT;
    }

    /**
     * Sets the current demon will type on the given item stack via the
     * DEMON_WILL_TYPE data component.
     */
    public static void setCurrentType(ItemStack stack, EnumWillType type) {
        stack.set(AnimusDataComponents.DEMON_WILL_TYPE.get(), type.toString());
    }

    /**
     * Gets the total amount of the specified demon will type the player has.
     */
    public static double getTotalWillOfType(Player player, EnumWillType type) {
        return NeoVitaeAPI.getInstance().getPlayerWillHandler().getTotalDemonWill(type, player);
    }

    /**
     * Drains (consumes) the specified amount of the given demon will type from the player.
     */
    public static void drainWillFromPlayer(Player player, EnumWillType type, double amount) {
        NeoVitaeAPI.getInstance().getPlayerWillHandler().consumeDemonWill(type, player, amount);
    }
}
