package com.breakinblocks.animusnv.datagen;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class AnimusBlockTagsProvider extends BlockTagsProvider {
    public AnimusBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Constants.Mod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.LOGS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get());

        this.tag(BlockTags.PLANKS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get());

        this.tag(BlockTags.LEAVES)
            .add(AnimusBlocks.BLOCK_BLOOD_LEAVES.get());

        this.tag(BlockTags.SAPLINGS)
            .add(AnimusBlocks.BLOCK_BLOOD_SAPLING.get());

        this.tag(BlockTags.WOODEN_FENCES)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE.get());

        this.tag(BlockTags.FENCES)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE.get());

        this.tag(BlockTags.FENCE_GATES)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE.get());

        this.tag(BlockTags.WOODEN_STAIRS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS.get());

        this.tag(BlockTags.STAIRS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS.get());

        this.tag(BlockTags.WOODEN_SLABS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB.get());

        this.tag(BlockTags.SLABS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB.get());

        this.tag(BlockTags.MINEABLE_WITH_AXE)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE.get())
            .add(AnimusBlocks.BLOCK_BLOOD_CORE.get());

        this.tag(BlockTags.MINEABLE_WITH_HOE)
            .add(AnimusBlocks.BLOCK_BLOOD_LEAVES.get());

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(AnimusBlocks.BLOCK_ANTILIFE.get())
            .add(AnimusBlocks.BLOCK_CRYSTALLIZED_SPIRITUS.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_WHITE.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_ORANGE.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_MAGENTA.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_BLUE.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_YELLOW.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_LIME.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_PINK.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_GRAY.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_CYAN.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_PURPLE.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_BLUE.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_BROWN.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_GREEN.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_RED.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE_BLACK.get())
            .addOptional(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "arcane_rune"))
            .addOptional(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "rune_unleashed_nature"));

        this.tag(BlockTags.NEEDS_STONE_TOOL)
            .add(AnimusBlocks.BLOCK_ANTILIFE.get())
            .add(AnimusBlocks.BLOCK_CRYSTALLIZED_SPIRITUS.get())
            .add(AnimusBlocks.BLOCK_WILLFUL_STONE.get())
            .addOptional(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "arcane_rune"))
            .addOptional(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "rune_unleashed_nature"));
    }
}
