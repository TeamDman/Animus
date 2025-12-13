package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;


public class AnimusBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(Constants.Mod.MODID);

    // Basic blocks
    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD = BLOCKS.register("blood_wood",
        BlockBloodWood::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_STRIPPED = BLOCKS.register("blood_wood_stripped",
        BlockBloodWoodStripped::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_PLANKS = BLOCKS.register("blood_wood_planks",
        BlockBloodWoodPlanks::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_STAIRS = BLOCKS.register("blood_wood_stairs",
        BlockBloodWoodStairs::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_SLAB = BLOCKS.register("blood_wood_slab",
        BlockBloodWoodSlab::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_FENCE = BLOCKS.register("blood_wood_fence",
        BlockBloodWoodFence::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_WOOD_FENCE_GATE = BLOCKS.register("blood_wood_fence_gate",
        BlockBloodWoodFenceGate::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_SAPLING = BLOCKS.register("blood_sapling",
        BlockBloodSapling::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_CORE = BLOCKS.register("blood_core",
        BlockBloodCore::new);

    public static final DeferredHolder<Block, Block> BLOCK_BLOOD_LEAVES = BLOCKS.register("blood_leaves",
        BlockBloodLeaves::new);

    public static final DeferredHolder<Block, Block> BLOCK_ANTILIFE = BLOCKS.register("antilife",
        BlockAntiLife::new);

    public static final DeferredHolder<Block, Block> BLOCK_CRYSTALLIZED_DEMON_WILL = BLOCKS.register("crystallized_demon_will_block",
        BlockCrystallizedDemonWill::new);

    public static final DeferredHolder<Block, Block> BLOCK_IMPERFECT_RITUAL_STONE = BLOCKS.register("imperfect_ritual_stone",
        BlockImperfectRitualStone::new);

    // Willful Stone blocks (all 16 colors)
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE = BLOCKS.register("willful_stone",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_WHITE = BLOCKS.register("willful_stone_white",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_ORANGE = BLOCKS.register("willful_stone_orange",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_MAGENTA = BLOCKS.register("willful_stone_magenta",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_LIGHT_BLUE = BLOCKS.register("willful_stone_light_blue",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_YELLOW = BLOCKS.register("willful_stone_yellow",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_LIME = BLOCKS.register("willful_stone_lime",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_PINK = BLOCKS.register("willful_stone_pink",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_LIGHT_GRAY = BLOCKS.register("willful_stone_light_gray",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_CYAN = BLOCKS.register("willful_stone_cyan",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_PURPLE = BLOCKS.register("willful_stone_purple",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_BLUE = BLOCKS.register("willful_stone_blue",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_BROWN = BLOCKS.register("willful_stone_brown",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_GREEN = BLOCKS.register("willful_stone_green",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_RED = BLOCKS.register("willful_stone_red",
        BlockWillfulStone::new);
    public static final DeferredHolder<Block, Block> BLOCK_WILLFUL_STONE_BLACK = BLOCKS.register("willful_stone_black",
        BlockWillfulStone::new);

    // Fluid blocks
    public static final DeferredHolder<Block, Block> BLOCK_FLUID_ANTILIFE = BLOCKS.register("antilife_fluid",
        BlockFluidAntiLife::new);

    public static final DeferredHolder<Block, Block> BLOCK_FLUID_LIVING_TERRA = BLOCKS.register("living_terra_fluid",
        BlockFluidLivingTerra::new);
}
