package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class AnimusItemModelProvider extends ItemModelProvider {
    public AnimusItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.Mod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Block items - use parent block model
        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD);
        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED);
        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS);
        blockItem(AnimusBlocks.BLOCK_BLOOD_CORE);
        blockItem(AnimusBlocks.BLOCK_BLOOD_LEAVES);

        // Blood Sapling - special case, uses generated item model
        withExistingParent("blood_sapling", mcLoc("item/generated"))
            .texture("layer0", modLoc("block/blockbloodsapling"));

        // Simple items with textures
        simpleItem(AnimusItems.BLOOD_APPLE, "item/itembloodapple");
        simpleItem(AnimusItems.ALTAR_DIVINER, "item/itemaltardiviner");
        simpleItem(AnimusItems.FRAGMENT_HEALING, "item/itemfragmenthealing");
        simpleItem(AnimusItems.KEY_BINDING, "item/itemkeybinding");
        simpleItem(AnimusItems.MOBSOUL, "item/itemmobsoul");

        // Pilums (Roman Javelins)
        simpleItem(AnimusItems.PILUM_IRON, "item/itempilumiron");
        simpleItem(AnimusItems.PILUM_DIAMOND, "item/itempilumdiamond");
        simpleItem(AnimusItems.PILUM_BOUND, "item/itempilumbound");

        // Sigils - Simple (non-toggleable)
        simpleItem(AnimusItems.SIGIL_CHAINS, "item/itemsigilchains");
        simpleItem(AnimusItems.SIGIL_CONSUMPTION, "item/itemsigilconsumption");
        simpleItem(AnimusItems.SIGIL_STORM, "item/itemsigilstorm");

        // Toggleable Sigils - with activation states
        toggleableSigil(AnimusItems.SIGIL_BUILDER, "sigil_builder");
        toggleableSigil(AnimusItems.SIGIL_LEECH, "itemsigilleech");
        toggleableSigil(AnimusItems.SIGIL_TRANSPOSITION, "sigil_transposition");

        // Fluid Buckets - use custom bucket textures (like Blood Magic's lifebucket.png)
        simpleItem(AnimusItems.ANTIMATTER_BUCKET, "item/antimatter_bucket");
        simpleItem(AnimusItems.DIRT_BUCKET, "item/dirt_bucket");
    }

    /**
     * Creates a toggleable sigil model with activated/deactivated texture variants
     */
    private void toggleableSigil(RegistryObject<Item> item, String baseTextureName) {
        String name = item.getId().getPath();

        // Main model (deactivated state)
        withExistingParent(name, mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + baseTextureName + "_deactivated"))
            .override()
                .predicate(modLoc("activated"), 1.0F)
                .model(getBuilder(name + "_activated"))
            .end();

        // Activated variant model
        withExistingParent(name + "_activated", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + baseTextureName + "_activated"));
    }

    private void blockItem(RegistryObject<Block> block) {
        String name = block.getId().getPath();
        withExistingParent(name, modLoc("block/" + name));
    }

    private void simpleItem(RegistryObject<Item> item, String texturePath) {
        String name = item.getId().getPath();
        withExistingParent(name, mcLoc("item/generated"))
            .texture("layer0", modLoc(texturePath));
    }

    @Override
    public ResourceLocation modLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, name);
    }

    @Override
    public ResourceLocation mcLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", name);
    }
}
