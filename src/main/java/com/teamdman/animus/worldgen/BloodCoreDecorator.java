package com.teamdman.animus.worldgen;

import com.mojang.serialization.Codec;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Tree decorator that replaces the top center log with a blood core
 */
public class BloodCoreDecorator extends TreeDecorator {
    public static final Codec<BloodCoreDecorator> CODEC = Codec.unit(() -> BloodCoreDecorator.INSTANCE);
    public static final BloodCoreDecorator INSTANCE = new BloodCoreDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return AnimusTreeDecoratorTypes.BLOOD_CORE.get();
    }

    @Override
    public void place(Context context) {
        // Find the highest log position in the trunk
        if (context.logs().isEmpty()) {
            return;
        }

        // Get the highest Y position from all logs
        BlockPos highestLog = context.logs().get(0);
        for (BlockPos log : context.logs()) {
            if (log.getY() > highestLog.getY()) {
                highestLog = log;
            }
        }

        // Find the topmost center log (x and z should match the base)
        BlockPos basePos = context.logs().get(0);
        for (BlockPos log : context.logs()) {
            if (log.getX() == basePos.getX() &&
                log.getZ() == basePos.getZ() &&
                log.getY() == highestLog.getY()) {
                // Replace this log with a blood core
                context.setBlock(log, AnimusBlocks.BLOCK_BLOOD_CORE.get().defaultBlockState());
                break;
            }
        }
    }
}
