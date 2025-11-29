package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.world.ForgeChunkManager;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Persistence - Keeps chunks loaded
 * Maintains chunk loading in a configurable radius around the ritual stone
 * Activation Cost: 50000 LP
 * Refresh Cost: Configurable (default: 100 LP per tick)
 * Refresh Time: 20 ticks (1 second)
 * Chunk Radius: Configurable (default: 3 chunks)
 */
@RitualRegister(Constants.Rituals.PERSISTENCE)
public class RitualPersistence extends Ritual {
    // Track loaded chunks per ritual stone position
    private static final Map<BlockPos, Set<ChunkPos>> loadedChunks = new HashMap<>();

    // Track if ritual was active last tick (for detecting state changes)
    private boolean wasActive = false;

    public RitualPersistence() {
        super(
            Constants.Rituals.PERSISTENCE,
            0,
            50000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.PERSISTENCE
        );
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        int currentEssence = network.getCurrentEssence();
        int refreshCost = getRefreshCost();

        // Check if we have enough LP
        if (currentEssence < refreshCost) {
            // Not enough LP - unload chunks
            unloadChunks(serverLevel, masterPos);
            network.causeNausea();
            return;
        }

        // Consume LP
        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_PERSISTENCE),
            refreshCost
        ), false);

        // Load chunks
        loadChunks(serverLevel, masterPos);
        wasActive = true;
    }

    /**
     * Load chunks in the configured radius around the ritual stone
     */
    private void loadChunks(ServerLevel level, BlockPos masterPos) {
        int radius = AnimusConfig.rituals.persistenceChunkRadius.get();
        ChunkPos centerChunk = new ChunkPos(masterPos);

        // Get or create the set of loaded chunks for this ritual
        Set<ChunkPos> chunks = loadedChunks.computeIfAbsent(masterPos, k -> new HashSet<>());

        // Calculate which chunks should be loaded
        Set<ChunkPos> chunksToLoad = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                ChunkPos chunkPos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                chunksToLoad.add(chunkPos);
            }
        }

        // Add any new chunks
        for (ChunkPos chunkPos : chunksToLoad) {
            if (!chunks.contains(chunkPos)) {
                ForgeChunkManager.forceChunk(
                    level,
                    Constants.Mod.MODID,
                    masterPos,
                    chunkPos.x,
                    chunkPos.z,
                    true,
                    false
                );
                chunks.add(chunkPos);
            }
        }

        // Remove any chunks that are no longer in range
        chunks.removeIf(chunkPos -> {
            if (!chunksToLoad.contains(chunkPos)) {
                ForgeChunkManager.forceChunk(
                    level,
                    Constants.Mod.MODID,
                    masterPos,
                    chunkPos.x,
                    chunkPos.z,
                    false,
                    false
                );
                return true;
            }
            return false;
        });
    }

    /**
     * Unload all chunks for this ritual
     */
    private void unloadChunks(ServerLevel level, BlockPos masterPos) {
        Set<ChunkPos> chunks = loadedChunks.get(masterPos);
        if (chunks != null) {
            for (ChunkPos chunkPos : chunks) {
                ForgeChunkManager.forceChunk(
                    level,
                    Constants.Mod.MODID,
                    masterPos,
                    chunkPos.x,
                    chunkPos.z,
                    false,
                    false
                );
            }
            chunks.clear();
            loadedChunks.remove(masterPos);
        }
        wasActive = false;
    }


    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.persistenceLPPerTick.get();
    }

    @Override
    public int getRefreshTime() {
        return 20; // 1 second
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a pattern that requires at least 2 dusk runes
        // Using a cross pattern with dusk runes and other runes for balance

        // Center cross with dusk runes (4 dusk runes total)
        addRune(components, 0, 0, -2, EnumRuneType.DUSK);
        addRune(components, 0, 0, 2, EnumRuneType.DUSK);
        addRune(components, -2, 0, 0, EnumRuneType.DUSK);
        addRune(components, 2, 0, 0, EnumRuneType.DUSK);

        // Inner corners with earth runes for stability
        addRune(components, -1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.EARTH);
        addRune(components, 1, 0, -1, EnumRuneType.EARTH);
        addRune(components, 1, 0, 1, EnumRuneType.EARTH);

        // Outer corners with air runes for range
        addRune(components, -3, 0, -3, EnumRuneType.AIR);
        addRune(components, -3, 0, 3, EnumRuneType.AIR);
        addRune(components, 3, 0, -3, EnumRuneType.AIR);
        addRune(components, 3, 0, 3, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualPersistence();
    }

    /**
     * Clean up all chunk loading when the ritual is removed or world unloads
     */
    public static void cleanupAllChunks(ServerLevel level) {
        for (Map.Entry<BlockPos, Set<ChunkPos>> entry : loadedChunks.entrySet()) {
            BlockPos masterPos = entry.getKey();
            Set<ChunkPos> chunks = entry.getValue();

            for (ChunkPos chunkPos : chunks) {
                ForgeChunkManager.forceChunk(
                    level,
                    Constants.Mod.MODID,
                    masterPos,
                    chunkPos.x,
                    chunkPos.z,
                    false,
                    false
                );
            }
        }
        loadedChunks.clear();
    }
}
