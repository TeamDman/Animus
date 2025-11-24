package com.teamdman.animus.blocks;

import com.teamdman.animus.blockentities.BlockEntityBloodCore;
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
import org.jetbrains.annotations.Nullable;

/**
 * Blood Core block - has a tile entity for special functionality
 */
public class BlockBloodCore extends Block implements EntityBlock {

    public BlockBloodCore() {
        super(BlockBehaviour.Properties.of()
            .strength(10.0F)
            .sound(SoundType.WOOD)
            .randomTicks()
            .ignitedByLava()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBloodCore(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null :
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof BlockEntityBloodCore bloodCore) {
                    bloodCore.tick();
                }
            };
    }
}
