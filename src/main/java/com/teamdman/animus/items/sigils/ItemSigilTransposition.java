package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * Sigil of Transposition - moves blocks with their tile entities
 * Right-click a block to select it, then right-click a location to move it
 * Right-click air to clear selection
 * Respects forge:relocation_not_supported tag and protection mods like FTB Chunks
 */
public class ItemSigilTransposition extends ItemSigilToggleableBase {
    private static final String POS_KEY = "transposition_pos";
    private static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("forge", "relocation_not_supported")
    );

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

        // Check binding
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return InteractionResult.FAIL;
        }

        CompoundTag tag = stack.getOrCreateTag();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (!getActivated(stack)) {
            // First click - select block to move
            // Check if block is tagged as non-relocatable
            if (clickedState.is(RELOCATION_NOT_SUPPORTED)) {
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                    true
                );
                return InteractionResult.PASS;
            }

            // Check if player can break the block (protection check)
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, clickedPos, clickedState, player);
            if (MinecraftForge.EVENT_BUS.post(breakEvent)) {
                // Protected by FTB Chunks or similar
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                    true
                );
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
            BlockState oldState = level.getBlockState(oldPos);

            // Check if block is tagged as non-relocatable
            if (oldState.is(RELOCATION_NOT_SUPPORTED)) {
                tag.putLong(POS_KEY, 0);
                level.playSound(null, newPos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0F, 1.0F);
                setActivatedState(stack, false);
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                    true
                );
                return InteractionResult.PASS;
            }

            // Check if player can still break the block (protection check)
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, oldPos, oldState, player);
            if (MinecraftForge.EVENT_BUS.post(breakEvent)) {
                // Protected by FTB Chunks or similar
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
                if (!result.isSuccess()) {
                    return InteractionResult.FAIL;
                }

                // Force load the chunk containing the old position
                ChunkPos oldChunkPos = new ChunkPos(oldPos);
                boolean wasForced = false;
                if (level instanceof ServerLevel serverLevel) {
                    wasForced = serverLevel.setChunkForced(oldChunkPos.x, oldChunkPos.z, true);
                }

                try {
                    // Get old tile entity
                    BlockEntity oldTile = level.getBlockEntity(oldPos);

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
                } finally {
                    // Unload the chunk if we force loaded it
                    if (level instanceof ServerLevel serverLevel && wasForced) {
                        serverLevel.setChunkForced(oldChunkPos.x, oldChunkPos.z, false);
                    }
                }

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
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_INFO));

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getLong(POS_KEY) != 0) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_STORED));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
