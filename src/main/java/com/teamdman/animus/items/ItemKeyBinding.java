package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.common.datacomponent.Binding;

import java.util.List;

/**
 * Key of Binding - A crafting component used in Animus recipes
 * This item can be bound to a player through Blood Magic's binding system
 * Can be equipped in the curio "key" slot for passive functionality
 */
public class ItemKeyBinding extends Item implements IBindable, ICurioItem {

    public ItemKeyBinding() {
        super(new Item.Properties()
            .stacksTo(1)
        );
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true; // Allow equipping directly from right-click
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.KEY));

        // Show owner name if bound
        Binding binding = getBinding(stack);
        if (binding != null && !binding.isEmpty()) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.OWNER, binding.name()));
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.KEY_CURIO)
                .withStyle(net.minecraft.ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.KEY_UNBOUND)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }
}
