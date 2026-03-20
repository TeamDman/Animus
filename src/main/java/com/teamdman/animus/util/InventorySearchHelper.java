package com.teamdman.animus.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.common.item.IActivatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class InventorySearchHelper {

    private InventorySearchHelper() {
    }

    /**
     * Searches main inventory, armor slots, and offhand.
     */
    public static Optional<ItemStack> findFirst(Player player, Predicate<ItemStack> predicate) {
        Inventory inv = player.getInventory();

        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }

        for (ItemStack stack : inv.armor) {
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

    public static Optional<ItemStack> findFirst(Player player, Item item) {
        return findFirst(player, stack -> stack.is(item));
    }

    public static boolean hasItem(Player player, Predicate<ItemStack> predicate) {
        return findFirst(player, predicate).isPresent();
    }

    public static boolean hasItem(Player player, Item item) {
        return findFirst(player, item).isPresent();
    }

    public static boolean hasActiveItem(Player player, Item item) {
        return findFirst(player, stack -> {
            if (!stack.is(item)) return false;
            if (stack.getItem() instanceof IActivatable activatable) {
                return activatable.getActivated(stack);
            }
            return false;
        }).isPresent();
    }

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

    public static int countStacks(Player player, Item item) {
        return countStacks(player, stack -> stack.is(item));
    }

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

    public static List<ItemStack> findAll(Player player, Item item) {
        return findAll(player, stack -> stack.is(item));
    }

    public static Optional<ItemStack> findFirstInMainInventory(Player player, Predicate<ItemStack> predicate) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    /**
     * Searches main inventory and offhand only (excludes armor).
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

    public static Optional<ItemStack> findFirstUsable(Player player, Item item) {
        return findFirstUsable(player, stack -> stack.is(item));
    }
}
