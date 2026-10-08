package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.AnimusModEventHandler;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.items.sigils.effects.FreeSoulSigilEffect;
import com.breakinblocks.animusnv.items.sigils.effects.TemporalDominanceSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.registry.AnimusRituals;
import com.breakinblocks.animusnv.rituals.RitualPersistence;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.BlockRitualStone;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.ritual.Ritual;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.common.world.chunk.TicketSet;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusLifecycleTests {
    private static final String TEMPLATE = "empty_5x5x7";

    @GameTest(template = TEMPLATE)
    public void acceleration_at_identical_coordinates_is_independent(GameTestHelper h) {
        ServerLevel first = h.getLevel();
        ServerLevel second = first.getServer().getLevel(Level.NETHER);
        BlockPos pos = new BlockPos(h.absolutePos(BlockPos.ZERO).getX(), 80, h.absolutePos(BlockPos.ZERO).getZ());
        var oldFirst = first.getBlockState(pos);
        var oldSecond = second.getBlockState(pos);
        var player = RegressionTestSupport.player(first);
        var network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(100000), 100000);
        var sigil = new ItemStack(AnimusItems.SIGIL_TEMPORAL_DOMINANCE.get());
        player.setShiftKeyDown(true);
        try {
            first.setBlockAndUpdate(pos, Blocks.FURNACE.defaultBlockState());
            second.setBlockAndUpdate(pos, Blocks.FURNACE.defaultBlockState());
            var effect = new TemporalDominanceSigilEffect();
            effect.useOnBlock(first, player, sigil, pos, Direction.UP, Vec3.atCenterOf(pos));
            player.teleportTo(second, pos.getX(), pos.getY(), pos.getZ(), 0, 0);
            effect.useOnBlock(second, player, sigil, pos, Direction.UP, Vec3.atCenterOf(pos));
            h.assertTrue(network.getCurrentEV() == 98000, "both dimensions pay first-level cost");
            var states = TemporalDominanceSigilEffect.getAcceleratedBlocks();
            h.assertTrue(states.get(GlobalPos.of(first.dimension(), pos)).level == 1, "first effect retained");
            h.assertTrue(states.get(GlobalPos.of(second.dimension(), pos)).level == 1, "second effect independent");
            TemporalDominanceSigilEffect.cleanupLevel(first);
            h.assertTrue(states.containsKey(GlobalPos.of(second.dimension(), pos)), "unload only clears one dimension");
        } finally {
            TemporalDominanceSigilEffect.cleanupLevel(first);
            TemporalDominanceSigilEffect.cleanupLevel(second);
            first.setBlockAndUpdate(pos, oldFirst);
            second.setBlockAndUpdate(pos, oldSecond);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void persistence_validates_orphaned_saved_tickets(GameTestHelper h) throws Exception {
        var level = h.getLevel();
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        ChunkPos chunk = new ChunkPos(pos);
        var controller = AnimusModEventHandler.getTicketController();
        controller.forceChunk(level, pos, chunk.x, chunk.z, true, false);
        ForcedChunksSavedData data = level.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
        var ownerConstructor = Class.forName("net.neoforged.neoforge.common.world.chunk.ForcedChunkManager$TicketOwner").getDeclaredConstructors()[0];
        ownerConstructor.setAccessible(true);
        Object owner = ownerConstructor.newInstance(controller.id(), pos);
        h.assertTrue(data.getBlockForcedChunks().getChunks().containsKey(owner), "ticket initially saved");
        var constructor = TicketHelper.class.getDeclaredConstructor(ForcedChunksSavedData.class, ResourceLocation.class, Map.class, Map.class);
        constructor.setAccessible(true);
        var tickets = new TicketSet(new LongOpenHashSet(new long[]{chunk.toLong()}), new LongOpenHashSet());
        var helper = constructor.newInstance(data, controller.id(), Map.of(pos, tickets), Map.of());
        controller.callback().validateTickets(level, helper);
        h.assertTrue(!data.getBlockForcedChunks().getChunks().containsKey(owner), "orphan removed from saved tickets");
        controller.forceChunk(level, pos, chunk.x, chunk.z, false, false);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public void persistence_updates_and_releases_selected_tickets(GameTestHelper h) throws Exception {
        ServerLevel level = h.getLevel();
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2)).above(30);
        ServerPlayer player = RegressionTestSupport.player(level);
        var network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(100000), 100000);
        Ritual ritual = AnimusRituals.PERSISTENCE.get();
        var controller = AnimusModEventHandler.getTicketController();
        ChunkPos chunk = new ChunkPos(pos);
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

            ChunkPos selectedChunk = new ChunkPos(chunk.x + 1, chunk.z - 1);
            BlockPos selectedMin = new BlockPos(selectedChunk.x * 16 - pos.getX(), 0,
                selectedChunk.z * 16 - pos.getZ());
            Ritual current = master.getCurrentRitual();
            current.getBlockRange(RitualPersistence.CHUNK_RANGE).modifyAreaByBlockPositions(
                selectedMin, selectedMin.offset(15, 0, 15));
            current.performRitual(master);
            h.assertTrue(Set.of(selectedChunk).equals(tracked.get(key)), "selection changes update the forced chunks exactly");
            h.assertFalse(controller.forceChunk(level, pos, chunk.x, chunk.z, false, false),
                "chunks removed from the selection release their tickets");

            ForcedChunksSavedData data = level.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
            var constructor = TicketHelper.class.getDeclaredConstructor(ForcedChunksSavedData.class, ResourceLocation.class, Map.class, Map.class);
            constructor.setAccessible(true);
            var tickets = new TicketSet(new LongOpenHashSet(new long[]{selectedChunk.toLong()}), new LongOpenHashSet());
            var helper = constructor.newInstance(data, controller.id(), Map.of(pos, tickets), Map.of());
            RitualPersistence.forgetLevel(level);
            h.assertTrue(!tracked.containsKey(key), "unload forgets in-memory tracking");
            controller.callback().validateTickets(level, helper);
            h.assertTrue(Set.of(selectedChunk).equals(tracked.get(key)), "load restores the active stone's selected chunks");

            master.stopRitual(Ritual.BreakType.DEACTIVATE);
            h.assertTrue(!tracked.containsKey(key), "stop releases tracking");
            h.assertFalse(controller.forceChunk(level, pos, selectedChunk.x, selectedChunk.z, false, false),
                "stop releases the selected chunk's saved ticket");
        } finally {
            RitualPersistence.cleanupAllChunks(level);
            for (BlockPos p : placed) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            }
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void free_soul_returns_to_original_dimension(GameTestHelper h) throws Exception {
        var player = RegressionTestSupport.player(h.getLevel());
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
        var activate = FreeSoulSigilEffect.class.getDeclaredMethod("activateSpectatorMode", ServerPlayer.class, ServerLevel.class, boolean.class);
        activate.setAccessible(true);
        activate.invoke(null, player, h.getLevel(), false);
        player.teleportTo(h.getLevel().getServer().getLevel(Level.NETHER), pos.getX(), 80, pos.getZ(), 0, 0);
        FreeSoulSigilEffect.onPlayerLogout(player);
        h.assertTrue(player.level() == h.getLevel(), "original dimension restored");
        h.assertTrue(player.blockPosition().equals(pos), "original position restored");
        h.succeed();
    }
}
