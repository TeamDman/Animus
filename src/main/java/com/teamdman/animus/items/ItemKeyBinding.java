package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Key of Binding - A crafting component used in Animus recipes
 * This item can be bound to a player for recipes requiring bound items
 * TODO: Implement IBindable interface from Blood Magic when available
 */
public class ItemKeyBinding extends Item {

    public ItemKeyBinding() {
        super(new Item.Properties()
            .stacksTo(1)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.KEY));

        // TODO: Implement binding system
        // When Blood Magic IBindable is integrated, show owner name:
        // Binding binding = getBinding(stack);
        // if (binding != null) {
        //     tooltip.add(Component.translatable(Constants.Localizations.Tooltips.OWNER, binding.getOwnerName()));
        // }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
