package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusRituals;
import com.breakinblocks.animusnv.rituals.RitualLuna;
import com.breakinblocks.animusnv.rituals.RitualNaturesLeach;
import com.breakinblocks.animusnv.rituals.RitualSol;
import com.breakinblocks.animusnv.util.ChebyshevSearcher;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusRitualTests {

    private static final String TEMPLATE = "empty_5x5x7";

    @GameTest(template = TEMPLATE)
    public void searcher_covers_its_volume_exactly(GameTestHelper helper) {
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

    @GameTest(template = TEMPLATE)
    public void searcher_restarts_after_sweep(GameTestHelper helper) {
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

    @GameTest(template = TEMPLATE)
    public void sol_and_luna_share_area(GameTestHelper helper) {
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

    @GameTest(template = TEMPLATE)
    public void search_volume_stays_in_range(GameTestHelper helper) {
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

    @GameTest(template = TEMPLATE)
    public void natures_leach_refresh_never_zero(GameTestHelper helper) {
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

    @GameTest(template = TEMPLATE)
    public void every_ritual_has_usable_refresh(GameTestHelper helper) {
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
            }
        }

        return rituals;
    }
}
