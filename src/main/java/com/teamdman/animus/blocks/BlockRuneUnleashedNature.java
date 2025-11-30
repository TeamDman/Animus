package com.teamdman.animus.blocks;

import com.teamdman.animus.blockentities.BlockEntityRuneUnleashedNature;
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
 * Rune of Unleashed Nature - A hybrid Blood Magic altar rune powered by Botania mana
 *
 * This rune combines the raw power of Blood Magic with the natural energies of Botania.
 * When fueled with mana, it provides exceptional bonuses to the Blood Altar.
 *
 * Features:
 * - Acts as a Capacity Rune (increases LP storage)
 * - Acts as a Rune of the Orb at half effectiveness (increases LP drain efficiency)
 * - When charged with mana, also acts as an Acceleration Rune
 * - Can receive mana via Botania sparks
 *
 * Note: The actual altar stat modifications are handled by Blood Magic's rune system
 * through component registration. This block entity tracks the mana consumption
 * and can be queried for its current bonus state.
 */
public class BlockRuneUnleashedNature extends Block implements EntityBlock {

    public BlockRuneUnleashedNature() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(2.0F, 5.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .lightLevel((state) -> 8) // Emit a subtle green glow
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityRuneUnleashedNature(pos, state);
    }

    // Static server ticker to avoid lambda allocation
    private static final BlockEntityTicker<BlockEntityRuneUnleashedNature> SERVER_TICKER =
        (level, pos, state, blockEntity) -> blockEntity.tick();

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null :
            createTickerHelper(type, AnimusBlockEntities.RUNE_UNLEASHED_NATURE.get(), SERVER_TICKER);
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
