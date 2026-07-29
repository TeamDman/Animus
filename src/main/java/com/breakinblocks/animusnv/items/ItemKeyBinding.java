package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import com.breakinblocks.neovitae.common.item.IBindable;
import com.breakinblocks.neovitae.common.datacomponent.Binding;

import java.util.function.Consumer;

/**
 * Key of Binding - A crafting component used in Animus recipes
 * This item can be bound to a player through NeoVitae's binding system
 * Can be equipped in the curio "key" slot for passive functionality
 */
public class ItemKeyBinding extends Item implements IBindable, ICurioItem {

    public ItemKeyBinding(Item.Properties props) {
        super(props
            .stacksTo(1)
        );
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.KEY));

        Binding binding = getBinding(stack);
        if (binding != null && !binding.isEmpty()) {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.OWNER, binding.name()));
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.KEY_CURIO)
                .withStyle(net.minecraft.ChatFormatting.AQUA));
        } else {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.KEY_UNBOUND)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
