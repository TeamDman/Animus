package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.DynamicTreesCompatLoader;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusDynamicTreesTests {

    private static final String TEMPLATE = "empty_5x5x7";
    private static final int MAX_PULSES = 400;
    private static final int MAX_TREE_HEIGHT = 32;

    @GameTest(template = TEMPLATE)
    public void dynamic_blood_tree_grows_blood_core(GameTestHelper h) {
        if (!ModList.get().isLoaded("dynamictrees")) {
            h.succeed();
            return;
        }

        ServerLevel level = h.getLevel();
        BlockPos soil = h.absolutePos(new BlockPos(2, 8, 2));
        BlockPos base = soil.above();
        level.setBlockAndUpdate(soil, Blocks.DIRT.defaultBlockState());

        try {
            h.assertTrue(DynamicTreesCompatLoader.plantBloodTree(level, base), "blood tree species planted on dirt");

            BlockPos core = findCore(level, base);
            for (int i = 0; i < MAX_PULSES && core == null; i++) {
                TreeHelper.growPulse(level, soil);
                core = findCore(level, base);
            }

            h.assertTrue(core != null, "blood core forms on the dynamic blood tree within " + MAX_PULSES + " growth pulses");
            h.assertTrue(TreeHelper.isBranch(level.getBlockState(core.below())), "blood core sits directly on the trunk");
            h.assertTrue(core.getY() - soil.getY() > 3, "blood core waits for the trunk to reach its minimum height");
        } finally {
            clearTree(level, soil);
        }

        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void dynamic_blood_tree_loot_tables_load(GameTestHelper h) {
        if (!ModList.get().isLoaded("dynamictrees")) {
            h.succeed();
            return;
        }

        List<String> paths = List.of(
            "blocks/bloodwood_leaves",
            "trees/leaves/bloodwood",
            "trees/branches/bloodwood",
            "trees/branches/stripped_bloodwood",
            "trees/voluntary/bloodwood");

        for (String path : paths) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, path));
            LootTable table = h.getLevel().getServer().reloadableRegistries().getLootTable(key);
            h.assertTrue(table != LootTable.EMPTY, path + " loot table failed to load");
        }

        h.succeed();
    }

    private static BlockPos findCore(ServerLevel level, BlockPos base) {
        for (int y = 0; y <= MAX_TREE_HEIGHT; y++) {
            BlockPos pos = base.above(y);
            if (level.getBlockState(pos).is(AnimusBlocks.BLOCK_BLOOD_CORE.get())) {
                return pos;
            }
        }
        return null;
    }

    private static void clearTree(ServerLevel level, BlockPos soil) {
        for (BlockPos pos : BlockPos.betweenClosed(soil.offset(-6, 1, -6), soil.offset(6, MAX_TREE_HEIGHT + 4, 6))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
        level.setBlock(soil, Blocks.AIR.defaultBlockState(), 2);
    }
}
