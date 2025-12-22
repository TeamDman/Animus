package com.teamdman.animus.compat.botania;

import com.teamdman.animus.altar.IMultiBonusRune;
import com.teamdman.animus.compat.BotaniaCompat;
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
 * Rune of Unleashed Nature - A hybrid Blood Magic altar rune powered by Botania mana
 *
 * This rune combines the raw power of Blood Magic with the natural energies of Botania.
 * When fueled with mana, it provides exceptional bonuses to the Blood Altar.
 *
 * Passive Bonuses (always active):
 * - Capacity Bonus: Provides 135% of a Capacity Rune's effect (rounded to 1)
 * - Orb Bonus: Provides 67.5% of a Rune of the Orb's effect (rounded to 1)
 *
 * Charged Bonus (when mana is available):
 * - Acceleration Bonus: Speeds up altar crafting and LP generation
 *
 * This rune implements IMultiBonusRune to provide multiple bonuses simultaneously,
 * processed via a mixin to Blood Magic's AltarUtil.
 */
public class BlockRuneUnleashedNature extends Block implements EntityBlock, IMultiBonusRune {

    public BlockRuneUnleashedNature() {
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
        return BloodRuneType.CAPACITY;
    }

    @Override
    public int getRuneCount(Level world, BlockPos pos) {
        // This is used for display/tooltip purposes
        return 1;
    }

    @Override
    public void applyMultiBonuses(Level level, BlockPos pos, AltarUpgrade upgrades) {
        BlockEntity be = level.getBlockEntity(pos);
        boolean isActive = be instanceof BlockEntityRuneUnleashedNature rune && rune.isActive();

        // Passive bonuses (always active):
        // - Capacity: 135% effectiveness → 1 rune (slightly better than standard)
        // - Orb: 67.5% effectiveness → 1 rune (partial orb bonus)
        upgrades.upgrade(BloodRuneType.CAPACITY, 1);
        upgrades.upgrade(BloodRuneType.ORB, 1);

        // Charged bonus (only when mana is available):
        // - Acceleration: speeds up altar operations
        if (isActive) {
            upgrades.upgrade(BloodRuneType.ACCELERATION, 1);
        }
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
            createTickerHelper(type, BotaniaCompat.RUNE_UNLEASHED_NATURE_BE.get(), SERVER_TICKER);
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
