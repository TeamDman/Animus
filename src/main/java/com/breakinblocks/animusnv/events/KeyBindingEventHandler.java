package com.breakinblocks.animusnv.events;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusItems;
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
 * When a player has a bound Key of Binding in their offhand and binds an item,
 * the item gets bound to the Key's owner instead of the player.
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class KeyBindingEventHandler {

    private static final Map<UUID, Binding> pendingBindingTransfers = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);

        if (offHandStack.getItem() != AnimusItems.KEY_BINDING.get()) {
            return;
        }

        if (!(offHandStack.getItem() instanceof IBindable keyBindable)) {
            return;
        }

        Binding keyBinding = keyBindable.getBinding(offHandStack);
        if (keyBinding == null) {
            return;
        }

        if (!(mainHandStack.getItem() instanceof IBindable itemBindable)) {
            return;
        }

        if (mainHandStack.getItem() == AnimusItems.KEY_BINDING.get()) {
            player.sendOverlayMessage(
                Component.translatable(Constants.Localizations.Text.KEY_CANNOT_BIND_KEY)
                    .withStyle(ChatFormatting.RED));
            event.setCanceled(true);
            return;
        }

        Binding itemBinding = itemBindable.getBinding(mainHandStack);
        if (itemBinding != null) {
            return;
        }

        // Deferred to next tick so NeoVitae's binding completes first
        pendingBindingTransfers.put(player.getUUID(), keyBinding);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        Binding keyBinding = pendingBindingTransfers.remove(playerId);
        if (keyBinding == null) {
            return;
        }

        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (mainHandStack.getItem() instanceof IBindable bindable) {
            Binding currentBinding = bindable.getBinding(mainHandStack);

            if (currentBinding != null && currentBinding.uuid().equals(playerId)) {
                mainHandStack.set(NVDataComponents.BINDING.get(), keyBinding);
                player.sendOverlayMessage(
                    Component.translatable(Constants.Localizations.Text.KEY_ITEM_BOUND, keyBinding.name())
                        .withStyle(ChatFormatting.AQUA));
            }
        }
    }
}
