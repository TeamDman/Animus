package com.teamdman.animus.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.item.IActivatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility class for searching player inventories.
 * Consolidates repeated inventory iteration patterns across the codebase.
 */
public final class InventorySearchHelper {

    private InventorySearchHelper() {
        // Utility class - no instantiation
    }

    /**
     * Find the first ItemStack matching the predicate in player's inventory.
     * Searches main inventory, armor slots, and offhand.
     *
     * @param player The player to search
     * @param predicate The condition to match
     * @return Optional containing the first matching stack, or empty if none found
     */
    public static Optional<ItemStack> findFirst(Player player, Predicate<ItemStack> predicate) {
        Inventory inv = player.getInventory();

        // Check main inventory
        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }

        // Check armor slots
        for (ItemStack stack : inv.armor) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }

        // Check offhand
        for (ItemStack stack : inv.offhand) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }

        return Optional.empty();
    }

    /**
     * Find the first ItemStack of the specified item type.
     *
     * @param player The player to search
     * @param item The item type to find
     * @return Optional containing the first matching stack, or empty if none found
     */
    public static Optional<ItemStack> findFirst(Player player, Item item) {
        return findFirst(player, stack -> stack.is(item));
    }

    /**
     * Check if player has any ItemStack matching the predicate.
     *
     * @param player The player to search
     * @param predicate The condition to match
     * @return true if a matching stack exists
     */
    public static boolean hasItem(Player player, Predicate<ItemStack> predicate) {
        return findFirst(player, predicate).isPresent();
    }

    /**
     * Check if player has the specified item type.
     *
     * @param player The player to search
     * @param item The item type to find
     * @return true if the item exists in inventory
     */
    public static boolean hasItem(Player player, Item item) {
        return findFirst(player, item).isPresent();
    }

    /**
     * Check if player has an activated sigil/activatable item of the specified type.
     *
     * @param player The player to search
     * @param item The item type (must implement IActivatable)
     * @return true if an activated instance exists
     */
    public static boolean hasActiveItem(Player player, Item item) {
        return findFirst(player, stack -> {
            if (!stack.is(item)) return false;
            if (stack.getItem() instanceof IActivatable activatable) {
                return activatable.getActivated(stack);
            }
            return false;
        }).isPresent();
    }

    /**
     * Count items matching the predicate in player's inventory.
     *
     * @param player The player to search
     * @param predicate The condition to match
     * @return Total count of matching items (sum of stack sizes)
     */
    public static int countItems(Player player, Predicate<ItemStack> predicate) {
        Inventory inv = player.getInventory();
        int count = 0;

        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                count += stack.getCount();
            }
        }

        for (ItemStack stack : inv.armor) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                count += stack.getCount();
            }
        }

        for (ItemStack stack : inv.offhand) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    /**
     * Count stacks (not items) matching the predicate.
     * Useful for items with max stack size of 1.
     *
     * @param player The player to search
     * @param predicate The condition to match
     * @return Number of matching stacks
     */
    public static int countStacks(Player player, Predicate<ItemStack> predicate) {
        Inventory inv = player.getInventory();
        int count = 0;

        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                count++;
            }
        }

        for (ItemStack stack : inv.armor) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                count++;
            }
        }

        for (ItemStack stack : inv.offhand) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Count stacks of the specified item type.
     *
     * @param player The player to search
     * @param item The item type to count
     * @return Number of matching stacks
     */
    public static int countStacks(Player player, Item item) {
        return countStacks(player, stack -> stack.is(item));
    }

    /**
     * Find all ItemStacks matching the predicate.
     *
     * @param player The player to search
     * @param predicate The condition to match
     * @return List of all matching stacks (may be empty)
     */
    public static List<ItemStack> findAll(Player player, Predicate<ItemStack> predicate) {
        Inventory inv = player.getInventory();
        List<ItemStack> results = new ArrayList<>();

        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                results.add(stack);
            }
        }

        for (ItemStack stack : inv.armor) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                results.add(stack);
            }
        }

        for (ItemStack stack : inv.offhand) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                results.add(stack);
            }
        }

        return results;
    }

    /**
     * Find all ItemStacks of the specified item type.
     *
     * @param player The player to search
     * @param item The item type to find
     * @return List of all matching stacks
     */
    public static List<ItemStack> findAll(Player player, Item item) {
        return findAll(player, stack -> stack.is(item));
    }

    /**
     * Search only the main inventory (excludes armor and offhand).
     *
     * @param player The player to search
     * @param predicate The condition to match
     * @return Optional containing the first matching stack
     */
    public static Optional<ItemStack> findFirstInMainInventory(Player player, Predicate<ItemStack> predicate) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    /**
     * Search main inventory and offhand only (excludes armor).
     * This is a common pattern for "usable" items.
     *
     * @param player The player to search
     * @param predicate The condition to match
     * @return Optional containing the first matching stack
     */
    public static Optional<ItemStack> findFirstUsable(Player player, Predicate<ItemStack> predicate) {
        Inventory inv = player.getInventory();

        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }

        for (ItemStack stack : inv.offhand) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }

        return Optional.empty();
    }

    /**
     * Find first usable item of the specified type.
     *
     * @param player The player to search
     * @param item The item type to find
     * @return Optional containing the first matching stack
     */
    public static Optional<ItemStack> findFirstUsable(Player player, Item item) {
        return findFirstUsable(player, stack -> stack.is(item));
    }
}
