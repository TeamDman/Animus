package com.breakinblocks.animusnv.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class SourceJarHelper {
    private static Class<?> sourceJarClass;
    private static Method getSourceMethod;
    private static Method getMaxSourceMethod;
    private static Method removeSourceMethod;
    private static Method addSourceMethod;
    private static boolean initialized = false;
    private static boolean initFailed = false;

    static {
        try {
            sourceJarClass = Class.forName("com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile");

            getSourceMethod = sourceJarClass.getMethod("getSource");
            getMaxSourceMethod = sourceJarClass.getMethod("getMaxSource");
            removeSourceMethod = sourceJarClass.getMethod("removeSource", int.class);
            addSourceMethod = sourceJarClass.getMethod("addSource", int.class);

            initialized = true;
        } catch (Exception e) {
            initFailed = true;
        }
    }

    public static int conversionRate(int base, int nearbyRituals) {
        long scaled = (long) Math.max(1, base) << Math.min(31, Math.max(0, nearbyRituals));
        return (int) Math.min(Integer.MAX_VALUE, scaled);
    }

    @Nullable
    private static BlockEntity jar(ServerLevel level, BlockPos pos) {
        if (!initialized || initFailed || !level.hasChunkAt(pos)) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && sourceJarClass.isInstance(be) ? be : null;
    }

    private static int source(BlockEntity jar) throws ReflectiveOperationException {
        return (int) getSourceMethod.invoke(jar);
    }

    private static int maxSource(BlockEntity jar) throws ReflectiveOperationException {
        return (int) getMaxSourceMethod.invoke(jar);
    }

    public static int drainSourceFromJar(ServerLevel level, BlockPos jarPos, int maxDrain) {
        BlockEntity jar = jar(level, jarPos);
        if (jar == null || maxDrain <= 0) {
            return 0;
        }
        try {
            int before = source(jar);
            int toDrain = Math.min(maxDrain, before);
            if (toDrain <= 0) {
                return 0;
            }
            removeSourceMethod.invoke(jar, toDrain);
            return Math.max(0, before - source(jar));
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    public static int drainSourceFromJarAbove(ServerLevel level, BlockPos center, int maxDrain) {
        return drainSourceFromJar(level, center.above(), maxDrain);
    }

    public static int addSourceToJar(ServerLevel level, BlockPos jarPos, int maxAdd) {
        BlockEntity jar = jar(level, jarPos);
        if (jar == null || maxAdd <= 0) {
            return 0;
        }
        try {
            int before = source(jar);
            int toAdd = Math.min(maxAdd, Math.max(0, maxSource(jar) - before));
            if (toAdd <= 0) {
                return 0;
            }
            addSourceMethod.invoke(jar, toAdd);
            return Math.max(0, source(jar) - before);
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    public static int addSourceToJarAbove(ServerLevel level, BlockPos center, int maxAdd) {
        return addSourceToJar(level, center.above(), maxAdd);
    }

    public static int getFreeSpace(ServerLevel level, BlockPos jarPos) {
        BlockEntity jar = jar(level, jarPos);
        if (jar == null) {
            return 0;
        }
        try {
            return Math.max(0, maxSource(jar) - source(jar));
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    public static int getFreeSpaceAbove(ServerLevel level, BlockPos center) {
        return getFreeSpace(level, center.above());
    }

    public static boolean isSourceJar(ServerLevel level, BlockPos pos) {
        return jar(level, pos) != null;
    }

    public static int getSourceAmount(ServerLevel level, BlockPos jarPos) {
        BlockEntity jar = jar(level, jarPos);
        if (jar == null) {
            return 0;
        }
        try {
            return source(jar);
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }
}
