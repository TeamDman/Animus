package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.sigil.ISigilEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sigil of Reparare - repairs damaged items in inventory and equipped slots.
 * While active, repairs damaged items periodically.
 * Respects the animus:disallow_repair tag.
 */
public record RepareSigilEffect() implements ISigilEffect {
    public static final MapCodec<RepareSigilEffect> CODEC = MapCodec.unit(RepareSigilEffect::new);

    // Track last repair tick per player
    private static final Map<UUID, Long> lastRepairTime = new ConcurrentHashMap<>();

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean isToggleable() {
        return true;
    }

    @Override
    public void activeTick(Level level, Player player, ItemStack stack, int itemSlot, boolean isSelected) {
        if (level.isClientSide) {
            return;
        }

        // Check if enough time has passed
        long currentTime = level.getGameTime();
        int repairInterval = AnimusConfig.sigils.reparareInterval.get();
        Long lastRepair = lastRepairTime.get(player.getUUID());
        if (lastRepair != null && currentTime - lastRepair < repairInterval) {
            return;
        }

        // Collect all repairable items
        List<ItemStack> repairableItems = new ArrayList<>();

        // Check main inventory
        for (ItemStack s : player.getInventory().items) {
            if (canRepair(s)) {
                repairableItems.add(s);
            }
        }

        // Check armor
        for (ItemStack s : player.getInventory().armor) {
            if (canRepair(s)) {
                repairableItems.add(s);
            }
        }

        // Check offhand
        for (ItemStack s : player.getInventory().offhand) {
            if (canRepair(s)) {
                repairableItems.add(s);
            }
        }

        if (repairableItems.isEmpty()) {
            return;
        }

        // Calculate repairs
        int maxRepairPerItem = AnimusConfig.sigils.reparareRepairAmount.get();

        // Apply repairs
        // Note: LP cost is handled by the sigil system based on sigil_type JSON
        for (ItemStack repairStack : repairableItems) {
            int damage = repairStack.getDamageValue();
            int toRepair = Math.min(damage, maxRepairPerItem);
            if (toRepair > 0) {
                repairStack.setDamageValue(damage - toRepair);
            }
        }

        lastRepairTime.put(player.getUUID(), currentTime);
    }

    /**
     * Check if an item can be repaired.
     */
    private static boolean canRepair(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        // Check if item has durability
        if (!stack.isDamageableItem()) {
            return false;
        }

        // Check if item is damaged
        if (stack.getDamageValue() <= 0) {
            return false;
        }

        // Check if item is blacklisted
        if (stack.is(Constants.Tags.DISALLOW_REPAIR)) {
            return false;
        }

        return true;
    }

    /**
     * Clean up tracking data when player logs out.
     */
    public static void onPlayerLogout(UUID playerId) {
        lastRepairTime.remove(playerId);
    }
}
