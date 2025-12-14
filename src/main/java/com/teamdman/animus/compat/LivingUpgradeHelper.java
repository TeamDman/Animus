package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.LivingStats;
import wayoftime.bloodmagic.common.living.LivingHelper;
import wayoftime.bloodmagic.common.living.LivingUpgrade;
import wayoftime.bloodmagic.common.registry.BMRegistries;

import java.util.Optional;

/**
 * Helper class for checking and modifying Living Armor upgrade levels
 * Used by compat modules to check for Animus-specific upgrades
 */
public class LivingUpgradeHelper {

    /**
     * Get the level of a specific upgrade on the player's Living Armor
     *
     * @param player     The player to check
     * @param upgradeKey The ResourceKey of the upgrade to check for
     * @return The upgrade level (0 if not present or player doesn't have valid Living Armor)
     */
    public static int getUpgradeLevel(Player player, ResourceKey<LivingUpgrade> upgradeKey) {
        // Check if player has a valid Living Armor set
        if (!LivingHelper.hasFullSet(player)) {
            return 0;
        }

        ItemStack chest = LivingHelper.getChest(player);
        Object2FloatOpenHashMap<Holder<LivingUpgrade>> upgrades = chest.getOrDefault(
            BMDataComponents.UPGRADES,
            LivingStats.EMPTY
        ).upgrades();

        for (Object2FloatMap.Entry<Holder<LivingUpgrade>> entry : upgrades.object2FloatEntrySet()) {
            if (entry.getKey().is(upgradeKey)) {
                return LivingHelper.getLevelFromXp(entry.getKey(), entry.getFloatValue());
            }
        }

        return 0;
    }

    /**
     * Check if the player has a specific upgrade at any level
     *
     * @param player     The player to check
     * @param upgradeKey The ResourceKey of the upgrade to check for
     * @return true if the player has the upgrade at level 1 or higher
     */
    public static boolean hasUpgrade(Player player, ResourceKey<LivingUpgrade> upgradeKey) {
        return getUpgradeLevel(player, upgradeKey) > 0;
    }

    /**
     * Add experience to a specific upgrade on the player's Living Armor
     * The upgrade must be defined in the Blood Magic living_upgrades data pack.
     *
     * @param player     The player whose armor should receive XP
     * @param upgradeKey The ResourceKey of the upgrade to grant XP to
     * @param xpToAdd    The amount of XP to add
     * @return true if XP was successfully added
     */
    public static boolean addExperience(Player player, ResourceKey<LivingUpgrade> upgradeKey, float xpToAdd) {
        // Check if player has a valid Living Armor set
        if (!LivingHelper.hasFullSet(player)) {
            return false;
        }

        if (player.level().isClientSide()) {
            return false;
        }

        ItemStack chest = LivingHelper.getChest(player);
        if (chest.isEmpty()) {
            return false;
        }

        // Get the living upgrade registry
        Registry<LivingUpgrade> registry = player.level().registryAccess()
            .registryOrThrow(BMRegistries.Keys.LIVING_UPGRADES);

        // Find the upgrade holder
        Optional<Holder.Reference<LivingUpgrade>> upgradeHolderOpt = registry.getHolder(upgradeKey);
        if (upgradeHolderOpt.isEmpty()) {
            Animus.LOGGER.debug("Could not find living upgrade: {}", upgradeKey.location());
            return false;
        }

        Holder<LivingUpgrade> upgradeHolder = upgradeHolderOpt.get();

        // Get current stats
        LivingStats currentStats = chest.getOrDefault(BMDataComponents.UPGRADES, LivingStats.EMPTY);
        Object2FloatOpenHashMap<Holder<LivingUpgrade>> upgrades = new Object2FloatOpenHashMap<>(currentStats.upgrades());

        // Get current XP and add to it
        float currentXp = upgrades.getFloat(upgradeHolder);
        float newXp = currentXp + xpToAdd;
        upgrades.put(upgradeHolder, newXp);

        // Create new stats and store on item
        LivingStats newStats = new LivingStats(upgrades);
        chest.set(BMDataComponents.UPGRADES, newStats);

        return true;
    }

    /**
     * Get the current XP for a specific upgrade on the player's Living Armor
     *
     * @param player     The player to check
     * @param upgradeKey The ResourceKey of the upgrade to check for
     * @return The current XP amount (0 if not present or player doesn't have valid Living Armor)
     */
    public static float getExperience(Player player, ResourceKey<LivingUpgrade> upgradeKey) {
        if (!LivingHelper.hasFullSet(player)) {
            return 0;
        }

        ItemStack chest = LivingHelper.getChest(player);
        Object2FloatOpenHashMap<Holder<LivingUpgrade>> upgrades = chest.getOrDefault(
            BMDataComponents.UPGRADES,
            LivingStats.EMPTY
        ).upgrades();

        for (Object2FloatMap.Entry<Holder<LivingUpgrade>> entry : upgrades.object2FloatEntrySet()) {
            if (entry.getKey().is(upgradeKey)) {
                return entry.getFloatValue();
            }
        }

        return 0;
    }
}
