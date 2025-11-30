package com.teamdman.animus.blocks;

import com.teamdman.animus.blockentities.BlockEntityArcaneRune;
import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.Nullable;

/**
 * Arcane Rune - An Ars Nouveau powered altar component
 *
 * This rune drains Source from nearby Ars Nouveau Source Jars to provide enhanced altar bonuses.
 *
 * When Source is available (1000 Source consumed every 10 seconds):
 * - Acts as a Speed Rune but 15% faster
 * - Acts as a Dislocation Rune
 *
 * When no Source available:
 * - Acts as a Speed Rune but half as fast
 *
 * Note: The actual altar stat modifications are handled by Blood Magic's rune system
 * through component registration. This block entity tracks the Source consumption
 * and can be queried for its current bonus state.
 */
public class BlockArcaneRune extends Block implements EntityBlock {

    public BlockArcaneRune() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(2.0F, 5.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .lightLevel((state) -> 7) // Emit a subtle glow
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityArcaneRune(pos, state);
    }

    // Static server ticker to avoid lambda allocation
    private static final BlockEntityTicker<BlockEntityArcaneRune> SERVER_TICKER =
        (level, pos, state, blockEntity) -> blockEntity.tick();

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null :
            createTickerHelper(type, AnimusBlockEntities.ARCANE_RUNE.get(), SERVER_TICKER);
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
