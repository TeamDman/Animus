package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class AnimusBlockTagsProvider extends BlockTagsProvider {
    public AnimusBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Constants.Mod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Blood Wood - logs tag
        this.tag(BlockTags.LOGS)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD.get());

        // Blood Wood - mineable with axe
        this.tag(BlockTags.MINEABLE_WITH_AXE)
            .add(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .add(AnimusBlocks.BLOCK_BLOOD_CORE.get());

        // Blood Leaves - leaves tag
        this.tag(BlockTags.LEAVES)
            .add(AnimusBlocks.BLOCK_BLOOD_LEAVES.get());

        // Blood Leaves - mineable with hoe
        this.tag(BlockTags.MINEABLE_WITH_HOE)
            .add(AnimusBlocks.BLOCK_BLOOD_LEAVES.get());

        // Blood Sapling - saplings tag
        this.tag(BlockTags.SAPLINGS)
            .add(AnimusBlocks.BLOCK_BLOOD_SAPLING.get());
    }
}
