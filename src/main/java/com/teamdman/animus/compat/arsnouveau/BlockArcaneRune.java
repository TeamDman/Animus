package com.teamdman.animus.compat.arsnouveau;

import com.teamdman.animus.altar.IMultiBonusRune;
import com.teamdman.animus.compat.ArsNouveauCompat;
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
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.altar.AltarUpgrade;
import wayoftime.bloodmagic.block.enums.BloodRuneType;

/**
 * Arcane Rune - An Ars Nouveau powered altar component
 *
 * This rune drains Source from linked Ars Nouveau Source Jars to provide
 * enhanced altar bonuses.
 *
 * Powered State (Source available, consumes 20 Source every 10 seconds):
 * - Acts as a Speed Rune but 20% faster
 * - Also provides Displacement bonus (increases LP transfer rate)
 *
 * Unpowered State (no Source available):
 * - Acts as a Speed Rune at 67.5% effectiveness
 *
 * This rune implements IMultiBonusRune to provide multiple bonuses when powered,
 * processed via a mixin to Blood Magic's AltarUtil.
 */
public class BlockArcaneRune extends Block implements EntityBlock, IMultiBonusRune {

    public BlockArcaneRune() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0F, 5.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 7)); // Subtle glow
    }

    @Nullable
    @Override
    public BloodRuneType getBloodRune(Level world, BlockPos pos) {
        // This is used for display/tooltip purposes
        // Actual bonuses are applied via applyMultiBonuses()
        return BloodRuneType.SPEED;
    }

    @Override
    public int getRuneCount(Level world, BlockPos pos) {
        // This is used for display/tooltip purposes
        return 1;
    }

    @Override
    public void applyMultiBonuses(Level level, BlockPos pos, AltarUpgrade upgrades) {
        BlockEntity be = level.getBlockEntity(pos);
        boolean hasSource = be instanceof BlockEntityArcaneRune rune && rune.hasSource();

        // Always provides Speed bonus
        // When powered: 20% faster (counts as 1 Speed rune, but the enhanced effect
        // is handled through the rune's internal mechanics)
        // When unpowered: 67.5% effectiveness (still 1 Speed rune)
        upgrades.upgrade(BloodRuneType.SPEED, 1);

        // Powered bonus (only when Source is available):
        // - Displacement: increases LP transfer rate to/from altar
        if (hasSource) {
            upgrades.upgrade(BloodRuneType.DISPLACEMENT, 1);
        }
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
            createTickerHelper(type, ArsNouveauCompat.ARCANE_RUNE_BE.get(), SERVER_TICKER);
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
