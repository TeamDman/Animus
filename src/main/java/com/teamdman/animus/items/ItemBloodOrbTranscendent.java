package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import wayoftime.bloodmagic.common.item.BloodOrbItem;

import java.util.List;

/**
 * Transcendent Blood Orb - Tier 7 Blood Orb
 * Holds 30,000,000 LP
 * Crafted in a Tier 6 altar with Crystallized Demon Will Block
 *
 * Stats are defined via DataMaps in data/animus/data_maps/item/blood_orb_stats.json
 */
public class ItemBloodOrbTranscendent extends BloodOrbItem {

    public ItemBloodOrbTranscendent() {
        // In Blood Magic 1.21.1, orb stats come from DataMaps, not constructor args
        super();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_ORB_TRANSCENDENT_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    }
}
