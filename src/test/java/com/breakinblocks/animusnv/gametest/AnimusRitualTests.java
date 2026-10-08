package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusRituals;
import com.breakinblocks.animusnv.rituals.RitualLuna;
import com.breakinblocks.animusnv.rituals.RitualNaturesLeach;
import com.breakinblocks.animusnv.rituals.RitualSol;
import com.breakinblocks.animusnv.util.ChebyshevSearcher;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.EnumReaderBoundaries;
import com.breakinblocks.neovitae.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AnimusRitualTests {

    private AnimusRitualTests() {
    }

    public static void register(AnimusTestRegistrar r) {
        r.add("searcher_covers_its_volume_exactly", AnimusRitualTests::searcherCoversItsVolumeExactly);
        r.add("searcher_restarts_after_sweep", AnimusRitualTests::searcherRestartsAfterCompletingSweep);
        r.add("sol_and_luna_share_area", AnimusRitualTests::solAndLunaShareTheSameArea);
        r.add("search_volume_stays_in_range", AnimusRitualTests::searchVolumeStaysInsideRange);
        r.add("natures_leach_refresh_never_zero", AnimusRitualTests::naturesLeachRefreshTimeNeverZero);
        r.add("natures_leach_ranges_are_editable", AnimusRitualTests::naturesLeachRangesAreEditable);
        r.add("natures_leach_loads_saved_ranges", AnimusRitualTests::naturesLeachLoadsSavedRanges);
        r.add("natures_leach_preserves_terrain", AnimusRitualTests::naturesLeachPreservesTerrain);
        r.add("every_ritual_has_usable_refresh", AnimusRitualTests::everyRitualHasUsableRefreshTime);
    }

    private static void searcherCoversItsVolumeExactly(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 1, 1));
        int radius = 2;
        int depth = 3;

        ChebyshevSearcher searcher = new ChebyshevSearcher();
        Set<BlockPos> visited = new HashSet<>();
        for (int run = 0; run < 4; run++) {
            searcher.search(master, master, radius, depth, true, 4096, pos -> {
                visited.add(pos.immutable());
                return false;
            });
        }

        Set<BlockPos> expected = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y < depth; y++) {
                    expected.add(master.offset(x, -y, z));
                }
            }
        }

        if (!visited.equals(expected)) {
            Set<BlockPos> missing = new HashSet<>(expected);
            missing.removeAll(visited);
            Set<BlockPos> extra = new HashSet<>(visited);
            extra.removeAll(expected);
            helper.fail("searcher coverage wrong: " + missing.size() + " missing, " + extra.size() + " outside volume");
            return;
        }

        helper.succeed();
    }

    private static void searcherRestartsAfterCompletingSweep(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 1, 1));

        ChebyshevSearcher searcher = new ChebyshevSearcher();
        int hits = 0;
        for (int run = 0; run < 3; run++) {
            if (searcher.search(master, master, 1, 2, true, 4096, pos -> pos.equals(master)) != null) {
                hits++;
            }
        }

        if (hits < 3) {
            helper.fail("searcher stopped finding the target after " + hits + " of 3 sweeps");
            return;
        }

        helper.succeed();
    }

    private static void solAndLunaShareTheSameArea(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 1, 1));
        AABB sol = new RitualSol().getBlockRange(RitualSol.EFFECT_RANGE).getAABB(master);
        AABB luna = new RitualLuna().getBlockRange(RitualLuna.EFFECT_RANGE).getAABB(master);

        if (!sol.equals(luna)) {
            helper.fail("Sol and Luna effect ranges differ: " + sol + " vs " + luna);
            return;
        }

        if (ChebyshevSearcher.horizontalRadiusOf(sol, master) != ChebyshevSearcher.horizontalRadiusOf(luna, master)
            || ChebyshevSearcher.downwardDepthOf(sol, master) != ChebyshevSearcher.downwardDepthOf(luna, master)) {
            helper.fail("Sol and Luna derive different search volumes from the same range");
            return;
        }

        helper.succeed();
    }

    private static void searchVolumeStaysInsideRange(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 1, 1));
        AreaDescriptor range = new AreaDescriptor.Rectangle(new BlockPos(-2, -2, -2), 5, 5, 5);
        AABB aabb = range.getAABB(master);

        int radius = ChebyshevSearcher.horizontalRadiusOf(aabb, master);
        if (radius != 2) {
            helper.fail("expected a radius of 2 for a 5 wide range, got " + radius);
            return;
        }

        int depth = ChebyshevSearcher.downwardDepthOf(aabb, master);
        if (depth != 3) {
            helper.fail("expected a downward depth of 3 for a 5 tall range, got " + depth);
            return;
        }

        helper.succeed();
    }

    private static void naturesLeachRefreshTimeNeverZero(GameTestHelper helper) {
        RitualNaturesLeach ritual = new RitualNaturesLeach();

        for (double spiritus : new double[]{0, 1, 20, 100, 1000, 1666, 1667, 100000}) {
            ritual.will = spiritus;
            int refresh = ritual.getRefreshTime();
            if (refresh < 1) {
                helper.fail("refresh time " + refresh + " at " + spiritus + " spiritus would divide by zero");
                return;
            }
        }

        helper.succeed();
    }

    private static void naturesLeachRangesAreEditable(GameTestHelper helper) {
        RitualNaturesLeach ritual = new RitualNaturesLeach();
        AABB effect = ritual.getBlockRange(RitualNaturesLeach.EFFECT_RANGE).getAABB(BlockPos.ZERO);
        helper.assertTrue(effect.equals(new AABB(-32, -32, -32, 33, 33, 33)),
            "default effect reaches exactly 32 blocks in each direction");

        for (String key : ritual.getListOfRanges()) {
            AreaDescriptor.Rectangle range = (AreaDescriptor.Rectangle) ritual.getBlockRange(key);
            helper.assertTrue(ritual.canBlockRangeBeModified(key, range, null,
                    range.getMinimumOffset(), range.getMaximumOffset()) == EnumReaderBoundaries.SUCCESS,
                key + " default must be accepted by the Ritual Tinkerer");

            int horizontal = ritual.getMaxHorizontalRadiusForRange(key);
            int vertical = ritual.getMaxVerticalRadiusForRange(key);
            helper.assertTrue(ritual.canBlockRangeBeModified(key, range, null,
                    new BlockPos(-horizontal, -vertical, -horizontal),
                    new BlockPos(horizontal, vertical, horizontal)) == EnumReaderBoundaries.SUCCESS,
                key + " full advertised reach must fit the volume limit");
            for (BlockPos outside : List.of(new BlockPos(horizontal + 1, 0, 0),
                    new BlockPos(0, -vertical - 1, 0), new BlockPos(0, 0, -horizontal - 1))) {
                helper.assertTrue(ritual.canBlockRangeBeModified(key, range, null, outside, outside)
                        == EnumReaderBoundaries.NOT_WITHIN_BOUNDARIES,
                    key + " must still reject positions outside its reach");
            }
        }
        helper.succeed();
    }

    private static void naturesLeachLoadsSavedRanges(GameTestHelper helper) {
        RitualNaturesLeach original = new RitualNaturesLeach();
        original.getBlockRange(RitualNaturesLeach.EFFECT_RANGE).modifyAreaByBlockPositions(
            new BlockPos(-32, -32, -32), new BlockPos(35, 35, 35));
        CompoundTag legacy = new CompoundTag();
        original.writeToNBT(legacy);
        legacy.remove("rangeVersion");
        RitualNaturesLeach loaded = new RitualNaturesLeach();
        loaded.readFromNBT(legacy);
        helper.assertTrue(loaded.getBlockRange(RitualNaturesLeach.EFFECT_RANGE).getAABB(BlockPos.ZERO)
                .equals(new AABB(-32, -32, -32, 33, 33, 33)),
            "saved legacy default is corrected when loaded");

        original.getBlockRange(RitualNaturesLeach.EFFECT_RANGE).modifyAreaByBlockPositions(
            new BlockPos(-4, 1, -3), new BlockPos(7, 5, 9));
        original.getBlockRange(RitualNaturesLeach.ALTAR_RANGE).modifyAreaByBlockPositions(
            new BlockPos(0, 3, 0), new BlockPos(0, 3, 0));
        CompoundTag customized = new CompoundTag();
        original.writeToNBT(customized);
        loaded.readFromNBT(customized);
        for (String key : original.getListOfRanges()) {
            helper.assertTrue(loaded.getBlockRange(key).getAABB(BlockPos.ZERO)
                    .equals(original.getBlockRange(key).getAABB(BlockPos.ZERO)),
                "saved custom " + key + " range is preserved");
        }
        helper.succeed();
    }

    private static void naturesLeachPreservesTerrain(GameTestHelper helper) {
        for (Block block : List.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.MYCELIUM,
                Blocks.ROOTED_DIRT, Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM,
                Blocks.STONE, Blocks.AIR, AnimusBlocks.BLOCK_BLOOD_CORE.get())) {
            helper.assertFalse(RitualNaturesLeach.isConsumable(block),
                "Nature's Leach must preserve " + block.getDescriptionId());
        }
        for (Block block : List.of(Blocks.OAK_LOG, Blocks.OAK_LEAVES, Blocks.OAK_SAPLING,
                Blocks.WHEAT, Blocks.DANDELION, Blocks.BROWN_MUSHROOM, Blocks.CRIMSON_FUNGUS,
                Blocks.AZALEA, Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.KELP, Blocks.CAVE_VINES, Blocks.BIG_DRIPLEAF,
                Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, AnimusBlocks.BLOCK_BLOOD_SAPLING.get())) {
            helper.assertTrue(RitualNaturesLeach.isConsumable(block),
                "Nature's Leach must still consume " + block.getDescriptionId());
        }
        helper.succeed();
    }

    private static void everyRitualHasUsableRefreshTime(GameTestHelper helper) {
        List<Ritual> rituals = animusRituals();
        if (rituals.size() < 10) {
            helper.fail("only found " + rituals.size() + " Animus rituals; the registry lookup has stopped covering them");
            return;
        }

        for (Ritual ritual : rituals) {
            int refresh = ritual.getRefreshTime();
            if (refresh < 1) {
                helper.fail(ritual.getName() + " has refresh time " + refresh + ", which crashes the master ritual stone");
                return;
            }
            if (ritual.getRefreshCost() < 0) {
                helper.fail(ritual.getName() + " has a negative refresh cost");
                return;
            }
        }

        helper.succeed();
    }

    /**
     * Every ritual Animus registers, read straight off the registry holders so newly added
     * rituals are covered without touching the tests. Holders that are null (registrations
     * gated behind an optional mod) or unregistered here are skipped.
     */
    private static List<Ritual> animusRituals() {
        List<Ritual> rituals = new ArrayList<>();

        for (Field field : AnimusRituals.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !DeferredHolder.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                DeferredHolder<?, ?> holder = (DeferredHolder<?, ?>) field.get(null);
                if (holder == null) {
                    continue;
                }
                if (holder.get() instanceof Ritual ritual) {
                    rituals.add(ritual);
                }
            } catch (IllegalAccessException | IllegalStateException | NullPointerException ignored) {
                // not registered in this environment
            }
        }

        return rituals;
    }
}
