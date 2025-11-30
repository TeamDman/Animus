package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
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
import wayoftime.bloodmagic.common.block.BlockTeleposer;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * Sigil of Transposition - moves blocks with their tile entities and teleports entities
 *
 * Block Transposition Mode:
 * - Right-click a block to select it, then right-click a location to move it
 * - Right-click air to clear selection
 * - Respects forge:relocation_not_supported tag and protection mods like FTB Chunks
 *
 * Entity Teleportation Mode:
 * - Sneak + Right-click a Teleposer to bind its location
 * - Attack an entity to teleport it to the bound Teleposer (1 block above)
 * - Can only teleport players if they are sneaking
 * - Consumes LP for each teleportation
 */
public class ItemSigilTransposition extends ItemSigilToggleableBase {
    private static final String POS_KEY = "transposition_pos";
    private static final String TELEPOSER_KEY = "teleposer_pos";
    private static final int TELEPORT_COST = 2000; // LP cost to teleport an entity
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

        // Sneak + Right-click on Teleposer to bind teleportation location
        if (player.isShiftKeyDown() && clickedState.getBlock() instanceof BlockTeleposer) {
            tag.putLong(TELEPOSER_KEY, clickedPos.asLong());
            player.displayClientMessage(
                Component.translatable("text.component.animus.transposition.teleposer_bound")
                    .withStyle(ChatFormatting.GOLD),
                true
            );
            level.playSound(null, clickedPos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.5F, 2.0F);
            return InteractionResult.SUCCESS;
        }

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

        if (tag != null && tag.getLong(TELEPOSER_KEY) != 0) {
            BlockPos teleposerPos = BlockPos.of(tag.getLong(TELEPOSER_KEY));
            tooltip.add(Component.translatable("tooltip.animus.transposition.teleposer_location", teleposerPos.getX(), teleposerPos.getY(), teleposerPos.getZ())
                .withStyle(ChatFormatting.AQUA));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide || !(attacker instanceof Player player)) {
            return super.hurtEnemy(stack, target, attacker);
        }

        if (isUnusable(stack)) {
            return super.hurtEnemy(stack, target, attacker);
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || tag.getLong(TELEPOSER_KEY) == 0) {
            player.displayClientMessage(
                Component.translatable("text.component.animus.transposition.no_teleposer")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return super.hurtEnemy(stack, target, attacker);
        }

        // Check if target is a player and not sneaking
        if (target instanceof Player targetPlayer && !targetPlayer.isShiftKeyDown()) {
            player.displayClientMessage(
                Component.translatable("text.component.animus.transposition.player_must_sneak")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return super.hurtEnemy(stack, target, attacker);
        }

        // Get teleposer position
        BlockPos teleposerPos = BlockPos.of(tag.getLong(TELEPOSER_KEY));
        BlockPos targetPos = teleposerPos.above(); // 1 block above teleposer

        // Verify teleposer still exists
        if (!(player.level().getBlockState(teleposerPos).getBlock() instanceof BlockTeleposer)) {
            player.displayClientMessage(
                Component.translatable("text.component.animus.transposition.teleposer_missing")
                    .withStyle(ChatFormatting.RED),
                true
            );
            tag.putLong(TELEPOSER_KEY, 0);
            return super.hurtEnemy(stack, target, attacker);
        }

        // Consume LP
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        SoulTicket ticket = new SoulTicket(
            Component.literal("Entity Teleportation"),
            TELEPORT_COST
        );

        var result = network.syphonAndDamage(player, ticket);
        if (!result.isSuccess()) {
            player.displayClientMessage(
                Component.translatable("text.component.animus.transposition.not_enough_lp", TELEPORT_COST)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return super.hurtEnemy(stack, target, attacker);
        }

        // Teleport the entity
        target.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
        target.fallDistance = 0.0F;

        // Effects
        player.level().playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.level().playSound(null, targetPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        String targetName = target instanceof Player ? target.getName().getString() : target.getType().getDescription().getString();
        player.displayClientMessage(
            Component.translatable("text.component.animus.transposition.teleported", targetName)
                .withStyle(ChatFormatting.GREEN),
            true
        );

        return super.hurtEnemy(stack, target, attacker);
    }
}
