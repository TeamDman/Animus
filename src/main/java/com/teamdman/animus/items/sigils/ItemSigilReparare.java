package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Sigil of Reparare - Repairs damaged items in inventory and equipped slots
 * <p>
 * Features:
 * - Right-click to toggle active/inactive
 * - While active, repairs damaged items periodically
 * - Respects the animus:disallow_repair tag
 * - Configurable repair amount, interval, and LP cost
 * <p>
 * Tick handler hooked up in AnimusEventHandler.onPlayerTick()
 */
public class ItemSigilReparare extends AnimusSigilBase {

    // Track active sigils per player - map of player UUID to (last repair tick, ItemStack)
    private static final Map<UUID, ActiveSigil> activeSigils = new HashMap<>();

    private static class ActiveSigil {
        ItemStack stack; // Not final so we can update the reference
        long lastRepairTick;

        ActiveSigil(ItemStack stack, long lastRepairTick) {
            this.stack = stack;
            this.lastRepairTick = lastRepairTick;
        }
    }

    public ItemSigilReparare() {
        super(Constants.Sigils.REPARARE, 0); // No cost on toggle, only on repair
    }

    /**
     * Toggle the sigil active state
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Check binding
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

        // Toggle active state
        boolean currentState = isActive(stack);
        setActive(stack, !currentState);

        // Update tracking
        if (!currentState) {
            // Activating
            if (level instanceof ServerLevel serverLevel) {
                activeSigils.put(player.getUUID(), new ActiveSigil(stack, serverLevel.getServer().getTickCount()));
            }
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.REPARARE_ACTIVATED)
                    .withStyle(ChatFormatting.GREEN),
                true
            );
        } else {
            // Deactivating
            activeSigils.remove(player.getUUID());
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.REPARARE_DEACTIVATED)
                    .withStyle(ChatFormatting.RED),
                true
            );
        }

        return InteractionResultHolder.success(stack);
    }

    /**
     * Check if this sigil is active
     */
    public static boolean isActive(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getBoolean("Active");
        }
        return false;
    }

    /**
     * Set the active state of this sigil
     */
    private void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("Active", active);
    }

    /**
     * Process active sigils - should be called from player tick event
     */
    public static void tickActiveSigils(Player player, ServerLevel level) {
        UUID playerId = player.getUUID();
        ActiveSigil activeSigil = activeSigils.get(playerId);

        if (activeSigil == null) {
            return;
        }

        // Verify the player still has the active sigil and update the reference
        // Search all inventory slots including armor and offhand
        boolean hasActiveSigil = false;
        ItemStack currentActiveSigil = null;

        // Check main inventory and hotbar
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemSigilReparare && isActive(stack)) {
                hasActiveSigil = true;
                currentActiveSigil = stack;
                break;
            }
        }

        // Check armor slots if not found
        if (!hasActiveSigil) {
            for (ItemStack stack : player.getInventory().armor) {
                if (stack.getItem() instanceof ItemSigilReparare && isActive(stack)) {
                    hasActiveSigil = true;
                    currentActiveSigil = stack;
                    break;
                }
            }
        }

        // Check offhand if not found
        if (!hasActiveSigil) {
            for (ItemStack stack : player.getInventory().offhand) {
                if (stack.getItem() instanceof ItemSigilReparare && isActive(stack)) {
                    hasActiveSigil = true;
                    currentActiveSigil = stack;
                    break;
                }
            }
        }

        if (!hasActiveSigil || currentActiveSigil == null) {
            // Player no longer has active sigil
            activeSigils.remove(playerId);
            return;
        }

        // Update the stored reference to the current ItemStack
        activeSigil.stack = currentActiveSigil;

        // Check if it's time to repair
        long currentTick = level.getServer().getTickCount();
        int repairInterval = AnimusConfig.sigils.reparareInterval.get();
        if (currentTick - activeSigil.lastRepairTick < repairInterval) {
            return;
        }

        // Time to repair!
        activeSigil.lastRepairTick = currentTick;

        // Collect all repairable items
        List<ItemStack> repairableItems = new ArrayList<>();

        // Check inventory
        for (ItemStack stack : player.getInventory().items) {
            if (canRepair(stack)) {
                repairableItems.add(stack);
            }
        }

        // Check armor
        for (ItemStack stack : player.getInventory().armor) {
            if (canRepair(stack)) {
                repairableItems.add(stack);
            }
        }

        // Check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (canRepair(stack)) {
                repairableItems.add(stack);
            }
        }

        if (repairableItems.isEmpty()) {
            return;
        }

        // Calculate total repair needed
        int maxRepairPerItem = AnimusConfig.sigils.reparareRepairAmount.get();
        int totalDamageToRepair = 0;
        Map<ItemStack, Integer> repairPlan = new HashMap<>();

        for (ItemStack stack : repairableItems) {
            int damage = stack.getDamageValue();
            int toRepair = Math.min(damage, maxRepairPerItem);
            if (toRepair > 0) {
                repairPlan.put(stack, toRepair);
                totalDamageToRepair += toRepair;
            }
        }

        if (totalDamageToRepair == 0) {
            return;
        }

        // Calculate LP cost
        int lpPerDamage = AnimusConfig.sigils.reparareLPPerDamage.get();
        int lpCost = totalDamageToRepair * lpPerDamage;

        // Try to consume LP from soul network
        wayoftime.bloodmagic.core.data.SoulNetwork network = wayoftime.bloodmagic.util.helper.NetworkHelper.getSoulNetwork(player);
        wayoftime.bloodmagic.core.data.SoulTicket ticket = new wayoftime.bloodmagic.core.data.SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_REPARARE),
            lpCost
        );

        var syphonResult = network.syphonAndDamage(player, ticket);
        if (!syphonResult.isSuccess()) {
            // Not enough LP - deactivate sigil
            setActiveStatic(activeSigil.stack, false);
            activeSigils.remove(playerId);
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.REPARARE_NO_LP)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Apply repairs
        int itemsRepaired = 0;
        for (Map.Entry<ItemStack, Integer> entry : repairPlan.entrySet()) {
            ItemStack stack = entry.getKey();
            int repairAmount = entry.getValue();
            stack.setDamageValue(stack.getDamageValue() - repairAmount);
            itemsRepaired++;
        }


        player.displayClientMessage(
           Component.literal("Repaired " + itemsRepaired + " items for " + lpCost + " LP"),
           true
        );
    }

    /**
     * Check if an item can be repaired
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
     * Static version of setActive for use in tick handler
     */
    private static void setActiveStatic(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("Active", active);
    }

    /**
     * Clean up when player logs out
     */
    public static void onPlayerLogout(UUID playerId) {
        activeSigils.remove(playerId);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REPARARE_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REPARARE_INFO));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REPARARE_COST,
            AnimusConfig.sigils.reparareLPPerDamage.get()));

        if (isActive(stack)) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REPARARE_ACTIVE)
                .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REPARARE_INACTIVE)
                .withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
