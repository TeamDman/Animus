package com.teamdman.animus.worldgen;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class BloodCoreDecorator extends TreeDecorator {
    public static final MapCodec<BloodCoreDecorator> CODEC = MapCodec.unit(BloodCoreDecorator.INSTANCE);
    public static final BloodCoreDecorator INSTANCE = new BloodCoreDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return AnimusTreeDecoratorTypes.BLOOD_CORE.get();
    }

    @Override
    public void place(Context context) {
        if (context.logs().isEmpty()) {
            return;
        }

        BlockPos highestLog = context.logs().get(0);
        for (BlockPos log : context.logs()) {
            if (log.getY() > highestLog.getY()) {
                highestLog = log;
            }
        }

        BlockPos basePos = context.logs().get(0);
        for (BlockPos log : context.logs()) {
            if (log.getX() == basePos.getX() &&
                log.getZ() == basePos.getZ() &&
                log.getY() == highestLog.getY()) {
                context.setBlock(log, AnimusBlocks.BLOCK_BLOOD_CORE.get().defaultBlockState());
                break;
            }
        }
    }
}
