package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.*;
import com.teamdman.animus.items.sigils.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.Mod.MODID);

    /**
     * Helper method to register a BlockItem for a given block
     */
    private static RegistryObject<Item> registerBlockItem(String name, RegistryObject<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Block Items
    public static final RegistryObject<Item> BLOCK_BLOOD_WOOD = registerBlockItem("blood_wood", AnimusBlocks.BLOCK_BLOOD_WOOD);
    public static final RegistryObject<Item> BLOCK_BLOOD_SAPLING = registerBlockItem("blood_sapling", AnimusBlocks.BLOCK_BLOOD_SAPLING);
    public static final RegistryObject<Item> BLOCK_BLOOD_CORE = registerBlockItem("blood_core", AnimusBlocks.BLOCK_BLOOD_CORE);

    // Regular Items
    public static final RegistryObject<Item> BLOOD_APPLE = ITEMS.register("blood_apple",
        ItemBloodApple::new);

    public static final RegistryObject<Item> FRAGMENT_HEALING = ITEMS.register("fragment_healing",
        ItemFragmentHealing::new);

    // Mob Soul - used by Sigil of Chains
    public static final RegistryObject<Item> MOBSOUL = ITEMS.register("mob_soul",
        () -> new Item(new Item.Properties().stacksTo(1)));

    // Sigils
    public static final RegistryObject<Item> SIGIL_BUILDER = ITEMS.register("sigil_builder",
        ItemSigilBuilder::new);

    public static final RegistryObject<Item> SIGIL_CHAINS = ITEMS.register("sigil_chains",
        ItemSigilChains::new);

    public static final RegistryObject<Item> SIGIL_CONSUMPTION = ITEMS.register("sigil_consumption",
        ItemSigilConsumption::new);

    public static final RegistryObject<Item> SIGIL_LEECH = ITEMS.register("sigil_leech",
        ItemSigilLeech::new);

    public static final RegistryObject<Item> SIGIL_STORM = ITEMS.register("sigil_storm",
        ItemSigilStorm::new);

    public static final RegistryObject<Item> SIGIL_TRANSPOSITION = ITEMS.register("sigil_transposition",
        ItemSigilTransposition::new);

    // TODO: Port remaining items:
    // - ItemAltarDiviner (complex Blood Magic integration)
    // - ItemKama
    // - ItemKamaBound
    // - ItemKeyBinding
}
