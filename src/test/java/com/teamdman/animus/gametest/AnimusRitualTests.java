package com.teamdman.animus.gametest;

import com.teamdman.animus.rituals.RitualAreaScanner;
import com.teamdman.animus.rituals.RitualLuna;
import com.teamdman.animus.rituals.RitualNaturesLeach;
import com.teamdman.animus.rituals.RitualSol;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wayoftime.bloodmagic.ritual.AreaDescriptor;
import wayoftime.bloodmagic.ritual.Ritual;

import java.util.HashSet;
import java.util.Set;

@GameTestHolder("animus")
@PrefixGameTestTemplate(false)
public class AnimusRitualTests {

    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void scannerCoversDeclaredRangeExactly(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 1, 1));
        AreaDescriptor range = new AreaDescriptor.Rectangle(new BlockPos(-2, -2, -2), 5);
        AABB aabb = range.getAABB(master);

        RitualAreaScanner scanner = new RitualAreaScanner();
        Set<BlockPos> visited = new HashSet<>();
        for (int run = 0; run < 4; run++) {
            scanner.scan(helper.getLevel(), master, range, pos -> {
                visited.add(pos.immutable());
                return false;
            });
        }

        Set<BlockPos> expected = new HashSet<>();
        for (int x = (int) aabb.minX; x < (int) aabb.maxX; x++) {
            for (int y = (int) aabb.minY; y < (int) aabb.maxY; y++) {
                for (int z = (int) aabb.minZ; z < (int) aabb.maxZ; z++) {
                    expected.add(new BlockPos(x, y, z));
                }
            }
        }

        if (!visited.equals(expected)) {
            Set<BlockPos> missing = new HashSet<>(expected);
            missing.removeAll(visited);
            Set<BlockPos> extra = new HashSet<>(visited);
            extra.removeAll(expected);
            helper.fail("scanner coverage wrong: " + missing.size() + " missing, " + extra.size()
                + " outside range (first outside: " + (extra.isEmpty() ? "none" : extra.iterator().next()) + ")");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void scannerRestartsAfterCompletingSweep(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 1, 1));
        AreaDescriptor range = new AreaDescriptor.Rectangle(new BlockPos(-1, -1, -1), 3);

        RitualAreaScanner scanner = new RitualAreaScanner();
        int[] hits = new int[1];
        for (int run = 0; run < 3; run++) {
            BlockPos found = scanner.scan(helper.getLevel(), master, range, pos -> pos.equals(master));
            if (found != null) {
                hits[0]++;
            }
        }

        if (hits[0] < 3) {
            helper.fail("scanner stopped finding the target after " + hits[0] + " of 3 sweeps");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void solAndLunaShareTheSameArea(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 1, 1));
        AABB sol = new RitualSol().getBlockRange(RitualSol.EFFECT_RANGE).getAABB(master);
        AABB luna = new RitualLuna().getBlockRange(RitualLuna.EFFECT_RANGE).getAABB(master);

        if (!sol.equals(luna)) {
            helper.fail("Sol and Luna effect ranges differ: " + sol + " vs " + luna);
            return;
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void naturesLeachRefreshTimeNeverZero(GameTestHelper helper) {
        RitualNaturesLeach ritual = new RitualNaturesLeach();

        for (double will : new double[]{0, 1, 20, 100, 1000, 1666, 1667, 100000}) {
            ritual.will = will;
            int refresh = ritual.getRefreshTime();
            if (refresh < 1) {
                helper.fail("refresh time " + refresh + " at will " + will + " would divide by zero");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void everyRitualHasUsableRefreshTime(GameTestHelper helper) {
        for (Ritual ritual : AnimusTestSupport.animusRituals()) {
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
}
