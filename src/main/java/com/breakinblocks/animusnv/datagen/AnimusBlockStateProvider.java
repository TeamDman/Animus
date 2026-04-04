package com.breakinblocks.animusnv.datagen;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.blocks.BlockBloodCore;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AnimusBlockStateProvider extends BlockStateProvider {
    public AnimusBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Constants.Mod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        axisBlock((RotatedPillarBlock) AnimusBlocks.BLOCK_BLOOD_WOOD.get(),
            modLoc("block/blockbloodwood"),
            modLoc("block/blockbloodwood_top")
        );

        axisBlock((RotatedPillarBlock) AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get(),
            modLoc("block/stripped_bloodwood_log"),
            modLoc("block/stripped_bloodwood_log_top")
        );

        simpleBlock(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get(),
            models().cubeAll(
                "blood_wood_planks",
                modLoc("block/bloodwood_planks")
            )
        );

        stairsBlock(
            (net.minecraft.world.level.block.StairBlock) AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS.get(),
            modLoc("block/bloodwood_planks")
        );

        slabBlock(
            (net.minecraft.world.level.block.SlabBlock) AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB.get(),
            modLoc("block/blood_wood_planks"),
            modLoc("block/bloodwood_planks")
        );

        fenceBlock(
            (net.minecraft.world.level.block.FenceBlock) AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE.get(),
            modLoc("block/bloodwood_planks")
        );

        fenceGateBlock(
            (net.minecraft.world.level.block.FenceGateBlock) AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE.get(),
            modLoc("block/bloodwood_planks")
        );

        simpleBlock(AnimusBlocks.BLOCK_BLOOD_SAPLING.get(),
            models().cross(
                "blood_sapling",
                modLoc("block/blockbloodsapling")
            ).renderType("cutout")
        );

        simpleBlock(AnimusBlocks.BLOCK_BLOOD_LEAVES.get(),
            models().cubeAll(
                "blood_leaves",
                modLoc("block/blockbloodleavesfancy")
            ).renderType("cutout_mipped")
        );

        bloodCoreBlock(AnimusBlocks.BLOCK_BLOOD_CORE.get());

        simpleBlock(AnimusBlocks.BLOCK_CRYSTALLIZED_SPIRITUS.get(),
            models().cubeAll(
                "crystallized_spiritus_block",
                modLoc("block/crystallized_spiritus_block")
            )
        );

        if (net.neoforged.fml.ModList.get().isLoaded("evilcraft")) {
            simpleBlock(com.breakinblocks.animusnv.compat.EvilCraftCompat.SANGUINE_RECTIFIER.get(),
                models().getExistingFile(modLoc("block/block_sanguine_rectifier")));
        }

        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE.get(), "willful_stone");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_WHITE.get(), "willful_stone_white");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_ORANGE.get(), "willful_stone_orange");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_MAGENTA.get(), "willful_stone_magenta");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_BLUE.get(), "willful_stone_light_blue");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_YELLOW.get(), "willful_stone_yellow");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_LIME.get(), "willful_stone_lime");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_PINK.get(), "willful_stone_pink");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_GRAY.get(), "willful_stone_light_gray");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_CYAN.get(), "willful_stone_cyan");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_PURPLE.get(), "willful_stone_purple");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_BLUE.get(), "willful_stone_blue");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_BROWN.get(), "willful_stone_brown");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_GREEN.get(), "willful_stone_green");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_RED.get(), "willful_stone_red");
        willfulStoneBlock(AnimusBlocks.BLOCK_WILLFUL_STONE_BLACK.get(), "willful_stone_black");
    }

    private void willfulStoneBlock(Block block, String name) {
        simpleBlock(block,
            models().cubeAll(
                name,
                modLoc("block/" + name)
            )
        );
    }

    private void bloodCoreBlock(Block block) {
        ModelFile inactive = models().cubeColumn(
            "blood_core",
            modLoc("block/blockbloodcore"),
            modLoc("block/blockbloodcoreheart")
        );

        ModelFile active = models().cubeColumn(
            "blood_core_active",
            modLoc("block/blockbloodcore_active"),
            modLoc("block/blockbloodcoreheart")
        );

        getVariantBuilder(block)
            .partialState().with(BlockBloodCore.ACTIVE, false)
                .modelForState().modelFile(inactive).addModel()
            .partialState().with(BlockBloodCore.ACTIVE, true)
                .modelForState().modelFile(active).addModel();
    }

    @Override
    public ResourceLocation modLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, name);
    }
}
