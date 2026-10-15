package com.breakinblocks.animusnv.blocks;

import com.breakinblocks.animusnv.blockentities.BlockEntitySanguineDynamo;
import com.breakinblocks.animusnv.recipes.SanguineDynamoRecipe;
import com.breakinblocks.animusnv.registry.AnimusBlockEntities;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BlockSanguineDynamo extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BlockSanguineDynamo() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_RED)
            .strength(3.0F, 6.0F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> state.getValue(ACTIVE) ? 7 : 0)
            .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(1, 0, 1, 15, 8, 15),
        Block.box(0, 8, 0, 16, 11, 16),
        Block.box(0.5, 11, 0.5, 2.5, 14, 2.5),
        Block.box(13.5, 11, 0.5, 15.5, 14, 2.5),
        Block.box(0.5, 11, 13.5, 2.5, 14, 15.5),
        Block.box(13.5, 11, 13.5, 15.5, 14, 15.5)
    );

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntitySanguineDynamo(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, AnimusBlockEntities.SANGUINE_DYNAMO.get(),
            (lvl, pos, st, be) -> be.tick());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!(level.getBlockEntity(pos) instanceof BlockEntitySanguineDynamo dynamo)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Optional<SanguineDynamoRecipe> recipe = dynamo.findRecipe(stack);
        if (recipe.isEmpty()) {
            if (!(stack.getItem() instanceof BloodOrbItem)) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("text.component.animusnv.sanguine_dynamo.orb_unsupported")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return ItemInteractionResult.FAIL;
        }

        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (!dynamo.isOrbBound(stack)) {
            player.displayClientMessage(
                Component.translatable("text.component.animusnv.sanguine_dynamo.orb_not_bound")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return ItemInteractionResult.FAIL;
        }

        ItemStack existing = dynamo.getOrbStack();
        dynamo.setOrbStack(stack.copyWithCount(1));
        stack.shrink(1);
        if (!existing.isEmpty() && !player.getInventory().add(existing)) {
            player.drop(existing, false);
        }

        SanguineDynamoRecipe r = recipe.get();
        player.displayClientMessage(
            Component.translatable("text.component.animusnv.sanguine_dynamo.orb_placed",
                    String.format("%,d", r.maxEvPerTick()), String.format("%,d", r.maxFePerTick()))
                .withStyle(ChatFormatting.GREEN),
            true
        );
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof BlockEntitySanguineDynamo dynamo)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            showStatus(dynamo, player);
            return InteractionResult.SUCCESS;
        }

        if (!dynamo.getOrbStack().isEmpty()) {
            ItemStack orb = dynamo.getOrbStack();
            dynamo.setOrbStack(ItemStack.EMPTY);
            if (!player.getInventory().add(orb)) {
                player.drop(orb, false);
            }
            player.displayClientMessage(
                Component.translatable("text.component.animusnv.sanguine_dynamo.orb_removed")
                    .withStyle(ChatFormatting.GOLD),
                true
            );
            return InteractionResult.SUCCESS;
        }

        showStatus(dynamo, player);
        return InteractionResult.SUCCESS;
    }

    private void showStatus(BlockEntitySanguineDynamo dynamo, Player player) {
        int stored = dynamo.getEnergyStorage().getEnergyStored();
        int capacity = dynamo.getEnergyStorage().getMaxEnergyStored();

        Optional<SanguineDynamoRecipe> recipe = dynamo.findRecipe(dynamo.getOrbStack());
        if (recipe.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("text.component.animusnv.sanguine_dynamo.status_no_orb",
                        String.format("%,d", stored), String.format("%,d", capacity))
                    .withStyle(ChatFormatting.GRAY),
                true
            );
            return;
        }

        SanguineDynamoRecipe r = recipe.get();
        IAnima network = dynamo.getOwnerNetwork();
        int networkEv = network == null ? 0 : network.getCurrentEV();
        int allowed = dynamo.getAllowedEvPerTick(r);
        int current = dynamo.getLastEvPerTick();

        player.displayClientMessage(
            Component.translatable("text.component.animusnv.sanguine_dynamo.status",
                    String.format("%,d", current),
                    String.format("%,d", current * r.fePerEv()),
                    String.format("%,d", allowed),
                    String.format("%,d", stored),
                    String.format("%,d", capacity),
                    String.format("%,d", networkEv))
                .withStyle(ChatFormatting.DARK_RED),
            true
        );
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockEntitySanguineDynamo dynamo) {
            return dynamo.getComparatorOutput();
        }
        return 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BlockEntitySanguineDynamo dynamo) {
            dynamo.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
