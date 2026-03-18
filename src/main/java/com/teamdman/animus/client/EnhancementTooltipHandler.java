package com.teamdman.animus.client;

import com.teamdman.animus.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

/**
 * Client-side tooltip handler for Blood Enhanced items.
 * Replaces enchantment tooltip lines with boosted versions (+1 level)
 * and adds a "Blood Enhanced" indicator line.
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EnhancementTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.hasTag() || !stack.getTag().getBoolean("AnimusEnhanced")) {
            return;
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        int lastEnchantIndex = -1;

        // Replace each enchantment line with the boosted version
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment ench = entry.getKey();
            int baseLevel = entry.getValue();

            // Generate the vanilla tooltip text (base level) and the boosted text
            String baseName = ench.getFullname(baseLevel).getString();
            Component boostedName = ench.getFullname(baseLevel + 1);

            for (int i = 0; i < tooltip.size(); i++) {
                if (tooltip.get(i).getString().equals(baseName)) {
                    tooltip.set(i, boostedName);
                    lastEnchantIndex = i;
                    break;
                }
            }
        }

        // Add the "Blood Enhanced" indicator after the last enchantment line
        if (lastEnchantIndex >= 0) {
            tooltip.add(lastEnchantIndex + 1,
                Component.translatable("tooltip.animus.blood_enhanced")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
