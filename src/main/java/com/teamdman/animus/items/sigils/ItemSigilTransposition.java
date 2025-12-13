package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import wayoftime.bloodmagic.common.block.TeleposerBlock;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

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
    private static final int TELEPORT_COST = 2000; // LP cost to teleport an entity
    private static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported")
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
            stack.remove(AnimusDataComponents.TRANSPOSITION_POS.get());
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
        if (binding == null || binding.isEmpty() || !binding.uuid().equals(player.getUUID())) {
            return InteractionResult.FAIL;
        }

        BlockState clickedState = level.getBlockState(clickedPos);

        // Sneak + Right-click on Teleposer to bind teleportation location
        if (player.isShiftKeyDown() && clickedState.getBlock() instanceof TeleposerBlock) {
            stack.set(AnimusDataComponents.TELEPOSER_POS.get(), clickedPos);
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
            if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) {
                // Protected by FTB Chunks or similar
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                    true
                );
                return InteractionResult.PASS;
            }

            stack.set(AnimusDataComponents.TRANSPOSITION_POS.get(), clickedPos);
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.TRANSPOSITION_SET),
                true
            );
            level.playSound(null, clickedPos, SoundEvents.SHULKER_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
            setActivatedState(stack, true);

            return InteractionResult.SUCCESS;
        } else {
            // Second click - move the block
            BlockPos oldPos = stack.get(AnimusDataComponents.TRANSPOSITION_POS.get());
            if (oldPos == null) {
                setActivatedState(stack, false);
                return InteractionResult.PASS;
            }
            BlockPos newPos = clickedPos.relative(face);

            // Check if old block is still movable
            BlockState oldState = level.getBlockState(oldPos);

            // Check if block is tagged as non-relocatable
            if (oldState.is(RELOCATION_NOT_SUPPORTED)) {
                stack.remove(AnimusDataComponents.TRANSPOSITION_POS.get());
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
            if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) {
                // Protected by FTB Chunks or similar
                stack.remove(AnimusDataComponents.TRANSPOSITION_POS.get());
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
                SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
                SoulTicket ticket = SoulTicket.create(getLpUsed());

                var syphonResult = network.syphonAndDamage(player, ticket);
                if (!syphonResult.isSuccess()) {
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
                            HolderLookup.Provider registries = level.registryAccess();
                            CompoundTag tileData = oldTile.saveCustomOnly(registries);
                            tileData.putInt("x", newPos.getX());
                            tileData.putInt("y", newPos.getY());
                            tileData.putInt("z", newPos.getZ());
                            newTile.loadCustomOnly(tileData, registries);
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
                stack.remove(AnimusDataComponents.TRANSPOSITION_POS.get());
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_INFO));

        BlockPos storedPos = stack.get(AnimusDataComponents.TRANSPOSITION_POS.get());
        if (storedPos != null) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_STORED));
        }

        BlockPos teleposerPos = stack.get(AnimusDataComponents.TELEPOSER_POS.get());
        if (teleposerPos != null) {
            tooltip.add(Component.translatable("tooltip.animus.transposition.teleposer_location", teleposerPos.getX(), teleposerPos.getY(), teleposerPos.getZ())
                .withStyle(ChatFormatting.AQUA));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide || !(attacker instanceof Player player)) {
            return super.hurtEnemy(stack, target, attacker);
        }

        if (isUnusable(stack)) {
            return super.hurtEnemy(stack, target, attacker);
        }

        BlockPos teleposerPos = stack.get(AnimusDataComponents.TELEPOSER_POS.get());
        if (teleposerPos == null) {
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

        // Get target position (1 block above teleposer)
        BlockPos targetTeleportPos = teleposerPos.above();

        // Verify teleposer still exists
        if (!(player.level().getBlockState(teleposerPos).getBlock() instanceof TeleposerBlock)) {
            player.displayClientMessage(
                Component.translatable("text.component.animus.transposition.teleposer_missing")
                    .withStyle(ChatFormatting.RED),
                true
            );
            stack.remove(AnimusDataComponents.TELEPOSER_POS.get());
            return super.hurtEnemy(stack, target, attacker);
        }

        // Consume LP
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
        SoulTicket ticket = SoulTicket.create(TELEPORT_COST);

        var syphonResult = network.syphonAndDamage(player, ticket);
        if (!syphonResult.isSuccess()) {
            player.displayClientMessage(
                Component.translatable("text.component.animus.transposition.not_enough_lp", TELEPORT_COST)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return super.hurtEnemy(stack, target, attacker);
        }

        // Teleport the entity
        target.teleportTo(targetTeleportPos.getX() + 0.5, targetTeleportPos.getY(), targetTeleportPos.getZ() + 0.5);
        target.fallDistance = 0.0F;

        // Effects
        player.level().playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.level().playSound(null, targetTeleportPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        String targetName = target instanceof Player ? target.getName().getString() : target.getType().getDescription().getString();
        player.displayClientMessage(
            Component.translatable("text.component.animus.transposition.teleported", targetName)
                .withStyle(ChatFormatting.GREEN),
            true
        );

        return super.hurtEnemy(stack, target, attacker);
    }
}
