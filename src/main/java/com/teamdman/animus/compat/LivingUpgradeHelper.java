package com.teamdman.animus.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.api.BloodMagicAPI;
import wayoftime.bloodmagic.api.living.ILivingArmorManager;

/**
 * Helper class for checking and modifying Living Armor upgrade levels.
 * Uses Blood Magic's public API for all operations.
 * Used by compat modules to check for Animus-specific upgrades.
 */
public class LivingUpgradeHelper {

    /**
     * Get the Living Armor manager from the Blood Magic API.
     */
    private static ILivingArmorManager getManager() {
        return BloodMagicAPI.getInstance().getLivingArmorManager();
    }

    /**
     * Check if the player has a full Living Armor set equipped.
     *
     * @param player The player to check
     * @return true if player has full Living Armor set
     */
    public static boolean hasFullSet(Player player) {
        return getManager().hasFullSet(player);
    }

    /**
     * Get the player's Living Armor chest piece.
     *
     * @param player The player to check
     * @return The chest piece ItemStack, or EMPTY if not wearing Living Armor
     */
    public static ItemStack getChestPiece(Player player) {
        return getManager().getChestPiece(player);
    }

    /**
     * Get the level of a specific upgrade on the player's Living Armor.
     *
     * @param player    The player to check
     * @param upgradeId The ResourceLocation of the upgrade to check for
     * @return The upgrade level (0 if not present or player doesn't have valid Living Armor)
     */
    public static int getUpgradeLevel(Player player, ResourceLocation upgradeId) {
        return getManager().getUpgradeLevel(player, upgradeId);
    }

    /**
     * Check if the player has a specific upgrade at any level.
     *
     * @param player    The player to check
     * @param upgradeId The ResourceLocation of the upgrade to check for
     * @return true if the player has the upgrade at level 1 or higher
     */
    public static boolean hasUpgrade(Player player, ResourceLocation upgradeId) {
        return getUpgradeLevel(player, upgradeId) > 0;
    }

    /**
     * Add experience to a specific upgrade on the player's Living Armor.
     * The upgrade must be defined in the Blood Magic living_upgrades data pack.
     *
     * @param player    The player whose armor should receive XP
     * @param upgradeId The ResourceLocation of the upgrade to grant XP to
     * @param xpToAdd   The amount of XP to add
     * @return true if XP was successfully added
     */
    public static boolean addExperience(Player player, ResourceLocation upgradeId, float xpToAdd) {
        return getManager().grantUpgradeExperience(player, upgradeId, xpToAdd);
    }

    /**
     * Get the current XP for a specific upgrade on the player's Living Armor.
     *
     * @param player    The player to check
     * @param upgradeId The ResourceLocation of the upgrade to check for
     * @return The current XP amount (0 if not present or player doesn't have valid Living Armor)
     */
    public static float getExperience(Player player, ResourceLocation upgradeId) {
        return getManager().getUpgradeExperience(player, upgradeId);
    }

    /**
     * Get the number of upgrade points currently used by the player's Living Armor.
     *
     * @param player The player to check
     * @return The number of used upgrade points
     */
    public static int getUsedUpgradePoints(Player player) {
        return getManager().getUsedUpgradePoints(player);
    }

    /**
     * Get the maximum number of upgrade points available.
     *
     * @return The maximum upgrade points (typically 100)
     */
    public static int getMaxUpgradePoints() {
        return getManager().getMaxUpgradePoints();
    }

    /**
     * Get the number of available (unused) upgrade points for the player.
     *
     * @param player The player to check
     * @return The number of available upgrade points
     */
    public static int getAvailableUpgradePoints(Player player) {
        return getManager().getAvailableUpgradePoints(player);
    }
}
