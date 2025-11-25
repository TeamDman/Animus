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

    public static final RegistryObject<Block> BLOCK_BLOOD_SAPLING = BLOCKS.register("blood_sapling",
        BlockBloodSapling::new);

    public static final RegistryObject<Block> BLOCK_BLOOD_CORE = BLOCKS.register("blood_core",
        BlockBloodCore::new);

    // TODO: Port remaining blocks:
    // - BlockAntimatter
    // - BlockBloodLeaves
    // - BlockFluidAntimatter (via AnimusFluids)
    // - BlockFluidDirt (via AnimusFluids)
}
