package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * Sigil of Transposition - moves blocks with their tile entities
 * Right-click a block to select it, then right-click a location to move it
 * Shift-right-click to clear selection
 */
public class ItemSigilTransposition extends ItemSigilToggleableBase {
    private static final String POS_KEY = "transposition_pos";

    public ItemSigilTransposition() {
        super(Constants.Sigils.TRANSPOSITION, 5000);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide || isUnusable(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        // Raycast to check if clicking air
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endVec = eyePos.add(lookVec.scale(5.0));

        BlockHitResult result = level.clip(new ClipContext(
            eyePos,
            endVec,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.ANY,
            player
        ));

        if (result.getType() == HitResult.Type.MISS || result.getType() != HitResult.Type.BLOCK) {
            // Clear selection
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(POS_KEY, 0);
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.TRANSPOSITION_CLEARED),
                true
            );
            setActivatedState(stack, false);
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

        if (isUnusable(stack) || level.isClientSide) {
            return InteractionResult.PASS;
        }

        CompoundTag tag = stack.getOrCreateTag();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (!getActivated(stack)) {
            // First click - select block to move
            if (AnimusConfig.sigils.transpositionMovesUnbreakables < 2
                && clickedState.getDestroySpeed(level, clickedPos) == -1) {
                return InteractionResult.PASS;
            }

            tag.putLong(POS_KEY, clickedPos.asLong());
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.TRANSPOSITION_SET),
                true
            );
            level.playSound(null, clickedPos, SoundEvents.SHULKER_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
            setActivatedState(stack, true);

            return InteractionResult.SUCCESS;
        } else {
            // Second click - move the block
            BlockPos oldPos = BlockPos.of(tag.getLong(POS_KEY));
            BlockPos newPos = clickedPos.relative(face);

            // Check if old block is still movable
            if (AnimusConfig.sigils.transpositionMovesUnbreakables == 0
                && level.getBlockState(oldPos).getDestroySpeed(level, oldPos) == -1) {
                tag.putLong(POS_KEY, 0);
                level.playSound(null, newPos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0F, 1.0F);
                setActivatedState(stack, false);
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                    true
                );
                return InteractionResult.PASS;
            }

            // Check if destination is air
            if (level.isEmptyBlock(newPos)) {
                // Consume LP
                SoulNetwork network = NetworkHelper.getSoulNetwork(player);
                SoulTicket ticket = new SoulTicket(
                    Component.translatable(Constants.Localizations.Text.TICKET_TRANSPOSITION),
                    getLpUsed()
                );

                var result = network.syphonAndDamage(player, ticket);
                if (!result.getValue()) {
                    return InteractionResult.FAIL;
                }

                // Get old tile entity
                BlockEntity oldTile = level.getBlockEntity(oldPos);
                BlockState oldState = level.getBlockState(oldPos);

                // Move the block
                level.setBlock(newPos, oldState, 3);

                // Move tile entity if present
                if (oldTile != null) {
                    BlockEntity newTile = level.getBlockEntity(newPos);
                    if (newTile != null) {
                        CompoundTag tileData = oldTile.saveWithoutMetadata();
                        tileData.putInt("x", newPos.getX());
                        tileData.putInt("y", newPos.getY());
                        tileData.putInt("z", newPos.getZ());
                        newTile.load(tileData);
                        level.removeBlockEntity(oldPos);
                    }
                }

                // Remove old block
                level.removeBlock(oldPos, false);

                // Effects
                level.playSound(null, newPos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

                // Clear selection
                tag.putLong(POS_KEY, 0);
                setActivatedState(stack, false);

                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.DIVINER_OBSTRUCTED),
                    true
                );
                return InteractionResult.FAIL;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_FLAVOUR));

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getLong(POS_KEY) != 0) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_STORED));
        }

        // TODO: Add binding owner tooltip when binding system is implemented
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
