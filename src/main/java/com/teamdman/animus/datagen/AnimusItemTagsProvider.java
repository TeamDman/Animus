package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class AnimusItemTagsProvider extends ItemTagsProvider {
    public AnimusItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, Constants.Mod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Copy block tags to item tags
        copy(net.minecraft.tags.BlockTags.LOGS, ItemTags.LOGS);
        copy(net.minecraft.tags.BlockTags.LEAVES, ItemTags.LEAVES);
        copy(net.minecraft.tags.BlockTags.SAPLINGS, ItemTags.SAPLINGS);
    }
}
