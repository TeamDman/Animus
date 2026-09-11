package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.AnimusStartupConfig;
import com.breakinblocks.animusnv.AnimusModEventHandler;
import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Persistence - Keeps chunks loaded
 * Maintains chunk loading in a configurable radius around the ritual stone
 * Activation Cost: 50000 EV
 * Refresh Cost: Configurable (default: 100 EV per tick)
 * Refresh Time: 20 ticks (1 second)
 * Chunk Radius: Configurable (default: 3 chunks)
 */
public class RitualPersistence extends Ritual {
    public static final String CHUNK_RANGE = "chunks";

    private static final Map<GlobalPos, Set<ChunkPos>> loadedChunks = new HashMap<>();

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

        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
        if (network == null) {
            unloadChunks(serverLevel, masterPos);
            return;
        }

        int currentEV = network.getCurrentEV();
        int refreshCost = getRefreshCost();

        if (currentEV < refreshCost) {
            unloadChunks(serverLevel, masterPos);
            return;
        }

        network.syphon(AnimaTicket.create(refreshCost));

        loadChunks(serverLevel, masterPos);
    }

    private void loadChunks(ServerLevel level, BlockPos masterPos) {
        AreaDescriptor chunkRange = getBlockRange(CHUNK_RANGE);
        AABB rangeAABB = chunkRange.getAABB(masterPos);
        int blockRadius = (int) Math.max(Math.abs(rangeAABB.maxX - masterPos.getX()), Math.abs(rangeAABB.maxZ - masterPos.getZ()));
        int radius = Math.max(0, (blockRadius / 16));

        ChunkPos centerChunk = new ChunkPos(masterPos);
        TicketController controller = AnimusModEventHandler.getTicketController();

        Set<ChunkPos> chunks = loadedChunks.computeIfAbsent(GlobalPos.of(level.dimension(), masterPos.immutable()), k -> new HashSet<>());

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

    private static void unloadChunks(ServerLevel level, BlockPos masterPos) {
        Set<ChunkPos> chunks = loadedChunks.get(GlobalPos.of(level.dimension(), masterPos));
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
            loadedChunks.remove(GlobalPos.of(level.dimension(), masterPos));
        }
    }


    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.persistenceEVPerTick.get();
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.persistenceRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 0, 0, -2, EnumRuneType.TENEBRAE);
        addRune(components, 0, 0, 2, EnumRuneType.TENEBRAE);
        addRune(components, -2, 0, 0, EnumRuneType.TENEBRAE);
        addRune(components, 2, 0, 0, EnumRuneType.TENEBRAE);

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

    @Override
    public void stopRitual(IMasterRitualStone mrs, BreakType breakType) {
        if (mrs.getWorldObj() instanceof ServerLevel level) {
            unloadChunks(level, mrs.getMasterBlockPos());
        }
        super.stopRitual(mrs, breakType);
    }

    private static boolean isActiveRitual(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof IMasterRitualStone mrs)
            || !mrs.isActive() || !(mrs.getCurrentRitual() instanceof RitualPersistence ritual)
            || level.hasNeighborSignal(pos)) return false;
        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
        return network != null && network.getCurrentEV() >= ritual.getRefreshCost();
    }

    public static void validateTickets(ServerLevel level, TicketHelper helper) {
        helper.getBlockTickets().forEach((pos, tickets) -> {
            if (!isActiveRitual(level, pos)) {
                helper.removeAllTickets(pos);
                loadedChunks.remove(GlobalPos.of(level.dimension(), pos));
                return;
            }
            Set<ChunkPos> chunks = new HashSet<>();
            tickets.nonTicking().forEach((long chunk) -> chunks.add(new ChunkPos(chunk)));
            tickets.ticking().forEach((long chunk) -> helper.removeTicket(pos, chunk, true));
            loadedChunks.put(GlobalPos.of(level.dimension(), pos.immutable()), chunks);
        });
        helper.getEntityTickets().keySet().forEach(helper::removeAllTickets);
    }

    /** Catch replacement, explosions, and redstone pauses even if the stone misses a stop callback. */
    public static void tickLoadedChunks(ServerLevel level) {
        for (GlobalPos pos : new ArrayList<>(loadedChunks.keySet())) {
            if (pos.dimension().equals(level.dimension()) && !isActiveRitual(level, pos.pos())) {
                unloadChunks(level, pos.pos());
            }
        }
    }

    public static void cleanupAllChunks(ServerLevel level) {
        for (GlobalPos pos : new ArrayList<>(loadedChunks.keySet())) {
            if (pos.dimension().equals(level.dimension())) unloadChunks(level, pos.pos());
        }
    }

    /** Keep valid tickets on disk for restart; release only in-memory level state. */
    public static void forgetLevel(ServerLevel level) {
        loadedChunks.keySet().removeIf(pos -> pos.dimension().equals(level.dimension()));
    }
}
