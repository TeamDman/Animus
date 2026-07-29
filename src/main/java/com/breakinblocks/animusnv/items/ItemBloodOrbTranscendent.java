package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;

import java.util.function.Consumer;

/**
 * Transcendent Orb of Vitae - Tier 6 Orb of Vitae
 * Holds 30,000,000 EV
 * Crafted in a Tier 6 altar with Crystallized Spiritus Block
 *
 * Stats are defined via DataMaps in data/animus/data_maps/item/blood_orb_stats.json
 */
public class ItemBloodOrbTranscendent extends BloodOrbItem {

    public ItemBloodOrbTranscendent(Item.Properties props) {
        super(props);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_ORB_TRANSCENDENT_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    }
}
