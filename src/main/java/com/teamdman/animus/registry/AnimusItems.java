package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.Mod.MODID);

    // Block Items
    public static final RegistryObject<Item> BLOCK_BLOOD_WOOD = ITEMS.register("blockbloodwood",
        () -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_WOOD.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLOCK_BLOOD_SAPLING = ITEMS.register("blockbloodsapling",
        () -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_SAPLING.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLOCK_BLOOD_CORE = ITEMS.register("blockbloodcore",
        () -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_CORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLOCK_PHANTOM_BUILDER = ITEMS.register("blockphantombuilder",
        () -> new BlockItem(AnimusBlocks.BLOCK_PHANTOM_BUILDER.get(), new Item.Properties()));

    // Regular Items
    public static final RegistryObject<Item> BLOOD_APPLE = ITEMS.register("bloodapple",
        ItemBloodApple::new);

    public static final RegistryObject<Item> FRAGMENT_HEALING = ITEMS.register("fragmenthealing",
        ItemFragmentHealing::new);

    // TODO: Port remaining items:
    // - ItemAltarDiviner (complex Blood Magic integration)
    // - ItemKama
    // - ItemKamaBound
    // - ItemKeyBinding
    // - ItemMobSoul
    // - All Sigils (separate phase)
}
