package com.breakinblocks.animusnv.compat.evilcraft;

import com.breakinblocks.animusnv.compat.EvilCraftCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockSanguineRectifier extends Block implements EntityBlock {

    public BlockSanguineRectifier() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_RED)
            .strength(3.0F, 6.0F)
            .sound(SoundType.METAL)
            .noOcclusion()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntitySanguineRectifier(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, EvilCraftCompat.SANGUINE_RECTIFIER_BE.get(),
            (lvl, pos, st, be) -> be.tick());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide || !(placer instanceof Player player)) return;

        if (level.getBlockEntity(pos) instanceof BlockEntitySanguineRectifier rectifier) {
            BlockPos altarPos = rectifier.scanForAltar();
            rectifier.setAltarPos(altarPos);

            if (altarPos != null) {
                player.displayClientMessage(
                    Component.translatable("text.component.animusnv.rectifier.linked",
                        altarPos.getX(), altarPos.getY(), altarPos.getZ())
                        .withStyle(ChatFormatting.GREEN),
                    true
                );
            } else {
                player.displayClientMessage(
                    Component.translatable("text.component.animusnv.rectifier.no_altar")
                        .withStyle(ChatFormatting.YELLOW),
                    true
                );
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof BlockEntitySanguineRectifier rectifier)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!stack.isEmpty() && rectifier.isValidOrb(stack)) {
            if (!rectifier.isOrbBound(stack)) {
                player.displayClientMessage(
                    Component.translatable("text.component.animusnv.rectifier.orb_not_bound")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return ItemInteractionResult.FAIL;
            }

            // Swap or place orb
            ItemStack existing = rectifier.getOrbStack();
            rectifier.setOrbStack(stack.copyWithCount(1));
            stack.shrink(1);
            if (!existing.isEmpty()) {
                if (!player.getInventory().add(existing)) {
                    player.drop(existing, false);
                }
            }

            player.displayClientMessage(
                Component.translatable("text.component.animusnv.rectifier.orb_placed")
                    .withStyle(ChatFormatting.GREEN),
                true
            );
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof BlockEntitySanguineRectifier rectifier)) {
            return InteractionResult.PASS;
        }

        // Sneak + empty hand = rescan for altar
        if (player.isShiftKeyDown()) {
            BlockPos altarPos = rectifier.scanForAltar();
            rectifier.setAltarPos(altarPos);

            if (altarPos != null) {
                player.displayClientMessage(
                    Component.translatable("text.component.animusnv.rectifier.linked",
                        altarPos.getX(), altarPos.getY(), altarPos.getZ())
                        .withStyle(ChatFormatting.GREEN),
                    true
                );
            } else {
                player.displayClientMessage(
                    Component.translatable("text.component.animusnv.rectifier.no_altar")
                        .withStyle(ChatFormatting.YELLOW),
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }

        // Empty hand = remove orb
        if (!rectifier.getOrbStack().isEmpty()) {
            ItemStack orb = rectifier.getOrbStack();
            rectifier.setOrbStack(ItemStack.EMPTY);

            if (!player.getInventory().add(orb)) {
                player.drop(orb, false);
            }

            player.displayClientMessage(
                Component.translatable("text.component.animusnv.rectifier.orb_removed")
                    .withStyle(ChatFormatting.GOLD),
                true
            );
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BlockEntitySanguineRectifier rectifier) {
            rectifier.dropContents(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
