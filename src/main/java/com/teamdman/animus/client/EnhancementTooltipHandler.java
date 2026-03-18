package com.teamdman.animus.client;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client-side tooltip handler that shows "Blood Enhanced" indicator on enhanced items.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.Mod.MODID)
public class EnhancementTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Boolean enhanced = stack.get(AnimusDataComponents.ANIMUS_ENHANCED.get());
        if (enhanced != null && enhanced) {
            event.getToolTip().add(Component.translatable("tooltip.animus.blood_enhanced")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        }
    }
}
