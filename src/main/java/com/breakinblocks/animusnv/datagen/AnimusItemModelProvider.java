package com.breakinblocks.animusnv.datagen;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AnimusItemModelProvider extends ItemModelProvider {
    private static final String[] SPIRITUS_ASPECTS = {"", "ruina", "nihilum", "vindicta", "invictus"};

    public AnimusItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.Mod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD, "blood_wood");
        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED, "blood_wood_stripped");
        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS, "blood_wood_planks");
        blockItem(AnimusBlocks.BLOCK_BLOOD_CORE, "blood_core");
        blockItem(AnimusBlocks.BLOCK_BLOOD_LEAVES, "blood_leaves");
        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS, "blood_wood_stairs");
        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB, "blood_wood_slab");
        blockItem(AnimusBlocks.BLOCK_CRYSTALLIZED_SPIRITUS, "crystallized_spiritus_block");

        withExistingParent("blood_wood_fence", mcLoc("block/fence_inventory"))
            .texture("texture", modLoc("block/bloodwood_planks"));

        blockItem(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE, "blood_wood_fence_gate");

        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE, "willful_stone");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_WHITE, "willful_stone_white");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_ORANGE, "willful_stone_orange");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_MAGENTA, "willful_stone_magenta");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_BLUE, "willful_stone_light_blue");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_YELLOW, "willful_stone_yellow");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_LIME, "willful_stone_lime");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_PINK, "willful_stone_pink");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_GRAY, "willful_stone_light_gray");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_CYAN, "willful_stone_cyan");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_PURPLE, "willful_stone_purple");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_BLUE, "willful_stone_blue");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_BROWN, "willful_stone_brown");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_GREEN, "willful_stone_green");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_RED, "willful_stone_red");
        blockItem(AnimusBlocks.BLOCK_WILLFUL_STONE_BLACK, "willful_stone_black");

        withExistingParent("blood_sapling", mcLoc("item/generated"))
            .texture("layer0", modLoc("block/blockbloodsapling"));

        simpleItem(AnimusItems.BLOOD_APPLE, "item/blood_apple");
        simpleItem(AnimusItems.BLOOD_ORB_TRANSCENDENT, "item/blood_orb_transcendent");
        simpleItem(AnimusItems.SANGUINE_DIVINER, "item/sanguine_diviner");
        simpleItem(AnimusItems.FRAGMENT_HEALING, "item/fragment_healing");
        simpleItem(AnimusItems.ACTIVATION_CRYSTAL_FRAGILE, "item/activation_crystal_fragile");
        simpleItem(AnimusItems.MOBSOUL, "item/mob_soul");

        keyBindingItem(AnimusItems.KEY_BINDING);

        simpleItem(AnimusItems.REAGENT_BUILDER, "item/reagentbuilder");
        simpleItem(AnimusItems.REAGENT_CHAINS, "item/reagentchains");
        simpleItem(AnimusItems.REAGENT_CONSUMPTION, "item/reagentconsumption");
        simpleItem(AnimusItems.REAGENT_LEACH, "item/reagentleach");
        simpleItem(AnimusItems.REAGENT_STORM, "item/reagentstorm");
        simpleItem(AnimusItems.REAGENT_TRANSPOSITION, "item/reagenttransposition");
        simpleItem(AnimusItems.REAGENT_BOUNDLESS_NATURE, "item/reagentboundlessnature");
        simpleItem(AnimusItems.REAGENT_EQUIVALENCY, "item/reagentequivalency");
        simpleItem(AnimusItems.REAGENT_FREE_SOUL, "item/reagentfreesoul");
        simpleItem(AnimusItems.REAGENT_HEAVENLY_WRATH, "item/reagentheavelywrath");
        simpleItem(AnimusItems.REAGENT_REMEDIUM, "item/reagentremendium");
        simpleItem(AnimusItems.REAGENT_REPARARE, "item/reagentreparare");
        simpleItem(AnimusItems.REAGENT_TEMPORAL_DOMINANCE, "item/reagenttemporaldominance");
        simpleItem(AnimusItems.REAGENT_FIST, "item/reagentfist");

        simpleItem(AnimusItems.SIGIL_CHAINS, "item/sigil_chains");
        simpleItem(AnimusItems.SIGIL_CONSUMPTION, "item/sigil_consumption");
        simpleItem(AnimusItems.SIGIL_STORM, "item/sigil_storm");
        simpleItem(AnimusItems.SIGIL_FREE_SOUL, "item/sigil_free_soul");
        simpleItem(AnimusItems.SIGIL_TEMPORAL_DOMINANCE, "item/sigil_temporal_dominance");
        simpleItem(AnimusItems.SIGIL_EQUIVALENCY, "item/sigil_equivalency");
        simpleItem(AnimusItems.SIGIL_MONK, "item/sigil_monk");

        toggleableSigil(AnimusItems.SIGIL_BUILDER, "sigil_builder_deactivated", "sigil_builder_activated");
        toggleableSigil(AnimusItems.SIGIL_LEACH, "sigil_leach_deactivated", "sigil_leach_activated");
        toggleableSigil(AnimusItems.SIGIL_TRANSPOSITION, "sigil_transposition_deactivated", "sigil_transposition_activated");
        toggleableSigil(AnimusItems.SIGIL_REMEDIUM, "sigil_remedium", "sigil_remedium_active");
        toggleableSigil(AnimusItems.SIGIL_REPARARE, "sigil_reparare", "sigil_reparare_active");
        toggleableSigil(AnimusItems.SIGIL_HEAVENLY_WRATH, "sigil_heavenly_wrath", "sigil_heavenly_wrath_active");

        simpleItem(AnimusItems.ANTILIFE_BUCKET, "item/antilife_bucket");
        simpleItem(AnimusItems.LIVING_TERRA_BUCKET, "item/living_terra_bucket");

        spiritusBowItem(AnimusItems.SENTIENT_BOW, "sentient_bow");
        bowItem(AnimusItems.HELLFORGED_BOW, "hellforged_bow");

    }

    private void spiritusBowItem(DeferredHolder<Item, Item> item, String textureName) {
        String name = item.getId().getPath();

        for (String aspect : SPIRITUS_ASPECTS) {
            String suffix = aspect.isEmpty() ? "" : "_" + aspect;
            for (int stage = 0; stage < 3; stage++) {
                withExistingParent(name + suffix + "_pulling_" + stage, mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/" + textureName + suffix + "_pulling_" + stage));
            }
            if (!suffix.isEmpty()) {
                withExistingParent(name + suffix, mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/" + textureName + suffix));
            }
        }

        ItemModelBuilder builder = withExistingParent(name, mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + textureName));

        for (int type = 1; type < SPIRITUS_ASPECTS.length; type++) {
            builder.override()
                .predicate(modLoc("spiritus_type"), type)
                .model(getBuilder(name + "_" + SPIRITUS_ASPECTS[type]))
                .end();
        }

        float[] pullStages = {0.0F, 0.65F, 0.9F};
        for (int stage = 0; stage < pullStages.length; stage++) {
            for (int type = 0; type < SPIRITUS_ASPECTS.length; type++) {
                String suffix = SPIRITUS_ASPECTS[type].isEmpty() ? "" : "_" + SPIRITUS_ASPECTS[type];
                ItemModelBuilder.OverrideBuilder override = builder.override()
                    .predicate(mcLoc("pulling"), 1.0F)
                    .predicate(modLoc("spiritus_type"), type);
                if (pullStages[stage] > 0.0F) {
                    override.predicate(mcLoc("pull"), pullStages[stage]);
                }
                override.model(getBuilder(name + suffix + "_pulling_" + stage)).end();
            }
        }
    }

    private void bowItem(DeferredHolder<Item, Item> item, String textureName) {
        String name = item.getId().getPath();

        withExistingParent(name + "_pulling_0", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + textureName + "_pulling_0"));

        withExistingParent(name + "_pulling_1", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + textureName + "_pulling_1"));

        withExistingParent(name + "_pulling_2", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + textureName + "_pulling_2"));

        withExistingParent(name, mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + textureName))
            .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .model(getBuilder(name + "_pulling_0"))
            .end()
            .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .predicate(mcLoc("pull"), 0.65F)
                .model(getBuilder(name + "_pulling_1"))
            .end()
            .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .predicate(mcLoc("pull"), 0.9F)
                .model(getBuilder(name + "_pulling_2"))
            .end();
    }

    private void toggleableSigil(DeferredHolder<Item, Item> item, String deactivatedTexture, String activatedTexture) {
        String name = item.getId().getPath();

        withExistingParent(name, mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + deactivatedTexture))
            .override()
                .predicate(modLoc("activated"), 1.0F)
                .model(getBuilder(name + "_activated"))
            .end();

        withExistingParent(name + "_activated", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/" + activatedTexture));
    }

    private void keyBindingItem(DeferredHolder<Item, Item> item) {
        String name = item.getId().getPath();

        withExistingParent(name, mcLoc("item/generated"))
            .texture("layer0", modLoc("item/key_binding"))
            .override()
                .predicate(modLoc("bound"), 1.0F)
                .model(getBuilder(name + "_bound"))
            .end();

        withExistingParent(name + "_bound", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/key_binding_active"));
    }

    private void blockItem(DeferredHolder<Block, Block> block, String modelName) {
        String name = block.getId().getPath();
        withExistingParent(name, modLoc("block/" + modelName));
    }

    private void simpleItem(DeferredHolder<Item, ? extends Item> item, String texturePath) {
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
