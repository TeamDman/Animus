package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neovitae.common.item.IBindable;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;
import com.breakinblocks.neovitae.ritual.Ritual;

import java.util.function.Consumer;

/**
 * Fragile Activation Crystal - A one-time use ritual activator
 * Works like a weak activation crystal but shatters after successfully activating a ritual
 */
public class ItemActivationCrystalFragile extends Item implements IBindable {

    public ItemActivationCrystalFragile(Item.Properties props) {
        super(props
            .stacksTo(1)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof IMasterRitualStone masterRitualStone) {
            Binding binding = getBinding(stack);
            if (binding == null) {
                player.sendOverlayMessage(
                    Component.translatable("text.component.animusnv.activation_crystal.unbound")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            Ritual ritual = masterRitualStone.getCurrentRitual();
            if (ritual == null) {
                return InteractionResult.FAIL;
            }

            // Crystal level 0 = weak tier
            boolean activated = masterRitualStone.activateRitual(ritual, player, 0);

            if (activated) {
                player.sendOverlayMessage(
                    Component.translatable("text.component.animusnv.activation_crystal.shattered")
                        .withStyle(ChatFormatting.GOLD));
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_FLAVOUR));
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_INFO));

        Binding binding = getBinding(stack);
        if (binding != null && !binding.isEmpty()) {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.OWNER, binding.name())
                .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.UNBOUND_BIND)
                .withStyle(ChatFormatting.GRAY));
        }

        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_WARNING)
            .withStyle(ChatFormatting.RED));

        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
