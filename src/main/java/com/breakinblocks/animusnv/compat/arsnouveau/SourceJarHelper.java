package com.breakinblocks.animusnv.compat.arsnouveau;

import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

/** Loaded only by the Ars integration. Use the jar API directly, including its sync hooks. */
public final class SourceJarHelper {
    private SourceJarHelper() {}

    @Nullable
    private static SourceJarTile jar(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SourceJarTile jar ? jar : null;
    }

    public static int drainSourceFromJar(ServerLevel level, BlockPos pos, int maxDrain) {
        SourceJarTile jar = jar(level, pos);
        if (jar == null || maxDrain <= 0) return 0;
        int before = jar.getSource();
        jar.removeSource(Math.min(maxDrain, before));
        return Math.max(0, before - jar.getSource());
    }

    public static int addSourceToJar(ServerLevel level, BlockPos pos, int maxAdd) {
        SourceJarTile jar = jar(level, pos);
        if (jar == null || maxAdd <= 0) return 0;
        int before = jar.getSource();
        int amount = Math.min(maxAdd, Math.max(0, jar.getMaxSource() - before));
        if (amount == 0) return 0;
        jar.addSource(amount);
        return Math.max(0, jar.getSource() - before);
    }

    public static int getFreeSpace(ServerLevel level, BlockPos pos) {
        SourceJarTile jar = jar(level, pos);
        return jar == null ? 0 : Math.max(0, jar.getMaxSource() - jar.getSource());
    }

    public static int getSourceAmount(ServerLevel level, BlockPos pos) {
        SourceJarTile jar = jar(level, pos);
        return jar == null ? 0 : jar.getSource();
    }

    public static boolean isSourceJar(ServerLevel level, BlockPos pos) {
        return jar(level, pos) != null;
    }

    public static int drainSourceFromJarAbove(ServerLevel level, BlockPos center, int amount) {
        return drainSourceFromJar(level, center.above(), amount);
    }

    public static int addSourceToJarAbove(ServerLevel level, BlockPos center, int amount) {
        return addSourceToJar(level, center.above(), amount);
    }

    public static int getFreeSpaceAbove(ServerLevel level, BlockPos center) {
        return getFreeSpace(level, center.above());
    }

    public static int conversionRate(int base, int nearbyRituals) {
        long scaled = (long) Math.max(1, base) << Math.min(31, Math.max(0, nearbyRituals));
        return (int) Math.min(Integer.MAX_VALUE, scaled);
    }
}
