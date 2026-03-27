package com.breakinblocks.animusnv.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.living.ILivingArmorManager;

/**
 * Wraps NeoVitae's Living Armor API for use by compat modules.
 */
public class LivingUpgradeHelper {

    private static ILivingArmorManager getManager() {
        return NeoVitaeAPI.getInstance().getLivingArmorManager();
    }

    public static boolean hasFullSet(Player player) {
        return getManager().hasFullSet(player);
    }

    public static ItemStack getChestPiece(Player player) {
        return getManager().getChestPiece(player);
    }

    public static int getUpgradeLevel(Player player, ResourceLocation upgradeId) {
        return getManager().getUpgradeLevel(player, upgradeId);
    }

    public static boolean hasUpgrade(Player player, ResourceLocation upgradeId) {
        return getUpgradeLevel(player, upgradeId) > 0;
    }

    public static boolean addExperience(Player player, ResourceLocation upgradeId, float xpToAdd) {
        return getManager().grantUpgradeExperience(player, upgradeId, xpToAdd);
    }

    public static float getExperience(Player player, ResourceLocation upgradeId) {
        return getManager().getUpgradeExperience(player, upgradeId);
    }

    public static int getUsedUpgradePoints(Player player) {
        return getManager().getUsedUpgradePoints(player);
    }

    public static int getMaxUpgradePoints() {
        return getManager().getMaxUpgradePoints();
    }

    /**
     * Takes into account armor evolution (e.g., evolved armor can have 300 points).
     */
    public static int getMaxUpgradePoints(Player player) {
        return getManager().getMaxUpgradePoints(player);
    }

    public static int getAvailableUpgradePoints(Player player) {
        return getManager().getAvailableUpgradePoints(player);
    }
}
