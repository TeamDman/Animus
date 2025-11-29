package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.blockentities.BlockEntityAntiLife;
import com.teamdman.animus.blockentities.BlockEntityBloodCore;
import com.teamdman.animus.blockentities.BlockEntityWillfulStone;
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

    public static final RegistryObject<BlockEntityType<BlockEntityWillfulStone>> WILLFUL_STONE = BLOCK_ENTITIES.register(
        "willful_stone",
        () -> BlockEntityType.Builder.of(
            BlockEntityWillfulStone::new,
            AnimusBlocks.BLOCK_WILLFUL_STONE.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_WHITE.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_ORANGE.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_MAGENTA.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_BLUE.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_YELLOW.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_LIME.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_PINK.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_GRAY.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_CYAN.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_PURPLE.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_BLUE.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_BROWN.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_GREEN.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_RED.get(),
            AnimusBlocks.BLOCK_WILLFUL_STONE_BLACK.get()
        ).build(null)
    );
}
