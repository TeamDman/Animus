package com.breakinblocks.animusnv.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.common.item.IActivatable;
import com.breakinblocks.neovitae.common.item.sigil.ItemSigilHolding;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class InventorySearchHelper {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    private InventorySearchHelper() {
    }

    private static List<ItemStack> armorItems(Player player) {
        List<ItemStack> items = new ArrayList<>(ARMOR_SLOTS.length);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            items.add(player.getItemBySlot(slot));
        }
        return items;
    }

    private static List<ItemStack> offhandItems(Player player) {
        return List.of(player.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    /**
     * Iterates all non-empty items across main inventory, armor, and offhand.
     */
    private static void forEachItem(Player player, Consumer<ItemStack> consumer) {
        Inventory inv = player.getInventory();
        for (ItemStack stack : inv.getNonEquipmentItems()) {
            if (!stack.isEmpty()) consumer.accept(stack);
        }
        for (ItemStack stack : armorItems(player)) {
            if (!stack.isEmpty()) consumer.accept(stack);
        }
        for (ItemStack stack : offhandItems(player)) {
            if (!stack.isEmpty()) consumer.accept(stack);
        }
    }

    /**
     * Searches main inventory, armor slots, and offhand.
     */
    public static Optional<ItemStack> findFirst(Player player, Predicate<ItemStack> predicate) {
        Inventory inv = player.getInventory();
        for (List<ItemStack> list : List.of(inv.getNonEquipmentItems(), armorItems(player), offhandItems(player))) {
            for (ItemStack stack : list) {
                if (!stack.isEmpty() && predicate.test(stack)) {
                    return Optional.of(stack);
                }
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
        return findActiveSigil(player, item).isPresent();
    }

    /**
     * Finds an active (toggled-on) sigil of the given type in the player's inventory.
     * Searches main inventory and offhand.
     */
    public static Optional<ItemStack> findActiveSigil(Player player, Item sigilItem) {
        return findFirstUsable(player, stack -> {
            if (!stack.is(sigilItem)) return false;
            if (stack.getItem() instanceof IActivatable activatable) {
                return activatable.getActivated(stack);
            }
            return false;
        });
    }

    public static int countItems(Player player, Predicate<ItemStack> predicate) {
        int[] count = {0};
        forEachItem(player, stack -> {
            if (predicate.test(stack)) {
                count[0] += stack.getCount();
            }
        });
        return count[0];
    }

    public static int countStacks(Player player, Predicate<ItemStack> predicate) {
        int[] count = {0};
        forEachItem(player, stack -> {
            if (predicate.test(stack)) {
                count[0]++;
            }
        });
        return count[0];
    }

    public static int countStacks(Player player, Item item) {
        return countStacks(player, stack -> stack.is(item));
    }

    public static List<ItemStack> findAll(Player player, Predicate<ItemStack> predicate) {
        List<ItemStack> results = new ArrayList<>();
        forEachItem(player, stack -> {
            if (predicate.test(stack)) {
                results.add(stack);
            }
        });
        return results;
    }

    public static List<ItemStack> findAll(Player player, Item item) {
        return findAll(player, stack -> stack.is(item));
    }

    public static Optional<ItemStack> findFirstInMainInventory(Player player, Predicate<ItemStack> predicate) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    /**
     * Searches main inventory and offhand only (excludes armor).
     * Also searches inside Sigil of Holding containers.
     */
    public static Optional<ItemStack> findFirstUsable(Player player, Predicate<ItemStack> predicate) {
        Inventory inv = player.getInventory();

        for (ItemStack stack : inv.getNonEquipmentItems()) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
            // Check inside Sigil of Holding
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSigilHolding) {
                NonNullList<ItemStack> holdingInv = ItemSigilHolding.getInternalInventory(stack);
                for (ItemStack heldStack : holdingInv) {
                    if (!heldStack.isEmpty() && predicate.test(heldStack)) {
                        return Optional.of(heldStack);
                    }
                }
            }
        }

        for (ItemStack stack : offhandItems(player)) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(stack);
            }
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSigilHolding) {
                NonNullList<ItemStack> holdingInv = ItemSigilHolding.getInternalInventory(stack);
                for (ItemStack heldStack : holdingInv) {
                    if (!heldStack.isEmpty() && predicate.test(heldStack)) {
                        return Optional.of(heldStack);
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<ItemStack> findFirstUsable(Player player, Item item) {
        return findFirstUsable(player, stack -> stack.is(item));
    }
}
