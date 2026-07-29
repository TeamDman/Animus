package com.breakinblocks.animusnv.events;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.items.ItemFragmentHealing;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class FragmentHealingEventHandler {

    private static final Map<UUID, Integer> healingCooldowns = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        // Count each stack as one fragment (max stack size is 1)
        int fragmentCount = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() == AnimusItems.FRAGMENT_HEALING.get() && !stack.isEmpty()) {
                fragmentCount++;
            }
        }

        if (fragmentCount == 0) {
            healingCooldowns.remove(playerId);
            return;
        }

        int currentCooldown = healingCooldowns.getOrDefault(playerId, 0);

        if (currentCooldown <= 0) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0F);
            }

            int healingInterval = ItemFragmentHealing.getHealingInterval(fragmentCount);
            healingCooldowns.put(playerId, healingInterval);
        } else {
            healingCooldowns.put(playerId, currentCooldown - 1);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getCrafting().getItem() == AnimusItems.FRAGMENT_HEALING.get()) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(
                    Component.translatable(Constants.Localizations.Text.HEALING_WARNING)
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }

    public static void cleanupPlayer(UUID playerId) {
        healingCooldowns.remove(playerId);
    }
}
