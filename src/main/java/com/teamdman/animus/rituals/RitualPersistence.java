package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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
 * Refresh Cost: Configurable (default: 100 LP), charged once per refresh
 * Refresh Time: Configurable (default: 20 ticks / 1 second), so the defaults work out to 100 LP per second
 * Chunk Radius: Configurable (default: 3 chunks)
 */
@RitualRegister(Constants.Rituals.PERSISTENCE)
public class RitualPersistence extends Ritual {
    // Track loaded chunks per ritual stone position
    private static final Map<GlobalPos, Set<ChunkPos>> loadedChunks = new HashMap<>();

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
        // Check if ritual is enabled
        if (!AnimusConfig.rituals.persistenceEnabled.get()) {
            unloadChunks(serverLevel, masterPos);
            return;
        }


        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            unloadChunks(serverLevel, masterPos);
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
    }

    /**
     * Load chunks in the configured radius around the ritual stone
     */
    private void loadChunks(ServerLevel level, BlockPos masterPos) {
        int radius = AnimusConfig.rituals.persistenceChunkRadius.get();
        ChunkPos centerChunk = new ChunkPos(masterPos);

        // Get or create the set of loaded chunks for this ritual
        Set<ChunkPos> chunks = loadedChunks.computeIfAbsent(GlobalPos.of(level.dimension(), masterPos.immutable()), k -> new HashSet<>());

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
    private static void unloadChunks(ServerLevel level, BlockPos masterPos) {
        Set<ChunkPos> chunks = loadedChunks.get(GlobalPos.of(level.dimension(), masterPos));
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
            loadedChunks.remove(GlobalPos.of(level.dimension(), masterPos));
        }
    }


    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.persistenceLPPerTick.get();
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.persistenceRefreshTime.get();
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

    @Override
    public void stopRitual(IMasterRitualStone mrs, BreakType breakType) {
        if (mrs.getWorldObj() instanceof ServerLevel level) {
            unloadChunks(level, mrs.getMasterBlockPos());
        }
        super.stopRitual(mrs, breakType);
    }

    private static boolean isActiveRitual(ServerLevel level, BlockPos pos) {
        return AnimusConfig.rituals.persistenceEnabled.get()
            && level.getBlockEntity(pos) instanceof IMasterRitualStone mrs
            && mrs.isActive() && mrs.getCurrentRitual() instanceof RitualPersistence
            && !level.hasNeighborSignal(pos);
    }

    /** Restore bookkeeping for valid saved tickets and remove orphaned tickets from older versions. */
    public static void validateTickets(ServerLevel level, ForgeChunkManager.TicketHelper helper) {
        helper.getBlockTickets().forEach((pos, tickets) -> {
            if (!isActiveRitual(level, pos)) {
                helper.removeAllTickets(pos);
                loadedChunks.remove(GlobalPos.of(level.dimension(), pos));
                return;
            }
            Set<ChunkPos> chunks = new HashSet<>();
            tickets.getFirst().forEach((long chunk) -> chunks.add(new ChunkPos(chunk)));
            // Animus uses non-ticking tickets; do not retain an unmanaged second set.
            tickets.getSecond().forEach((long chunk) -> helper.removeTicket(pos, chunk, true));
            loadedChunks.put(GlobalPos.of(level.dimension(), pos.immutable()), chunks);
        });
    }

    /** Also catches explosions, block replacement and redstone pauses. */
    public static void tickLoadedChunks(ServerLevel level) {
        for (GlobalPos pos : new ArrayList<>(loadedChunks.keySet())) {
            if (pos.dimension().equals(level.dimension()) && !isActiveRitual(level, pos.pos())) {
                unloadChunks(level, pos.pos());
            }
        }
    }

    /** Active tickets stay saved so rituals resume after a server restart. */
    public static void forgetLevel(ServerLevel level) {
        loadedChunks.keySet().removeIf(pos -> pos.dimension().equals(level.dimension()));
    }
}
