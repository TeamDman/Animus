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
        // Imperfect Ritual Stone - skip datagen, will be created manually

        // Blood Sapling - special case, uses generated item model
        withExistingParent("blood_sapling", mcLoc("item/generated"))
            .texture("layer0", modLoc("block/blockbloodsapling"));

        // Simple items with textures
        simpleItem(AnimusItems.BLOOD_APPLE, "item/itembloodapple");
        simpleItem(AnimusItems.SANGUINE_DIVINER, "item/itemsanguinediviner");
        simpleItem(AnimusItems.FRAGMENT_HEALING, "item/itemfragmenthealing");
        boundItem(AnimusItems.KEY_BINDING, "itemkeybinding"); // Toggleable between unbound/bound
        simpleItem(AnimusItems.ACTIVATION_CRYSTAL_FRAGILE, "item/itemactivationcrystalfragile");
        simpleItem(AnimusItems.MOBSOUL, "item/itemmobsoul");

        // Pilums (Roman Javelins)
        simpleItem(AnimusItems.PILUM_IRON, "item/itempilumiron");
        simpleItem(AnimusItems.PILUM_DIAMOND, "item/itempilumdiamond");
        toggleableSigil(AnimusItems.PILUM_BOUND, "itempilumbound"); // Toggleable between activated/deactivated
        simpleItem(AnimusItems.PILUM_SENTIENT, "item/itempilumsentient");

        // Sigils - Simple (non-toggleable)
        simpleItem(AnimusItems.SIGIL_CHAINS, "item/itemsigilchains");
        simpleItem(AnimusItems.SIGIL_CONSUMPTION, "item/itemsigilconsumption");
        simpleItem(AnimusItems.SIGIL_STORM, "item/itemsigilstorm");
        simpleItem(AnimusItems.SIGIL_FREE_SOUL, "item/sigil_free_soul");
        simpleItem(AnimusItems.SIGIL_TEMPORAL_DOMINANCE, "item/sigil_temporal_dominance");
        simpleItem(AnimusItems.SIGIL_EQUIVALENCY, "item/sigil_equivalency");

        // Toggleable Sigils - with activation states
        toggleableSigil(AnimusItems.SIGIL_BUILDER, "sigil_builder");
        toggleableSigil(AnimusItems.SIGIL_LEACH, "itemsigilleach");
        toggleableSigil(AnimusItems.SIGIL_TRANSPOSITION, "sigil_transposition");

        // Active state Sigils - with active/inactive states
        activeSigil(AnimusItems.SIGIL_REMEDIUM, "sigil_remedium");
        activeSigil(AnimusItems.SIGIL_REPARARE, "sigil_reparare");
        activeSigil(AnimusItems.SIGIL_HEAVENLY_WRATH, "sigil_heavenly_wrath");
        activeSigil(AnimusItems.SIGIL_BOUNDLESS_NATURE, "sigil_boundless_nature");

        // Fluid Buckets - use custom bucket textures (like Blood Magic's lifebucket.png)
        simpleItem(AnimusItems.ANTILIFE_BUCKET, "item/antilife_bucket");
        simpleItem(AnimusItems.LIVING_TERRA_BUCKET, "item/living_terra_bucket");
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

    /**
     * Creates an active sigil model with active/inactive texture variants
     * Uses the "active" predicate (for sigils that use the Active NBT tag)
     */
    private void activeSigil(RegistryObject<Item> item, String baseTextureName) {
        String name = item.getId().getPath();

        // Main model (inactive state)
        withExistingParent(name, mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + baseTextureName))
            .override()
                .predicate(modLoc("active"), 1.0F)
                .model(getBuilder(name + "_active"))
            .end();

        // Active variant model
        withExistingParent(name + "_active", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + baseTextureName + "_active"));
    }

    /**
     * Creates a bound item model with unbound/bound texture variants
     */
    private void boundItem(RegistryObject<Item> item, String baseTextureName) {
        String name = item.getId().getPath();

        // Main model (unbound state)
        withExistingParent(name, mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + baseTextureName))
            .override()
                .predicate(modLoc("bound"), 1.0F)
                .model(getBuilder(name + "_active"))
            .end();

        // Bound variant model
        withExistingParent(name + "_active", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + baseTextureName + "_active"));
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
