package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.Mod.MODID);

    // Basic blocks
    public static final RegistryObject<Block> BLOCK_BLOOD_WOOD = BLOCKS.register("blood_wood",
        BlockBloodWood::new);

    public static final RegistryObject<Block> BLOCK_BLOOD_WOOD_STRIPPED = BLOCKS.register("blood_wood_stripped",
        BlockBloodWoodStripped::new);

    public static final RegistryObject<Block> BLOCK_BLOOD_WOOD_PLANKS = BLOCKS.register("blood_wood_planks",
        BlockBloodWoodPlanks::new);

    public static final RegistryObject<Block> BLOCK_BLOOD_SAPLING = BLOCKS.register("blood_sapling",
        BlockBloodSapling::new);

    public static final RegistryObject<Block> BLOCK_BLOOD_CORE = BLOCKS.register("blood_core",
        BlockBloodCore::new);

    public static final RegistryObject<Block> BLOCK_BLOOD_LEAVES = BLOCKS.register("blood_leaves",
        BlockBloodLeaves::new);

    public static final RegistryObject<Block> BLOCK_ANTILIFE = BLOCKS.register("antilife",
        BlockAntiLife::new);

    public static final RegistryObject<Block> BLOCK_CRYSTALLIZED_DEMON_WILL = BLOCKS.register("crystallized_demon_will_block",
        BlockCrystallizedDemonWill::new);

    // Fluid blocks
    public static final RegistryObject<Block> BLOCK_FLUID_ANTILIFE = BLOCKS.register("antilife_fluid",
        BlockFluidAntiLife::new);

    public static final RegistryObject<Block> BLOCK_FLUID_LIVING_TERRA = BLOCKS.register("living_terra_fluid",
        BlockFluidLivingTerra::new);
}
