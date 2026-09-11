package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.AnimusModEventHandler;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.breakinblocks.animusnv.items.sigils.effects.FreeSoulSigilEffect;
import com.breakinblocks.animusnv.items.sigils.effects.TemporalDominanceSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.registry.AnimusRituals;
import com.breakinblocks.animusnv.rituals.RitualPersistence;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.common.block.BlockRitualStone;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AnimusLifecycleTests {

    private AnimusLifecycleTests() {
    }

    public static void register(AnimusTestRegistrar r) {
        r.add("acceleration_per_dimension", AnimusLifecycleTests::accelerationAtIdenticalCoordinatesIsIndependent);
        r.add("persistence_drops_orphaned_tickets", AnimusLifecycleTests::persistenceValidatesOrphanedSavedTickets);
        r.add("persistence_stone_restores_and_releases", 200, 0, AnimusLifecycleTests::persistenceStoneRestoresAndReleasesTickets);
        r.add("free_soul_returns_to_dimension", AnimusLifecycleTests::freeSoulReturnsToOriginalDimension);
    }

    private static void accelerationAtIdenticalCoordinatesIsIndependent(GameTestHelper h) {
        ServerLevel first = h.getLevel();
        ServerLevel second = first.getServer().getLevel(Level.NETHER);
        if (second == null) {
            h.fail("nether is not available");
            return;
        }
        BlockPos base = h.absolutePos(BlockPos.ZERO);
        BlockPos pos = new BlockPos(base.getX(), 80, base.getZ());
        BlockState oldFirst = first.getBlockState(pos);
        BlockState oldSecond = second.getBlockState(pos);
        ServerPlayer player = RegressionTestSupport.player(first);
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(100000), 100000);
        ItemStack sigil = new ItemStack(AnimusItems.SIGIL_TEMPORAL_DOMINANCE.get());
        player.setShiftKeyDown(true);
        try {
            first.setBlockAndUpdate(pos, Blocks.FURNACE.defaultBlockState());
            second.setBlockAndUpdate(pos, Blocks.FURNACE.defaultBlockState());
            TemporalDominanceSigilEffect effect = new TemporalDominanceSigilEffect();
            effect.useOnBlock(first, player, sigil, pos, Direction.UP, Vec3.atCenterOf(pos));
            player.teleportTo(second, pos.getX(), pos.getY(), pos.getZ(), Set.of(), 0, 0, false);
            effect.useOnBlock(second, player, sigil, pos, Direction.UP, Vec3.atCenterOf(pos));
            h.assertTrue(network.getCurrentEV() == 98000, "both dimensions pay the first-level cost");
            Map<GlobalPos, TemporalDominanceSigilEffect.AccelerationState> states = TemporalDominanceSigilEffect.getAcceleratedBlocks();
            TemporalDominanceSigilEffect.AccelerationState firstState = states.get(GlobalPos.of(first.dimension(), pos));
            TemporalDominanceSigilEffect.AccelerationState secondState = states.get(GlobalPos.of(second.dimension(), pos));
            h.assertTrue(firstState != null && firstState.level == 1, "first effect retained");
            h.assertTrue(secondState != null && secondState.level == 1, "second effect independent");
            TemporalDominanceSigilEffect.cleanupLevel(first);
            h.assertTrue(!states.containsKey(GlobalPos.of(first.dimension(), pos)), "unload clears the unloaded dimension");
            h.assertTrue(states.containsKey(GlobalPos.of(second.dimension(), pos)), "unload keeps the other dimension");
        } finally {
            TemporalDominanceSigilEffect.cleanupLevel(first);
            TemporalDominanceSigilEffect.cleanupLevel(second);
            first.setBlockAndUpdate(pos, oldFirst);
            second.setBlockAndUpdate(pos, oldSecond);
        }
        h.succeed();
    }

    private static void persistenceValidatesOrphanedSavedTickets(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        ChunkPos chunk = ChunkPos.containing(pos);
        TicketController controller = AnimusModEventHandler.getTicketController();
        h.assertTrue(controller.forceChunk(level, pos, chunk.x(), chunk.z(), true, false), "ticket added");
        TicketStorage storage = level.getDataStorage().computeIfAbsent(TicketStorage.TYPE);
        storage.getBlockForcedChunks().deactivateTicketsOnClosing();
        storage.getEntityForcedChunks().deactivateTicketsOnClosing();
        ForcedChunkManager.activateAllDeactivatedTickets(level, storage);
        boolean stillForced = controller.forceChunk(level, pos, chunk.x(), chunk.z(), false, false);
        h.assertTrue(!stillForced, "ticket without an active ritual stone is dropped on load");
        h.succeed();
    }

    private static void persistenceStoneRestoresAndReleasesTickets(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2)).above(30);
        ServerPlayer player = RegressionTestSupport.player(level);
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(100000), 100000);
        Ritual ritual = AnimusRituals.PERSISTENCE.get();
        TicketController controller = AnimusModEventHandler.getTicketController();
        ChunkPos chunk = ChunkPos.containing(pos);
        List<BlockPos> placed = new ArrayList<>();
        try {
            level.setBlockAndUpdate(pos, NVBlocks.MASTER_RITUAL_STONE.block().get().defaultBlockState());
            placed.add(pos);
            ritual.gatherComponents(component -> {
                Block rune = BuiltInRegistries.BLOCK.stream()
                    .filter(b -> b instanceof BlockRitualStone stone && stone.getRuneType() == component.runeType())
                    .findFirst().orElseThrow();
                BlockPos runePos = pos.offset(component.offset());
                level.setBlockAndUpdate(runePos, rune.defaultBlockState());
                placed.add(runePos);
            });
            if (!(level.getBlockEntity(pos) instanceof MasterRitualStoneBlockEntity master)) {
                h.fail("master ritual stone block entity missing");
                return;
            }
            h.assertTrue(master.activateRitual(ritual, player, 4), "ritual activates");
            master.performRitual();

            Field field = RitualPersistence.class.getDeclaredField("loadedChunks");
            field.setAccessible(true);
            Map<?, ?> tracked = (Map<?, ?>) field.get(null);
            GlobalPos key = GlobalPos.of(level.dimension(), pos);
            h.assertTrue(tracked.containsKey(key), "running stone tracks its chunks");

            TicketStorage storage = level.getDataStorage().computeIfAbsent(TicketStorage.TYPE);
            storage.getBlockForcedChunks().deactivateTicketsOnClosing();
            storage.getEntityForcedChunks().deactivateTicketsOnClosing();
            RitualPersistence.forgetLevel(level);
            h.assertTrue(!tracked.containsKey(key), "unload forgets in-memory tracking");
            ForcedChunkManager.activateAllDeactivatedTickets(level, storage);
            h.assertTrue(tracked.containsKey(key), "load restores tracking for the active stone");

            master.stopRitual(Ritual.BreakType.DEACTIVATE);
            h.assertTrue(!tracked.containsKey(key), "stop releases tracking");
            h.assertTrue(!controller.forceChunk(level, pos, chunk.x(), chunk.z(), false, false), "stop releases the saved ticket");
        } catch (ReflectiveOperationException e) {
            h.fail("reflection failed: " + e);
            return;
        } finally {
            RitualPersistence.cleanupAllChunks(level);
            for (BlockPos p : placed) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            }
        }
        h.succeed();
    }

    private static void freeSoulReturnsToOriginalDimension(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        ServerLevel nether = h.getLevel().getServer().getLevel(Level.NETHER);
        if (nether == null) {
            h.fail("nether is not available");
            return;
        }
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        player.snapTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
        try {
            Method activate = FreeSoulSigilEffect.class.getDeclaredMethod("activateSpectatorMode", ServerPlayer.class, ServerLevel.class, boolean.class);
            activate.setAccessible(true);
            activate.invoke(null, player, h.getLevel(), false);
        } catch (ReflectiveOperationException e) {
            h.fail("reflection failed: " + e);
            return;
        }
        player.teleportTo(nether, pos.getX(), pos.getY(), pos.getZ(), Set.of(), 0, 0, false);
        h.assertTrue(player.level() == nether, "player moved to the nether");
        FreeSoulSigilEffect.onPlayerLogout(player);
        h.assertTrue(player.level() == h.getLevel(), "original dimension restored");
        h.assertTrue(player.blockPosition().equals(pos), "original position restored");
        h.succeed();
    }
}
