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
    public static final RegistryObject<Block> BLOCK_BLOOD_WOOD = BLOCKS.register("blockbloodwood",
        BlockBloodWood::new);

    public static final RegistryObject<Block> BLOCK_BLOOD_SAPLING = BLOCKS.register("blockbloodsapling",
        BlockBloodSapling::new);

    public static final RegistryObject<Block> BLOCK_BLOOD_CORE = BLOCKS.register("blockbloodcore",
        BlockBloodCore::new);

    public static final RegistryObject<Block> BLOCK_PHANTOM_BUILDER = BLOCKS.register("blockphantombuilder",
        BlockPhantomBuilder::new);

    // TODO: Port remaining blocks:
    // - BlockAntimatter
    // - BlockBloodLeaves
    // - BlockFluidAntimatter (via AnimusFluids)
    // - BlockFluidDirt (via AnimusFluids)
}
