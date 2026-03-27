package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.neovitae.common.block.TeleposerBlock;

/**
 * Sigil of Transposition - moves blocks with their tile entities and teleports entities.
 * <p>
 * Block Transposition Mode:
 * - Right-click a block to select it, then right-click air to clear selection
 * - useOnBlock handles the actual block movement
 * <p>
 * Entity Teleportation Mode:
 * - Sneak + Right-click a Teleposer to bind its location
 * - Attack an entity to teleport it to the bound Teleposer
 */
public record TranspositionSigilEffect() implements ISigilEffect {
    public static final MapCodec<TranspositionSigilEffect> CODEC = MapCodec.unit(TranspositionSigilEffect::new);

    private static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported")
    );

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return false;
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

        if (result.getType() == HitResult.Type.MISS) {
            // Clear selection
            stack.remove(AnimusDataComponents.TRANSPOSITION_POS.get());
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TRANSPOSITION_CLEARED),
                    true
            );
            return true;
        }

        return false;
    }

    @Override
    public boolean useOnBlock(Level level, Player player, ItemStack stack, BlockPos clickedPos, Direction side, Vec3 hitVec) {
        if (level.isClientSide) {
            return false;
        }

        BlockState clickedState = level.getBlockState(clickedPos);

        // Sneak + Right-click on Teleposer to bind teleportation location
        if (player.isShiftKeyDown() && clickedState.getBlock() instanceof TeleposerBlock) {
            stack.set(AnimusDataComponents.TELEPOSER_POS.get(), clickedPos);
            player.displayClientMessage(
                    Component.translatable("text.component.animusnv.transposition.teleposer_bound")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
            level.playSound(null, clickedPos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.5F, 2.0F);
            return true;
        }

        BlockPos storedPos = stack.get(AnimusDataComponents.TRANSPOSITION_POS.get());

        if (storedPos == null) {
            // First click - select block to move
            if (clickedState.is(RELOCATION_NOT_SUPPORTED)) {
                player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                        true
                );
                return false;
            }

            // Check protection
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, clickedPos, clickedState, player);
            if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) {
                player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                        true
                );
                return false;
            }

            stack.set(AnimusDataComponents.TRANSPOSITION_POS.get(), clickedPos);
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TRANSPOSITION_SET),
                    true
            );
            level.playSound(null, clickedPos, SoundEvents.SHULKER_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);

            return true;
        } else {
            // Second click - move the block
            BlockPos newPos = clickedPos.relative(side);

            BlockState oldState = level.getBlockState(storedPos);

            // Re-check restrictions
            if (oldState.is(RELOCATION_NOT_SUPPORTED)) {
                stack.remove(AnimusDataComponents.TRANSPOSITION_POS.get());
                level.playSound(null, newPos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                        true
                );
                return false;
            }

            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, storedPos, oldState, player);
            if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) {
                stack.remove(AnimusDataComponents.TRANSPOSITION_POS.get());
                level.playSound(null, newPos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE),
                        true
                );
                return false;
            }

            // Check destination is air
            if (!level.isEmptyBlock(newPos)) {
                player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.DIVINER_OBSTRUCTED),
                        true
                );
                return false;
            }

            // Force load chunk
            ChunkPos oldChunkPos = new ChunkPos(storedPos);
            boolean wasForced = false;
            if (level instanceof ServerLevel serverLevel) {
                wasForced = serverLevel.setChunkForced(oldChunkPos.x, oldChunkPos.z, true);
            }

            try {
                BlockEntity oldTile = level.getBlockEntity(storedPos);

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
                        level.removeBlockEntity(storedPos);
                    }
                }

                // Remove old block
                level.removeBlock(storedPos, false);
            } finally {
                if (level instanceof ServerLevel serverLevel && wasForced) {
                    serverLevel.setChunkForced(oldChunkPos.x, oldChunkPos.z, false);
                }
            }

            level.playSound(null, newPos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.remove(AnimusDataComponents.TRANSPOSITION_POS.get());

            return true;
        }
    }

    /**
     * Handle entity teleportation when attacking with the sigil.
     * This is called from an event handler since ISigilEffect doesn't have hurtEnemy.
     */
    public static boolean handleEntityTeleport(Player player, LivingEntity target, ItemStack stack) {
        if (player.level().isClientSide) {
            return false;
        }

        BlockPos teleposerPos = stack.get(AnimusDataComponents.TELEPOSER_POS.get());
        if (teleposerPos == null) {
            player.displayClientMessage(
                    Component.translatable("text.component.animusnv.transposition.no_teleposer")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Check if target is a player and not sneaking
        if (target instanceof Player targetPlayer && !targetPlayer.isShiftKeyDown()) {
            player.displayClientMessage(
                    Component.translatable("text.component.animusnv.transposition.player_must_sneak")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Verify teleposer still exists
        if (!(player.level().getBlockState(teleposerPos).getBlock() instanceof TeleposerBlock)) {
            player.displayClientMessage(
                    Component.translatable("text.component.animusnv.transposition.teleposer_missing")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            stack.remove(AnimusDataComponents.TELEPOSER_POS.get());
            return false;
        }

        // Teleport target
        BlockPos targetTeleportPos = teleposerPos.above();
        target.teleportTo(targetTeleportPos.getX() + 0.5, targetTeleportPos.getY(), targetTeleportPos.getZ() + 0.5);
        target.fallDistance = 0.0F;

        // Effects
        player.level().playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.level().playSound(null, targetTeleportPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        String targetName = target instanceof Player ? target.getName().getString() : target.getType().getDescription().getString();
        player.displayClientMessage(
                Component.translatable("text.component.animusnv.transposition.teleported", targetName)
                        .withStyle(ChatFormatting.GREEN),
                true
        );

        return true;
    }
}
