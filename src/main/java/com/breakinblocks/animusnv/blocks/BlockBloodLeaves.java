package com.breakinblocks.animusnv.blocks;

import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;

/**
 * Blood Leaves block - decorative leaves that drop blood saplings
*/

public class BlockBloodLeaves extends LeavesBlock {

    public BlockBloodLeaves() {
        super(BlockBehaviour.Properties.of()
            .strength(0.2F)
            .sound(SoundType.GRASS)
            .randomTicks()
            .noOcclusion()
            .isValidSpawn((state, world, pos, type) -> false)
            .isSuffocating((state, world, pos) -> false)
            .isViewBlocking((state, world, pos) -> false)
        );
    }
}
