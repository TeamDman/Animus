package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.AnimusStartupConfig;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.breakinblocks.animusnv.registry.AnimusRituals;
import com.breakinblocks.animusnv.rituals.RitualLuna;
import com.breakinblocks.animusnv.rituals.RitualPersistence;
import com.breakinblocks.animusnv.rituals.RitualRelentlessTides;
import com.breakinblocks.animusnv.rituals.RitualSiphon;
import com.breakinblocks.animusnv.rituals.RitualSol;
import com.breakinblocks.animusnv.util.ChebyshevSearcher;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.EnumReaderBoundaries;
import com.breakinblocks.neovitae.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AnimusRitualRangeTests {
    private AnimusRitualRangeTests() {}

    public static void register(AnimusTestRegistrar r) {
        r.add("searcher_respects_rectangular_selections", AnimusRitualRangeTests::searcherRespectsRectangularSelections);
        r.add("searcher_resets_after_selection_changes", AnimusRitualRangeTests::searcherResetsAfterSelectionChanges);
        r.add("block_rituals_search_selected_area", AnimusRitualRangeTests::blockRitualsSearchSelectedArea);
        r.add("persistence_selects_intersecting_chunks", AnimusRitualRangeTests::persistenceSelectsIntersectingChunks);
        r.add("persistence_default_aligns_to_chunks", AnimusRitualRangeTests::persistenceDefaultAlignsToChunks);
        r.add("fluid_rituals_select_one_tank", AnimusRitualRangeTests::fluidRitualsSelectOneTank);
        r.add("steadfast_heart_uses_configured_radius", AnimusRitualRangeTests::steadfastHeartUsesConfiguredRadius);
        r.add("all_ritual_ranges_have_valid_limits", AnimusRitualRangeTests::allRitualRangesHaveValidLimits);
    }

    private static void searcherRespectsRectangularSelections(GameTestHelper h) {
        for (AABB area : List.of(new AABB(2, 1, -3, 5, 4, -1), new AABB(-5, -4, 2, -2, -1, 3),
                new AABB(-2, -2, -1, 3, 3, 2), new AABB(0, 2, 0, 1, 3, 1))) {
            ChebyshevSearcher searcher = new ChebyshevSearcher();
            Set<BlockPos> visited = new HashSet<>();
            for (int tick = 0; tick < 1000; tick++) {
                int[] checks = {0};
                searcher.search(BlockPos.ZERO, area, 1, pos -> {
                    h.assertTrue(area.contains(pos.getX(), pos.getY(), pos.getZ()), "search must stay inside " + area);
                    visited.add(pos.immutable());
                    checks[0]++;
                    return false;
                });
                h.assertTrue(checks[0] <= 1, "search respects its per-tick budget");
            }
            Set<BlockPos> expected = new HashSet<>();
            BlockPos.betweenClosed((int) area.minX, (int) area.minY, (int) area.minZ,
                (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)
                .forEach(pos -> expected.add(pos.immutable()));
            h.assertTrue(visited.equals(expected), "search covers every selected block, including upper layers");
        }
        h.succeed();
    }

    private static void searcherResetsAfterSelectionChanges(GameTestHelper h) {
        ChebyshevSearcher searcher = new ChebyshevSearcher();
        searcher.search(BlockPos.ZERO, new AABB(-4, -4, -4, 5, 5, 5), 71, pos -> false);
        for (BlockPos target : List.of(new BlockPos(3, 4, 2), new BlockPos(-5, -6, -2))) {
            AABB area = new AABB(target.getX(), target.getY(), target.getZ(),
                target.getX() + 1, target.getY() + 1, target.getZ() + 1);
            for (int sweep = 0; sweep < 3; sweep++) {
                h.assertTrue(target.equals(searcher.search(BlockPos.ZERO, area, 8, pos -> true)),
                    "moving or shrinking a selection resets the cursor and repeated sweeps keep finding the block");
            }
        }
        h.succeed();
    }

    private static void blockRitualsSearchSelectedArea(GameTestHelper h) {
        BlockPos master = h.absolutePos(new BlockPos(1, 2, 1));
        BlockPos target = master.offset(2, 1, 1);
        AreaDescriptor area = new AreaDescriptor.Rectangle(new BlockPos(2, 1, 1), 1, 1, 1);
        ServerLevel level = h.getLevel();
        level.setBlockAndUpdate(master.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(target.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
        try {
            BlockPos found = invokeSearch(new RitualSol(), "findDarkSpot",
                new Class<?>[]{Level.class, BlockPos.class, AreaDescriptor.class}, level, master, area);
            h.assertTrue(target.equals(found), "Sol places light inside a selected box above and to one side");

            level.setBlockAndUpdate(master, Blocks.GLOWSTONE.defaultBlockState());
            level.setBlockAndUpdate(target, Blocks.GLOWSTONE.defaultBlockState());
            found = invokeSearch(new RitualLuna(), "findLightEmittingBlock",
                new Class<?>[]{Level.class, BlockPos.class, AreaDescriptor.class}, level, master, area);
            h.assertTrue(target.equals(found), "Luna ignores light outside the selected box");

            level.setBlockAndUpdate(master.below(), Blocks.WATER.defaultBlockState());
            level.setBlockAndUpdate(target, Blocks.WATER.defaultBlockState());
            found = invokeSearch(new RitualSiphon(), "findFluidSource",
                new Class<?>[]{ServerLevel.class, BlockPos.class, AABB.class}, level, master, area.getAABB(master));
            h.assertTrue(target.equals(found), "Siphon searches the selected water above the stone");

            level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
            found = invokeSearch(new RitualRelentlessTides(), "findValidPlacementPosition",
                new Class<?>[]{ServerLevel.class, BlockPos.class, AABB.class, Fluid.class},
                level, master, area.getAABB(master), Fluids.WATER);
            h.assertTrue(target.equals(found), "Relentless Tides places fluid only inside its selected box");
        } catch (ReflectiveOperationException e) {
            h.fail("ritual search threw: " + e);
            return;
        }
        h.succeed();
    }

    private static BlockPos invokeSearch(Object ritual, String name, Class<?>[] parameters, Object... arguments)
            throws ReflectiveOperationException {
        Method method = ritual.getClass().getDeclaredMethod(name, parameters);
        method.setAccessible(true);
        return (BlockPos) method.invoke(ritual, arguments);
    }

    private static void persistenceSelectsIntersectingChunks(GameTestHelper h) {
        RitualPersistence ritual = new RitualPersistence();
        BlockPos master = new BlockPos(-1, 64, -1);
        ritual.getBlockRange(RitualPersistence.CHUNK_RANGE).modifyAreaByBlockPositions(
            new BlockPos(-31, 0, 1), new BlockPos(0, 0, 32));
        Set<ChunkPos> expected = Set.of(new ChunkPos(-2, 0), new ChunkPos(-2, 1),
            new ChunkPos(-1, 0), new ChunkPos(-1, 1));
        h.assertTrue(ritual.getChunksToLoad(master).equals(expected),
            "Persistence respects negative boundaries, rectangular selections and exclusive chunk edges");
        ritual.getBlockRange(RitualPersistence.CHUNK_RANGE).modifyAreaByBlockPositions(BlockPos.ZERO, BlockPos.ZERO);
        h.assertTrue(ritual.getChunksToLoad(master).equals(Set.of(new ChunkPos(-1, -1))),
            "shrinking the selection drops chunks outside it");
        h.succeed();
    }

    private static void persistenceDefaultAlignsToChunks(GameTestHelper h) {
        int radius = AnimusStartupConfig.ritualRanges.persistenceChunkRadius.get();
        Set<ChunkPos> expected = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                expected.add(new ChunkPos(-2 + x, -2 + z));
            }
        }
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                RitualPersistence ritual = new RitualPersistence();
                BlockPos master = new BlockPos(-32 + x, 64, -32 + z);
                h.assertTrue(ritual.getChunksToLoad(master).equals(expected),
                    "the default chunk count is independent of the stone's position within a chunk");
                CompoundTag saved = new CompoundTag();
                ritual.writeToNBT(saved);
                RitualPersistence loaded = new RitualPersistence();
                loaded.readFromNBT(saved);
                h.assertTrue(loaded.getChunksToLoad(master).equals(expected), "aligned chunk ranges survive saving");
            }
        }
        RitualPersistence custom = new RitualPersistence();
        BlockPos master = new BlockPos(-32, 64, -32);
        custom.getChunksToLoad(master);
        int oldRadius = (radius * 2 + 1) * 8;
        custom.getBlockRange(RitualPersistence.CHUNK_RANGE).modifyAreaByBlockPositions(
            new BlockPos(-oldRadius, 0, -oldRadius), new BlockPos(oldRadius - 1, 0, oldRadius - 1));
        int customWidth = radius * 2 + 2;
        h.assertTrue(custom.getChunksToLoad(master).size() == customWidth * customWidth,
            "a new custom box matching the legacy shape is not realigned");
        CompoundTag savedCustom = new CompoundTag();
        custom.writeToNBT(savedCustom);
        RitualPersistence loadedCustom = new RitualPersistence();
        loadedCustom.readFromNBT(savedCustom);
        h.assertTrue(loadedCustom.getChunksToLoad(master).equals(custom.getChunksToLoad(master)),
            "saving preserves an intentional custom box matching the legacy shape");
        h.succeed();
    }

    private static void fluidRitualsSelectOneTank(GameTestHelper h) {
        for (Ritual ritual : List.of(new RitualSiphon(), new RitualRelentlessTides())) {
            AreaDescriptor range = ritual.getBlockRange("tank");
            h.assertTrue(ritual.canBlockRangeBeModified("tank", range, null,
                    new BlockPos(2, 1, 0), new BlockPos(3, 1, 0)) == EnumReaderBoundaries.VOLUME_TOO_LARGE,
                "tank selectors reject multi-block areas");
            range.modifyAreaByBlockPositions(new BlockPos(2, 1, 0), new BlockPos(3, 1, 0));
            CompoundTag saved = new CompoundTag();
            ritual.writeToNBT(saved);
            Ritual loaded = ritual.getNewCopy();
            loaded.readFromNBT(saved);
            h.assertTrue(loaded.getBlockRange("tank").getAABB(BlockPos.ZERO).equals(new AABB(2, 1, 0, 3, 2, 1)),
                "old multi-block tank selections keep the tank they actually used");
        }
        h.succeed();
    }

    private static void steadfastHeartUsesConfiguredRadius(GameTestHelper h) {
        int previous = AnimusConfig.rituals.steadfastHeartRange.get();
        try {
            AnimusConfig.rituals.steadfastHeartRange.set(12);
            Ritual ritual = AnimusRituals.STEADFAST_HEART.get().getNewCopy();
            AABB expected = new AABB(-12, -12, -12, 13, 13, 13);
            h.assertTrue(ritual.getBlockRange("effect").getAABB(BlockPos.ZERO).equals(expected),
                "new Steadfast Heart rituals use the configured radius rather than half a hardcoded width");
            ritual.getBlockRange("effect").modifyAreaByBlockPositions(new BlockPos(-64, -64, -64), new BlockPos(64, 64, 64));
            CompoundTag legacy = new CompoundTag();
            ritual.writeToNBT(legacy);
            legacy.remove("rangeVersion");
            Ritual loaded = ritual.getNewCopy();
            loaded.readFromNBT(legacy);
            h.assertTrue(loaded.getBlockRange("effect").getAABB(BlockPos.ZERO).equals(expected),
                "saved legacy defaults use the corrected radius");

            ritual.getBlockRange("effect").modifyAreaByBlockPositions(new BlockPos(1, 0, 2), new BlockPos(3, 4, 5));
            CompoundTag custom = new CompoundTag();
            ritual.writeToNBT(custom);
            loaded.readFromNBT(custom);
            h.assertTrue(loaded.getBlockRange("effect").getAABB(BlockPos.ZERO)
                .equals(ritual.getBlockRange("effect").getAABB(BlockPos.ZERO)), "custom Steadfast Heart selections survive loading");
        } finally {
            AnimusConfig.rituals.steadfastHeartRange.set(previous);
        }
        h.succeed();
    }

    private static void allRitualRangesHaveValidLimits(GameTestHelper h) {
        for (var holder : AnimusRituals.RITUALS.getEntries()) {
            Ritual ritual = holder.get().getNewCopy();
            for (String key : ritual.getListOfRanges()) {
                AreaDescriptor.Rectangle range = (AreaDescriptor.Rectangle) ritual.getBlockRange(key);
                h.assertTrue(ritual.getMaxVolumeForRange(key) > 0, ritual.getName() + "/" + key + " has a meaningful volume display");
                h.assertTrue(ritual.canBlockRangeBeModified(key, range, null, range.getMinimumOffset(), range.getMaximumOffset())
                    == EnumReaderBoundaries.SUCCESS, ritual.getName() + "/" + key + " accepts its default area");
                if (ritual.getMaxVolumeForRange(key) > 1) {
                    int horizontal = ritual.getMaxHorizontalRadiusForRange(key);
                    int vertical = ritual.getMaxVerticalRadiusForRange(key);
                    h.assertTrue(ritual.canBlockRangeBeModified(key, range, null,
                        new BlockPos(-horizontal, -vertical, -horizontal), new BlockPos(horizontal, vertical, horizontal))
                        == EnumReaderBoundaries.SUCCESS, ritual.getName() + "/" + key + " accepts its full advertised reach");
                }
            }
        }
        h.succeed();
    }
}
