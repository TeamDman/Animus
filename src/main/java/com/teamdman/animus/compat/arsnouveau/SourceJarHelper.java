package com.teamdman.animus.compat.arsnouveau;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Helper class for interacting with Ars Nouveau Source Jars
 * Uses reflection to avoid hard dependencies on Ars Nouveau classes
 */
public class SourceJarHelper {
    // Cache reflected classes and methods
    private static Class<?> sourceJarClass;
    private static java.lang.reflect.Method getSourceMethod;
    private static java.lang.reflect.Method removeSourceMethod;
    private static boolean initialized = false;
    private static boolean initFailed = false;

    static {
        try {
            // Load Ars Nouveau classes via reflection
            sourceJarClass = Class.forName("com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile");

            // Get methods for Source manipulation
            getSourceMethod = sourceJarClass.getMethod("getSource");
            removeSourceMethod = sourceJarClass.getMethod("removeSource", int.class);

            initialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize Ars Nouveau SourceJar integration: " + e.getMessage());
            initFailed = true;
        }
    }

    /**
     * Drain Source from a Source Jar at the specified position
     *
     * @param level The server level
     * @param jarPos The position of the Source Jar
     * @param maxDrain Maximum amount of Source to drain
     * @return Amount of Source actually drained
     */
    public static int drainSourceFromJar(ServerLevel level, BlockPos jarPos, int maxDrain) {
        if (!initialized || initFailed) {
            return 0;
        }

        BlockEntity be = level.getBlockEntity(jarPos);

        if (be != null && sourceJarClass.isInstance(be)) {
            try {
                // Get current Source amount
                int currentSource = (int) getSourceMethod.invoke(be);

                if (currentSource > 0) {
                    // Drain as much as possible from this jar
                    int toDrain = Math.min(maxDrain, currentSource);
                    removeSourceMethod.invoke(be, toDrain);
                    return toDrain;
                }
            } catch (Exception e) {
                // Silent failure - jar cannot be drained
            }
        }

        return 0;
    }

    /**
     * Drain Source from the Source Jar directly above the specified position (y+1)
     *
     * @param level The server level
     * @param center The center position (ritual stone location)
     * @param maxDrain Maximum amount of Source to drain
     * @return Amount of Source actually drained
     */
    public static int drainSourceFromJarAbove(ServerLevel level, BlockPos center, int maxDrain) {
        return drainSourceFromJar(level, center.above(), maxDrain);
    }

    /**
     * Check if a block entity at the specified position is a Source Jar
     *
     * @param level The server level
     * @param pos The position to check
     * @return true if the block entity is a Source Jar
     */
    public static boolean isSourceJar(ServerLevel level, BlockPos pos) {
        if (!initialized || initFailed) {
            return false;
        }

        BlockEntity be = level.getBlockEntity(pos);
        return be != null && sourceJarClass.isInstance(be);
    }

    /**
     * Get the current Source amount in a Source Jar
     *
     * @param level The server level
     * @param jarPos The position of the Source Jar
     * @return Current Source amount, or 0 if not a Source Jar or error
     */
    public static int getSourceAmount(ServerLevel level, BlockPos jarPos) {
        if (!initialized || initFailed) {
            return 0;
        }

        BlockEntity be = level.getBlockEntity(jarPos);

        if (be != null && sourceJarClass.isInstance(be)) {
            try {
                return (int) getSourceMethod.invoke(be);
            } catch (Exception e) {
                // Silent failure
            }
        }

        return 0;
    }
}
