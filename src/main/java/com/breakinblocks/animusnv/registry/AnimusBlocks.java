package com.breakinblocks.animusnv.registry;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.blocks.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;


public class AnimusBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.Mod.MODID);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD = BLOCKS.registerBlock("blood_wood",
        BlockBloodWood::new, AnimusBlockProperties::bloodWoodLog);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_STRIPPED = BLOCKS.registerBlock("blood_wood_stripped",
        BlockBloodWoodStripped::new, AnimusBlockProperties::bloodWoodLog);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_PLANKS = BLOCKS.registerBlock("blood_wood_planks",
        BlockBloodWoodPlanks::new, AnimusBlockProperties::bloodWoodPlanks);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_STAIRS = BLOCKS.registerBlock("blood_wood_stairs",
        BlockBloodWoodStairs::new, AnimusBlockProperties::bloodWoodPlanks);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_SLAB = BLOCKS.registerBlock("blood_wood_slab",
        BlockBloodWoodSlab::new, AnimusBlockProperties::bloodWoodPlanks);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_FENCE = BLOCKS.registerBlock("blood_wood_fence",
        BlockBloodWoodFence::new, AnimusBlockProperties::bloodWoodPlanks);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_FENCE_GATE = BLOCKS.registerBlock("blood_wood_fence_gate",
        BlockBloodWoodFenceGate::new, AnimusBlockProperties::bloodWoodPlanks);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_SAPLING = BLOCKS.registerBlock("blood_sapling",
        BlockBloodSapling::new, BlockBloodSapling::defaultProperties);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_CORE = BLOCKS.registerBlock("blood_core",
        BlockBloodCore::new, BlockBloodCore::defaultProperties);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_LEAVES = BLOCKS.registerBlock("blood_leaves",
        BlockBloodLeaves::new, BlockBloodLeaves::defaultProperties);

    public static final DeferredHolder<Block, Block> BLOCK_ANTILIFE = BLOCKS.registerBlock("antilife",
        BlockAntiLife::new, BlockAntiLife::defaultProperties);

    public static final DeferredHolder<Block, Block> BLOCK_CRYSTALLIZED_SPIRITUS = BLOCKS.registerBlock("crystallized_spiritus_block",
        BlockCrystallizedSpiritus::new, BlockCrystallizedSpiritus::defaultProperties);

    // Imperfect Ritual Stone removed - use NeoVitae's native NVBlocks.IMPERFECT_RITUAL_STONE instead

    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE = BLOCKS.registerBlock("willful_stone",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_WHITE = BLOCKS.registerBlock("willful_stone_white",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_ORANGE = BLOCKS.registerBlock("willful_stone_orange",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_MAGENTA = BLOCKS.registerBlock("willful_stone_magenta",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_LIGHT_BLUE = BLOCKS.registerBlock("willful_stone_light_blue",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_YELLOW = BLOCKS.registerBlock("willful_stone_yellow",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_LIME = BLOCKS.registerBlock("willful_stone_lime",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_PINK = BLOCKS.registerBlock("willful_stone_pink",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_LIGHT_GRAY = BLOCKS.registerBlock("willful_stone_light_gray",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_CYAN = BLOCKS.registerBlock("willful_stone_cyan",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_PURPLE = BLOCKS.registerBlock("willful_stone_purple",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_BLUE = BLOCKS.registerBlock("willful_stone_blue",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_BROWN = BLOCKS.registerBlock("willful_stone_brown",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_GREEN = BLOCKS.registerBlock("willful_stone_green",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_RED = BLOCKS.registerBlock("willful_stone_red",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_BLACK = BLOCKS.registerBlock("willful_stone_black",
        BlockWillfulStone::new, BlockWillfulStone::defaultProperties);

    // Sanguine Rectifier moved to EvilCraftCompat (only exists when EvilCraft is loaded)

    public static final DeferredHolder<Block, Block> BLOCK_FLUID_ANTILIFE = BLOCKS.registerBlock("antilife_fluid",
        BlockFluidAntiLife::new, BlockFluidAntiLife::defaultProperties);

    public static final DeferredHolder<Block, Block> BLOCK_FLUID_LIVING_TERRA = BLOCKS.registerBlock("living_terra_fluid",
        BlockFluidLivingTerra::new, BlockFluidLivingTerra::defaultProperties);
}
