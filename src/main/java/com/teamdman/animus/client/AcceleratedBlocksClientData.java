package com.teamdman.animus.client;

import com.teamdman.animus.network.AcceleratedBlocksSyncPayload.AccelerationData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AcceleratedBlocksClientData {
    private static final Map<BlockPos, AccelerationData> acceleratedBlocks = new ConcurrentHashMap<>();

    public static void setAcceleratedBlocks(Map<BlockPos, AccelerationData> blocks) {
        acceleratedBlocks.clear();
        acceleratedBlocks.putAll(blocks);
    }

    public static Map<BlockPos, AccelerationData> getAcceleratedBlocks() {
        return Collections.unmodifiableMap(acceleratedBlocks);
    }

    public static void clear() {
        acceleratedBlocks.clear();
    }

    public static AccelerationData getAccelerationData(BlockPos pos) {
        return acceleratedBlocks.get(pos);
    }

    public static boolean isAccelerated(BlockPos pos, ResourceKey<Level> dimension) {
        AccelerationData data = acceleratedBlocks.get(pos);
        return data != null && data.dimension().equals(dimension);
    }
}
