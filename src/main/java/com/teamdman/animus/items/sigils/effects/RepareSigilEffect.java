package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.util.InventorySearchHelper;
import com.teamdman.animus.util.SigilStateTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.sigil.ISigilEffect;

import java.util.List;

/**
 * Sigil of Reparare - repairs damaged items in inventory and equipped slots.
 * While active, repairs damaged items periodically.
 * Respects the animus:disallow_repair tag.
 */
public record RepareSigilEffect() implements ISigilEffect {
    public static final MapCodec<RepareSigilEffect> CODEC = MapCodec.unit(RepareSigilEffect::new);

    // Track last repair tick per player (auto-registered for cleanup)
    private static final SigilStateTracker TRACKER = new SigilStateTracker("reparare");

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

        // Check if enough time has passed using the tracker
        long currentTime = level.getGameTime();
        int repairInterval = AnimusConfig.sigils.reparareInterval.get();
        if (!TRACKER.isReady(player.getUUID(), currentTime, repairInterval)) {
            return;
        }

        // Collect all repairable items using InventorySearchHelper
        List<ItemStack> repairableItems = InventorySearchHelper.findAll(player, RepareSigilEffect::canRepair);

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

        TRACKER.updateTime(player.getUUID(), currentTime);
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

    // Cleanup is handled automatically by SigilStateCleanupManager via TRACKER registration
}
