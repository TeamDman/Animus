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
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.ritual.IMasterRitualStone;

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

        // Check if the block entity is a Master Ritual Stone
        if (level.getBlockEntity(pos) instanceof IMasterRitualStone masterRitualStone) {
            // Check if crystal is bound
            Binding binding = getBinding(stack);
            if (binding == null) {
                player.displayClientMessage(
                    Component.translatable("text.component.animus.activation_crystal.unbound")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return InteractionResult.FAIL;
            }

            // Get the ritual from the master ritual stone
            wayoftime.bloodmagic.ritual.Ritual ritual = masterRitualStone.getCurrentRitual();
            if (ritual == null) {
                // No ritual set in this master ritual stone
                return InteractionResult.FAIL;
            }

            // Try to activate the ritual
            boolean activated = masterRitualStone.activateRitual(stack, player, ritual);

            if (activated) {
                // Ritual activated successfully - shatter the crystal
                player.displayClientMessage(
                    Component.translatable("text.component.animus.activation_crystal.shattered")
                        .withStyle(ChatFormatting.GOLD),
                    true
                );
                stack.shrink(1); // Consume the crystal
                return InteractionResult.SUCCESS;
            } else {
                // Activation failed (not enough LP, etc.)
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_INFO));

        // Show owner name if bound
        Binding binding = getBinding(stack);
        if (binding != null) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.OWNER, binding.getOwnerName())
                .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.literal("Unbound - Right-click to bind")
                .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.ACTIVATION_CRYSTAL_WARNING)
            .withStyle(ChatFormatting.RED));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
