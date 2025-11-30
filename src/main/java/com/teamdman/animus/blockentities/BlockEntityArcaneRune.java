package com.teamdman.animus.blockentities;

import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import wayoftime.bloodmagic.common.tile.TileAltar;

import java.util.ArrayList;
import java.util.List;

/**
 * Block Entity for Arcane Rune
 * Drains Source from nearby Ars Nouveau Source Jars to provide enhanced altar bonuses
 *
 * When Source is available (1000 Source consumed every 10 seconds):
 * - Acts as a Speed Rune but 15% faster
 * - Acts as a Dislocation Rune
 *
 * When no Source available:
 * - Acts as a Speed Rune but half as fast
 */
public class BlockEntityArcaneRune extends BlockEntity {
    // Ars Nouveau integration flag
    private static final boolean ARS_LOADED = ModList.get().isLoaded("ars_nouveau");

    // Ars Nouveau integration helper
    private static ArsIntegration arsIntegration;

    static {
        if (ARS_LOADED) {
            try {
                arsIntegration = new ArsIntegration();
            } catch (Exception e) {
                System.err.println("Failed to initialize Ars Nouveau integration for Arcane Rune: " + e.getMessage());
            }
        }
    }

    // Source drain settings
    private static final int SOURCE_DRAIN_INTERVAL = 200; // 10 seconds (200 ticks)
    private static final int SOURCE_DRAIN_AMOUNT = 1000; // 1000 Source per cycle
    private static final int SOURCE_JAR_RANGE = 6; // 6 block radius

    // Timing
    private int tickCounter = 0;

    // Source state
    private boolean hasSource = false;

    // Cache for source jar positions (refreshed every cycle)
    private List<BlockPos> cachedSourceJars = new ArrayList<>();
    private int cacheRefreshCounter = 0;
    private static final int CACHE_REFRESH_INTERVAL = SOURCE_DRAIN_INTERVAL; // Refresh cache when draining

    public BlockEntityArcaneRune(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.ARCANE_RUNE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Verify the block entity is still valid
        if (level.getBlockEntity(worldPosition) != this) {
            return;
        }

        tickCounter++;
        cacheRefreshCounter++;

        // Refresh cache periodically
        if (cacheRefreshCounter >= CACHE_REFRESH_INTERVAL) {
            cacheRefreshCounter = 0;
            refreshSourceJarCache((ServerLevel) level);
        }

        // Drain source every 10 seconds
        if (tickCounter >= SOURCE_DRAIN_INTERVAL) {
            tickCounter = 0;

            if (ARS_LOADED && arsIntegration != null) {
                int drained = arsIntegration.drainSourceFromJars((ServerLevel) level, cachedSourceJars, SOURCE_DRAIN_AMOUNT);
                hasSource = drained >= SOURCE_DRAIN_AMOUNT;
            } else {
                hasSource = false;
            }

            setChanged();
        }
    }

    /**
     * Refresh the cache of nearby source jar positions
     */
    private void refreshSourceJarCache(ServerLevel level) {
        cachedSourceJars.clear();

        if (!ARS_LOADED || arsIntegration == null) {
            return;
        }

        // Search for source jars in a 6-block radius
        for (BlockPos pos : BlockPos.betweenClosed(
            worldPosition.offset(-SOURCE_JAR_RANGE, -SOURCE_JAR_RANGE, -SOURCE_JAR_RANGE),
            worldPosition.offset(SOURCE_JAR_RANGE, SOURCE_JAR_RANGE, SOURCE_JAR_RANGE)
        )) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null && arsIntegration.isSourceJar(be)) {
                cachedSourceJars.add(pos.immutable());
            }
        }
    }

    /**
     * Get the speed multiplier this rune provides
     * 1.15 when source available (15% faster than speed rune)
     * 0.5 when no source (half speed)
     */
    public float getSpeedMultiplier() {
        return hasSource ? 1.15f : 0.5f;
    }

    /**
     * Check if this rune provides dislocation bonus
     * Only when source is available
     */
    public boolean providesDislocationBonus() {
        return hasSource;
    }

    /**
     * Get whether this rune currently has source
     */
    public boolean hasSource() {
        return hasSource;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        hasSource = tag.getBoolean("hasSource");
        tickCounter = tag.getInt("tickCounter");
        cacheRefreshCounter = tag.getInt("cacheRefreshCounter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("hasSource", hasSource);
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("cacheRefreshCounter", cacheRefreshCounter);
    }

    /**
     * Ars Nouveau integration helper class
     * Uses reflection to avoid hard dependency
     */
    private static class ArsIntegration {
        private Class<?> sourceJarClass;
        private java.lang.reflect.Method getSourceMethod;
        private java.lang.reflect.Method removeSourceMethod;

        public ArsIntegration() throws Exception {
            // Load Ars Nouveau classes via reflection
            sourceJarClass = Class.forName("com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile");
            getSourceMethod = sourceJarClass.getMethod("getSource");
            removeSourceMethod = sourceJarClass.getMethod("removeSource", int.class);
        }

        public boolean isSourceJar(BlockEntity be) {
            return sourceJarClass.isInstance(be);
        }

        /**
         * Drain Source from a list of source jars
         * @return Total amount of Source drained
         */
        public int drainSourceFromJars(ServerLevel level, List<BlockPos> jarPositions, int maxDrain) {
            int totalDrained = 0;
            int remaining = maxDrain;

            for (BlockPos pos : jarPositions) {
                if (remaining <= 0) {
                    break;
                }

                BlockEntity be = level.getBlockEntity(pos);
                if (be != null && sourceJarClass.isInstance(be)) {
                    try {
                        // Get current Source amount
                        int currentSource = (int) getSourceMethod.invoke(be);

                        if (currentSource > 0) {
                            // Drain as much as possible from this jar
                            int toDrain = Math.min(remaining, currentSource);
                            removeSourceMethod.invoke(be, toDrain);

                            totalDrained += toDrain;
                            remaining -= toDrain;
                        }
                    } catch (Exception e) {
                        // Silent failure - jar cannot be drained
                    }
                }
            }

            return totalDrained;
        }
    }
}
