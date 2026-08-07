package com.breakinblocks.animusnv.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

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

    public static int drainSourceFromJar(ServerLevel level, BlockPos jarPos, int maxDrain) {
        if (!initialized || initFailed) {
            return 0;
        }

        BlockEntity be = level.getBlockEntity(jarPos);

        if (be != null && sourceJarClass.isInstance(be)) {
            try {
                int currentSource = (int) getSourceMethod.invoke(be);

                if (currentSource > 0) {
                    int toDrain = Math.min(maxDrain, currentSource);
                    removeSourceMethod.invoke(be, toDrain);
                    return toDrain;
                }
            } catch (Exception e) {
                return 0;
            }
        }

        return 0;
    }

    public static int drainSourceFromJarAbove(ServerLevel level, BlockPos center, int maxDrain) {
        return drainSourceFromJar(level, center.above(), maxDrain);
    }

    public static int addSourceToJar(ServerLevel level, BlockPos jarPos, int maxAdd) {
        if (!initialized || initFailed || maxAdd <= 0) {
            return 0;
        }

        BlockEntity be = level.getBlockEntity(jarPos);

        if (be != null && sourceJarClass.isInstance(be)) {
            try {
                int currentSource = (int) getSourceMethod.invoke(be);
                int maxSource = (int) getMaxSourceMethod.invoke(be);
                int toAdd = Math.min(maxAdd, maxSource - currentSource);

                if (toAdd > 0) {
                    addSourceMethod.invoke(be, toAdd);
                    return toAdd;
                }
            } catch (Exception e) {
                return 0;
            }
        }

        return 0;
    }

    public static int addSourceToJarAbove(ServerLevel level, BlockPos center, int maxAdd) {
        return addSourceToJar(level, center.above(), maxAdd);
    }

    public static boolean isSourceJar(ServerLevel level, BlockPos pos) {
        if (!initialized || initFailed) {
            return false;
        }

        BlockEntity be = level.getBlockEntity(pos);
        return be != null && sourceJarClass.isInstance(be);
    }

    public static int getSourceAmount(ServerLevel level, BlockPos jarPos) {
        if (!initialized || initFailed) {
            return 0;
        }

        BlockEntity be = level.getBlockEntity(jarPos);

        if (be != null && sourceJarClass.isInstance(be)) {
            try {
                return (int) getSourceMethod.invoke(be);
            } catch (Exception e) {
                return 0;
            }
        }

        return 0;
    }
}
