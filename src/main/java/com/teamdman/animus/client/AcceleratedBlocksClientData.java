package com.teamdman.animus.client;

import com.teamdman.animus.network.AcceleratedBlocksSyncPayload.AccelerationData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side storage for accelerated block data received from the server
 * Used by AcceleratedBlockRenderer to display acceleration overlays
 */
public class AcceleratedBlocksClientData {
    private static final Map<BlockPos, AccelerationData> acceleratedBlocks = new ConcurrentHashMap<>();

    /**
     * Set the accelerated blocks (called from network packet handler)
     */
    public static void setAcceleratedBlocks(Map<BlockPos, AccelerationData> blocks) {
        acceleratedBlocks.clear();
        acceleratedBlocks.putAll(blocks);
    }

    /**
     * Get all accelerated blocks for rendering
     */
    public static Map<BlockPos, AccelerationData> getAcceleratedBlocks() {
        return Collections.unmodifiableMap(acceleratedBlocks);
    }

    /**
     * Clear all accelerated blocks (called when leaving a world)
     */
    public static void clear() {
        acceleratedBlocks.clear();
    }

    /**
     * Get acceleration data for a specific position
     */
    public static AccelerationData getAccelerationData(BlockPos pos) {
        return acceleratedBlocks.get(pos);
    }

    /**
     * Check if a block is accelerated in the given dimension
     */
    public static boolean isAccelerated(BlockPos pos, ResourceKey<Level> dimension) {
        AccelerationData data = acceleratedBlocks.get(pos);
        return data != null && data.dimension().equals(dimension);
    }
}
