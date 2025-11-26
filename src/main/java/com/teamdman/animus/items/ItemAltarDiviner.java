package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import wayoftime.bloodmagic.altar.AltarTier;
import wayoftime.bloodmagic.common.tile.TileAltar;

import java.util.List;

/**
 * Altar Diviner - Displays information about Blood Magic altars
 * <p>
 * Features:
 * - Right-click altar to check tier and capacity
 * - Shows current blood level, capacity, and tier
 * - Triggers tier recalculation
 */
public class ItemAltarDiviner extends Item {

    public ItemAltarDiviner() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Check if clicked block is a Blood Altar
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof TileAltar altar)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Trigger tier recalculation
        altar.checkTier();

        // Get altar information
        int currentBlood = altar.getCurrentBlood();
        int capacity = altar.getCapacity();

        // Display information to player
        player.displayClientMessage(
            Component.literal("§6=== Blood Altar Info ==="), false
        );
        player.displayClientMessage(
            Component.literal("§4Blood: §f" + currentBlood + " / " + capacity + " LP"), false
        );

        // Show tier information
        int tierLevel = altar.getTier();
        player.displayClientMessage(
            Component.literal("§cTier: §f" + tierLevel), false
        );

        // Play sound
        level.playSound(
            null,
            pos,
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.BLOCKS,
            0.5F,
            1.0F
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_FIRST));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_SECOND));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_THIRD));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
