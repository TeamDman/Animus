package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
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
        // LOGS TAG - All log variants
        this.tag(BlockTags.LOGS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get());

        // PLANKS TAG - All plank variants
        this.tag(BlockTags.PLANKS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get());

        // LEAVES TAG
        this.tag(BlockTags.LEAVES)
            .add(AnimusBlocks.BLOCK_BLOOD_LEAVES.get());

        // SAPLINGS TAG
        this.tag(BlockTags.SAPLINGS)
            .add(AnimusBlocks.BLOCK_BLOOD_SAPLING.get());

        // WOODEN FENCES TAG
        this.tag(BlockTags.WOODEN_FENCES)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE.get());

        // FENCES TAG (for connection logic)
        this.tag(BlockTags.FENCES)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE.get());

        // FENCE GATES TAG
        this.tag(BlockTags.FENCE_GATES)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE.get());

        // STAIRS TAG
        this.tag(BlockTags.STAIRS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS.get());

        // SLABS TAG
        this.tag(BlockTags.SLABS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB.get());

        // MINEABLE WITH AXE - Wood blocks and core
        this.tag(BlockTags.MINEABLE_WITH_AXE)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE.get())
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE.get())
            .add(AnimusBlocks.BLOCK_BLOOD_CORE.get());

        // MINEABLE WITH HOE - Leaves
        this.tag(BlockTags.MINEABLE_WITH_HOE)
            .add(AnimusBlocks.BLOCK_BLOOD_LEAVES.get());

        // MINEABLE WITH PICKAXE - AntiLife block
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(AnimusBlocks.BLOCK_ANTILIFE.get());

        // No mining level requirements - all blocks mineable with any tier
        // (Blood wood blocks are wood tier, antilife has no special requirements)
    }
}
