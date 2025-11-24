package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fragment of Healing - special item that cannot be dropped or used to break blocks
 */
public class ItemFragmentHealing extends Item {

    public ItemFragmentHealing() {
        super(new Item.Properties()
            .stacksTo(1)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HEALING_INFO));
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
}
