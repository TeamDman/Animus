package com.teamdman.animus.blocks;

import com.teamdman.animus.blockentities.BlockEntityBloodCore;
import com.teamdman.animus.registry.AnimusBlockEntities;
import com.teamdman.animus.registry.AnimusSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Blood Core block - has a tile entity for special functionality
 * Shows different texture when active (spreading enabled)
 */
public class BlockBloodCore extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BlockBloodCore() {
        super(BlockBehaviour.Properties.of()
            .strength(10.0F)
            .sound(SoundType.WOOD)
            .randomTicks()
            // Blood core is non-flammable
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBloodCore(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityBloodCore bloodCore) {
                // Toggle spreading
                boolean newSpreading = !bloodCore.isSpreading();
                bloodCore.setSpreading(newSpreading);

                // Update block state to reflect active status
                level.setBlock(pos, state.setValue(ACTIVE, newSpreading), 3);

                // Send feedback to player
                if (newSpreading) {
                    // Play awakening sound when activating
                    level.playSound(null, pos, AnimusSounds.AWAKEN_CORE.get(),
                        SoundSource.BLOCKS, 1.0f, 1.0f);

                    player.displayClientMessage(
                        Component.literal("Blood Core: Spreading enabled")
                            .withStyle(ChatFormatting.DARK_RED),
                        true
                    );
                } else {
                    player.displayClientMessage(
                        Component.literal("Blood Core: Spreading disabled")
                            .withStyle(ChatFormatting.GRAY),
                        true
                    );
                }

                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    // Static server ticker to avoid lambda allocation
    private static final BlockEntityTicker<BlockEntityBloodCore> SERVER_TICKER =
        (level, pos, state, blockEntity) -> blockEntity.tick();

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null :
            createTickerHelper(type, AnimusBlockEntities.BLOOD_CORE.get(), SERVER_TICKER);
    }

    /**
     * Helper method for type-safe ticker creation
     */
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> givenType,
        BlockEntityType<E> expectedType,
        BlockEntityTicker<? super E> ticker
    ) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }
}
