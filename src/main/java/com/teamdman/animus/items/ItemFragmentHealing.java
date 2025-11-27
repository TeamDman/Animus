package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fragment of Healing - provides passive healing based on quantity in inventory
 * - Cannot be dropped (Curse of Binding)
 * - Heals 1 health every 200 ticks
 * - Each additional fragment reduces healing time by 5 ticks
 */
public class ItemFragmentHealing extends Item {
    public static final int BASE_HEALING_INTERVAL = 200; // 10 seconds
    public static final int REDUCTION_PER_FRAGMENT = 5;  // 0.25 seconds per fragment

    public ItemFragmentHealing() {
        super(new Item.Properties()
            .stacksTo(64) // Allow stacking for the mechanic to work
        );
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        applyBindingCurse(stack);
    }

    /**
     * Apply Curse of Binding to prevent dropping
     */
    private void applyBindingCurse(ItemStack stack) {
        stack.enchant(Enchantments.BINDING_CURSE, 1);
    }

    /**
     * When the item is first added to inventory, apply curse
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // Ensure curse is applied
        if (!stack.isEnchanted()) {
            applyBindingCurse(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_INFO));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_RATE));
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        // Only allow dropping in creative mode
        return player.getAbilities().instabuild;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, net.minecraft.core.BlockPos pos, Player player) {
        // Prevent breaking blocks with this item
        return false;
    }

    /**
     * Calculate healing interval based on number of fragments
     * @param fragmentCount Number of healing fragments in inventory
     * @return Ticks between healing
     */
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
