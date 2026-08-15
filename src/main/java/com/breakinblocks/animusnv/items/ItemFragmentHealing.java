package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Fragment of Healing - provides passive healing based on quantity in inventory
 * - Cannot be moved or dropped once in inventory (except on death)
 * - Heals 1 health every 200 ticks
 * - Each additional fragment reduces healing time by 5 ticks
 */
public class ItemFragmentHealing extends Item {
    public static final int BASE_HEALING_INTERVAL = 200; // 10 seconds
    public static final int REDUCTION_PER_FRAGMENT = 5;  // 0.25 seconds per fragment

    public ItemFragmentHealing() {
        super(new Item.Properties()
            .stacksTo(1)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_INFO));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_RATE));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_PERMANENT)
            .withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        if (!player.level().isClientSide) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.HEALING_CANNOT_DROP)
                    .withStyle(ChatFormatting.RED),
                true
            );
        }
        return false;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    public static int getHealingInterval(int fragmentCount) {
        if (fragmentCount <= 0) {
            return BASE_HEALING_INTERVAL;
        }
        // Each fragment after first reduces time by 5 ticks
        int reduction = (fragmentCount - 1) * REDUCTION_PER_FRAGMENT;
        int interval = BASE_HEALING_INTERVAL - reduction;
        // Minimum 20 ticks (1 second) to prevent too-rapid healing
        return Math.max(interval, 20);
    }
}
