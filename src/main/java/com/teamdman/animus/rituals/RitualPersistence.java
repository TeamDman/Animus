package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.AnimusStartupConfig;
import com.teamdman.animus.AnimusModEventHandler;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

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
public class RitualPersistence extends Ritual {
    public static final String CHUNK_RANGE = "chunks";

    private static final Map<BlockPos, Set<ChunkPos>> loadedChunks = new HashMap<>();
    private boolean wasActive = false;

    public RitualPersistence() {
        super(
            Constants.Rituals.PERSISTENCE,
            0,
            50000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.PERSISTENCE
        );

        int chunkRadius = AnimusStartupConfig.ritualRanges.persistenceChunkRadius.get();
        int blockRadius = (chunkRadius * 2 + 1) * 8;
        int size = blockRadius * 2;

        addBlockRange(CHUNK_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-blockRadius, 0, -blockRadius), size, 1, size));
        setMaximumVolumeAndDistanceOfRange(CHUNK_RANGE, 0, blockRadius + 64, 1);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        int currentEssence = network.getCurrentEssence();
        int refreshCost = getRefreshCost();

        if (currentEssence < refreshCost) {
            unloadChunks(serverLevel, masterPos);
            return;
        }

        network.syphon(SoulTicket.create(refreshCost));

        loadChunks(serverLevel, masterPos);
        wasActive = true;
    }

    private void loadChunks(ServerLevel level, BlockPos masterPos) {
        AreaDescriptor chunkRange = getBlockRange(CHUNK_RANGE);
        net.minecraft.world.phys.AABB rangeAABB = chunkRange.getAABB(masterPos);
        int blockRadius = (int) Math.max(Math.abs(rangeAABB.maxX - masterPos.getX()), Math.abs(rangeAABB.maxZ - masterPos.getZ()));
        int radius = Math.max(0, (blockRadius / 16));

        ChunkPos centerChunk = new ChunkPos(masterPos);
        TicketController controller = AnimusModEventHandler.getTicketController();

        Set<ChunkPos> chunks = loadedChunks.computeIfAbsent(masterPos, k -> new HashSet<>());

        Set<ChunkPos> chunksToLoad = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                ChunkPos chunkPos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                chunksToLoad.add(chunkPos);
            }
        }

        for (ChunkPos chunkPos : chunksToLoad) {
            if (!chunks.contains(chunkPos)) {
                controller.forceChunk(
                    level,
                    masterPos,
                    chunkPos.x,
                    chunkPos.z,
                    true,
                    false
                );
                chunks.add(chunkPos);
            }
        }

        chunks.removeIf(chunkPos -> {
            if (!chunksToLoad.contains(chunkPos)) {
                controller.forceChunk(
                    level,
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

    private void unloadChunks(ServerLevel level, BlockPos masterPos) {
        Set<ChunkPos> chunks = loadedChunks.get(masterPos);
        TicketController controller = AnimusModEventHandler.getTicketController();
        if (chunks != null) {
            for (ChunkPos chunkPos : chunks) {
                controller.forceChunk(
                    level,
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
        return 20;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 0, 0, -2, EnumRuneType.DUSK);
        addRune(components, 0, 0, 2, EnumRuneType.DUSK);
        addRune(components, -2, 0, 0, EnumRuneType.DUSK);
        addRune(components, 2, 0, 0, EnumRuneType.DUSK);

        addRune(components, -1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.EARTH);
        addRune(components, 1, 0, -1, EnumRuneType.EARTH);
        addRune(components, 1, 0, 1, EnumRuneType.EARTH);

        addRune(components, -3, 0, -3, EnumRuneType.AIR);
        addRune(components, -3, 0, 3, EnumRuneType.AIR);
        addRune(components, 3, 0, -3, EnumRuneType.AIR);
        addRune(components, 3, 0, 3, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualPersistence();
    }

    public static void cleanupAllChunks(ServerLevel level) {
        TicketController controller = AnimusModEventHandler.getTicketController();
        for (Map.Entry<BlockPos, Set<ChunkPos>> entry : loadedChunks.entrySet()) {
            BlockPos masterPos = entry.getKey();
            Set<ChunkPos> chunks = entry.getValue();

            for (ChunkPos chunkPos : chunks) {
                controller.forceChunk(
                    level,
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
