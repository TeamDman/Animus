package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * Sigil of the Phantom Builder - places blocks from the offhand
 * When activated (toggleable), removes right-click delay for fast building
 * Right-click to place blocks from offhand in front of player
 * Right-click on a block while sneaking to fill area
 */
public class ItemSigilBuilder extends ItemSigilToggleableBase {

    public ItemSigilBuilder() {
        super(Constants.Sigils.BUILDER, 100);
    }

    /**
     * Get the stack to use for building (opposite hand)
     */
    private ItemStack getStackToUse(InteractionHand hand, Player player) {
        return hand == InteractionHand.MAIN_HAND
            ? player.getOffhandItem()
            : player.getMainHandItem();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (getActivated(stack)) {
            // TODO: Remove right-click delay for fast building
            // This requires reflection to access Minecraft's rightClickDelay field
            // For now, this is a placeholder
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide || isUnusable(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.isShiftKeyDown()) {
            // Toggle activation
            setActivatedState(stack, !getActivated(stack));
            return InteractionResultHolder.success(stack);
        } else {
            // Place block in front of player
            ItemStack buildStack = getStackToUse(hand, player);

            if (!buildStack.isEmpty() && buildStack.getItem() instanceof BlockItem blockItem) {
                BlockPos placePos = player.blockPosition()
                    .relative(player.getDirection(), 2)
                    .above();

                if (level.isEmptyBlock(placePos)) {
                    // Get block state
                    Block block = blockItem.getBlock();
                    BlockState state = block.defaultBlockState();

                    // Place the block
                    level.setBlock(placePos, state, 3);

                    // Consume LP
                    SoulNetwork network = NetworkHelper.getSoulNetwork(player);
                    SoulTicket ticket = new SoulTicket(
                        Component.translatable(Constants.Localizations.Text.TICKET_BUILDER),
                        getLpUsed()
                    );
                    network.syphonAndDamage(player, ticket);

                    // Consume item
                    buildStack.shrink(1);
                    if (buildStack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
                        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }

                    return InteractionResultHolder.success(stack);
                }
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        InteractionHand hand = context.getHand();

        if (isUnusable(stack) || level.isClientSide) {
            return InteractionResult.PASS;
        }

        ItemStack buildStack = getStackToUse(hand, player);
        if (buildStack.isEmpty() || !(buildStack.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.PASS;
        }

        Block block = blockItem.getBlock();

        if (player.isShiftKeyDown()) {
            // Fill area
            int radius = (int) Math.sqrt(AnimusConfig.sigils.builderRange.get());

            for (int r = 1; r <= radius; r++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos placePos = switch (face.getAxis()) {
                            case X -> clickedPos.offset(0, x, z);
                            case Y -> clickedPos.offset(x, 0, z);
                            case Z -> clickedPos.offset(x, z, 0);
                        };

                        if (level.isEmptyBlock(placePos)) {
                            if (buildStack.isEmpty()) {
                                return InteractionResult.SUCCESS;
                            }

                            BlockState state = block.defaultBlockState();
                            level.setBlock(placePos, state, 3);

                            buildStack.shrink(1);
                            if (buildStack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
                                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }

            return InteractionResult.SUCCESS;
        } else {
            // Place single block
            BlockPos placePos = clickedPos;
            int distance = 0;

            // Find air block along the face direction
            do {
                placePos = placePos.relative(face.getOpposite());
                distance++;
                if (distance > AnimusConfig.sigils.builderRange.get()) {
                    return InteractionResult.SUCCESS;
                }
            } while (!level.isEmptyBlock(placePos) && placePos.getY() > level.getMinBuildHeight());

            if (level.isEmptyBlock(placePos) && !buildStack.isEmpty()) {
                BlockState state = block.defaultBlockState();
                level.setBlock(placePos, state, 3);

                // Consume LP
                SoulNetwork network = NetworkHelper.getSoulNetwork(player);
                SoulTicket ticket = new SoulTicket(
                    Component.translatable(Constants.Localizations.Text.TICKET_BUILDER),
                    getLpUsed()
                );
                network.syphonAndDamage(player, ticket);

                buildStack.shrink(1);
                if (buildStack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
                    player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_BUILDER_FLAVOUR));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
