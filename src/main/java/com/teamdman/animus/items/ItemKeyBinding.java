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
 * This item can be bound to a player through Blood Magic's binding system
 * TODO: Investigate if IBindable interface exists in Blood Magic 1.20.1
 * If not, binding may be handled through NBT data directly
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

        // TODO: Add binding display when Blood Magic binding system is integrated
        // May need to check NBT for binding data if IBindable interface no longer exists

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
