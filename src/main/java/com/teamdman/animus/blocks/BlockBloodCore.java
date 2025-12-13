package com.teamdman.animus.blocks;

import com.teamdman.animus.Constants;
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
 * Can be bonemealed to trigger sapling spreading if active
 */
public class BlockBloodCore extends Block implements EntityBlock, BonemealableBlock {
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
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
                        Component.translatable(Constants.Localizations.Text.BLOOD_CORE_SPREADING_ENABLED)
                            .withStyle(ChatFormatting.DARK_RED),
                        true
                    );
                } else {
                    player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.BLOOD_CORE_SPREADING_DISABLED)
                            .withStyle(ChatFormatting.GRAY),
                        true
                    );
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
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

    // BonemealableBlock implementation
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        // Can only bonemeal if active
        return state.getValue(ACTIVE);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        // Always succeed if active
        return state.getValue(ACTIVE);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // Only work if active
        if (!state.getValue(ACTIVE)) {
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityBloodCore bloodCore) {
            // Trigger tree spreading
            bloodCore.trySpreadBloodTree(level);

            // Play enchantment table particles
            for (int i = 0; i < 15; i++) {
                double d0 = pos.getX() + random.nextDouble();
                double d1 = pos.getY() + random.nextDouble() + 0.5;
                double d2 = pos.getZ() + random.nextDouble();
                level.sendParticles(
                    ParticleTypes.ENCHANT,
                    d0, d1, d2,
                    1,
                    0.0, 0.1, 0.0,
                    0.5
                );
            }

            // Play magic sound
            level.playSound(
                null,
                pos,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F + random.nextFloat() * 0.4F
            );
        }
    }
}
