package com.teamdman.animus.blocks;

import com.teamdman.animus.blockentities.BlockEntityDiabolicalFungi;
import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;

/**
 * Diabolical Fungi (Devil's Tooth Mushroom) Block
 *
 * A dark, otherworldly mushroom that feeds on demon will in the air,
 * converting it into botanical mana.
 */
public class BlockDiabolicalFungi extends FlowerBlock implements EntityBlock {

    public BlockDiabolicalFungi() {
        super(MobEffects.WITHER, 8, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
            .noCollission()
            .strength(0.0F)
            .sound(net.minecraft.world.level.block.SoundType.GRASS)
            .lightLevel(state -> 7) // Slight dark glow from consuming will
            .noOcclusion());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDiabolicalFungi(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        net.minecraft.world.level.Level level,
        BlockState state,
        BlockEntityType<T> type
    ) {
        if (level.isClientSide) {
            return null;
        }

        return type == AnimusBlockEntities.DIABOLICAL_FUNGI.get()
            ? (lvl, pos, st, be) -> ((GeneratingFlowerBlockEntity) be).tickFlower()
            : null;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction direction) {
        return 0; // Not flammable - it's a demon will-consuming fungi
    }
}
