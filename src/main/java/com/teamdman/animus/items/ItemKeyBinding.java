package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;

import java.util.List;

/**
 * Key of Binding - A crafting component used in Animus recipes
 * This item can be bound to a player through Blood Magic's binding system
 */
public class ItemKeyBinding extends Item implements IBindable {

    public ItemKeyBinding() {
        super(new Item.Properties()
            .stacksTo(1)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.KEY));

        // Show owner name if bound
        Binding binding = getBinding(stack);
        if (binding != null) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.OWNER, binding.getOwnerName()));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
