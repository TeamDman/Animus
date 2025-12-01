package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBloodOrbs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.item.ItemBloodOrb;

import java.util.List;

/**
 * Transcendent Blood Orb - Tier 6 Blood Orb
 * Holds 300,000 LP
 * Crafted in a Tier 6 altar with Crystallized Demon Will Block
 */
public class ItemBloodOrbTranscendent extends ItemBloodOrb {
    private static final int CAPACITY = 300000;

    public ItemBloodOrbTranscendent() {
        super(AnimusBloodOrbs.BLOOD_ORB_TRANSCENDENT::get);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_ORB_TRANSCENDENT_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_ORB_TRANSCENDENT_INFO, CAPACITY)
            .withStyle(ChatFormatting.DARK_RED));
    }
}
