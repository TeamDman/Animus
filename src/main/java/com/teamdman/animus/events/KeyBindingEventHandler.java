package com.teamdman.animus.events;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.common.item.IBindable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event handler for Key of Binding functionality
 * Allows transferring binding ownership from key holder to items
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class KeyBindingEventHandler {

    // Track pending binding transfers
    private static final Map<UUID, Binding> pendingBindingTransfers = new HashMap<>();

    /**
     * Key of Binding functionality: Transfer binding ownership
     * When a player has a bound Key of Binding in their offhand and binds an item,
     * the item gets bound to the Key's owner instead of the player
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        // Only run on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);

        // Check if player has Key of Binding in offhand
        if (offHandStack.getItem() != AnimusItems.KEY_BINDING.get()) {
            return;
        }

        // Check if the Key is bound
        if (!(offHandStack.getItem() instanceof IBindable keyBindable)) {
            return;
        }

        Binding keyBinding = keyBindable.getBinding(offHandStack);
        if (keyBinding == null) {
            // Key is not bound, no effect
            return;
        }

        // Check if main hand item is an IBindable
        if (!(mainHandStack.getItem() instanceof IBindable itemBindable)) {
            return;
        }

        // Prevent Key of Binding from binding another Key of Binding
        if (mainHandStack.getItem() == AnimusItems.KEY_BINDING.get()) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.KEY_CANNOT_BIND_KEY)
                    .withStyle(ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        // Check if the item is not already bound
        Binding itemBinding = itemBindable.getBinding(mainHandStack);
        if (itemBinding != null) {
            // Item is already bound, don't interfere
            return;
        }

        // Schedule the binding transfer for next tick
        // This allows Blood Magic's binding to complete first
        pendingBindingTransfers.put(player.getUUID(), keyBinding);
    }

    /**
     * Complete pending binding transfers from Key of Binding
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        // Only run on server side
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        // Check if this player has a pending binding transfer
        Binding keyBinding = pendingBindingTransfers.remove(playerId);
        if (keyBinding == null) {
            return;
        }

        // Find any newly bound items in player's inventory and transfer the binding
        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (mainHandStack.getItem() instanceof IBindable bindable) {
            Binding currentBinding = bindable.getBinding(mainHandStack);

            // Check if the item was just bound to this player
            if (currentBinding != null && currentBinding.uuid().equals(playerId)) {
                // Transfer the binding to the Key's owner using DataComponents
                mainHandStack.set(NVDataComponents.BINDING.get(), keyBinding);

                // Notify player
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.KEY_ITEM_BOUND, keyBinding.name())
                        .withStyle(ChatFormatting.AQUA),
                    true
                );
            }
        }
    }
}
