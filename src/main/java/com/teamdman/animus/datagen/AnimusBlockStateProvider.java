package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.BlockBloodCore;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class AnimusBlockStateProvider extends BlockStateProvider {
    public AnimusBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Constants.Mod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Blood Wood Log - pillar block (rotatable)
        logBlock((net.minecraft.world.level.block.RotatedPillarBlock) AnimusBlocks.BLOCK_BLOOD_WOOD.get());

        // Stripped Blood Wood Log - pillar block (rotatable)
        axisBlock((net.minecraft.world.level.block.RotatedPillarBlock) AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get(),
            modLoc("block/blockbloodwoodstripped"),
            modLoc("block/blockbloodwoodstripped_top")
        );

        // Blood Wood Planks - simple cube all
        simpleBlock(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get(),
            models().cubeAll(
                "blood_wood_planks",
                modLoc("block/blockbloodwoodplanks")
            )
        );

        // Blood Sapling - cross model
        simpleBlock(AnimusBlocks.BLOCK_BLOOD_SAPLING.get(),
            models().cross(
                "blood_sapling",
                modLoc("block/blockbloodsapling")
            ).renderType("cutout")
        );

        // Blood Leaves - cube all with special texture
        simpleBlock(AnimusBlocks.BLOCK_BLOOD_LEAVES.get(),
            models().cubeAll(
                "blood_leaves",
                modLoc("block/blockbloodleavesfancy")
            ).renderType("cutout_mipped")
        );

        // Blood Core - special case with active state
        bloodCoreBlock(AnimusBlocks.BLOCK_BLOOD_CORE.get());
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
