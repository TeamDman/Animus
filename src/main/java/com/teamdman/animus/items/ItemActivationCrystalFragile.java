package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neovitae.common.item.IBindable;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;

import java.util.List;

/**
 * Fragile Activation Crystal - A one-time use ritual activator
 * Works like a weak activation crystal but shatters after successfully activating a ritual
 */
public class ItemActivationCrystalFragile extends Item implements IBindable {

    public ItemActivationCrystalFragile() {
        super(new Item.Properties()
            .stacksTo(1)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof IMasterRitualStone masterRitualStone) {
            Binding binding = getBinding(stack);
            if (binding == null) {
                player.displayClientMessage(
                    Component.translatable("text.component.animus.activation_crystal.unbound")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return InteractionResult.FAIL;
            }

            com.breakinblocks.neovitae.ritual.Ritual ritual = masterRitualStone.getCurrentRitual();
            if (ritual == null) {
                return InteractionResult.FAIL;
            }

            // Crystal level 0 = weak tier
            boolean activated = masterRitualStone.activateRitual(ritual, player, 0);

            if (activated) {
                player.displayClientMessage(
                    Component.translatable("text.component.animus.activation_crystal.shattered")
                        .withStyle(ChatFormatting.GOLD),
                    true
                );
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_INFO));

        Binding binding = getBinding(stack);
        if (binding != null && !binding.isEmpty()) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.OWNER, binding.name())
                .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.UNBOUND_BIND)
                .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_WARNING)
            .withStyle(ChatFormatting.RED));

        super.appendHoverText(stack, context, tooltip, flag);
    }
}
