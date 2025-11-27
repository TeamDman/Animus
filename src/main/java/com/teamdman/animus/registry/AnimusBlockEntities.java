package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.blockentities.BlockEntityAntiLife;
import com.teamdman.animus.blockentities.BlockEntityBloodCore;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.Mod.MODID);

    public static final RegistryObject<BlockEntityType<BlockEntityBloodCore>> BLOOD_CORE = BLOCK_ENTITIES.register(
        "bloodcore",
        () -> BlockEntityType.Builder.of(
            BlockEntityBloodCore::new,
            AnimusBlocks.BLOCK_BLOOD_CORE.get()
        ).build(null)
    );

    public static final RegistryObject<BlockEntityType<BlockEntityAntiLife>> ANTILIFE = BLOCK_ENTITIES.register(
        "antilife",
        () -> BlockEntityType.Builder.of(
            BlockEntityAntiLife::new,
            AnimusBlocks.BLOCK_ANTILIFE.get()
        ).build(null)
    );
}
