package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
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
        // Copy block tags to item tags for wood-related items
        copy(net.minecraft.tags.BlockTags.LOGS, ItemTags.LOGS);
        copy(net.minecraft.tags.BlockTags.PLANKS, ItemTags.PLANKS);
        copy(net.minecraft.tags.BlockTags.LEAVES, ItemTags.LEAVES);
        copy(net.minecraft.tags.BlockTags.SAPLINGS, ItemTags.SAPLINGS);

        // Willful stones tag
        tag(Constants.Tags.WILLFUL_STONES)
            .add(AnimusItems.BLOCK_WILLFUL_STONE.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_WHITE.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_ORANGE.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_MAGENTA.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_LIGHT_BLUE.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_YELLOW.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_LIME.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_PINK.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_LIGHT_GRAY.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_CYAN.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_PURPLE.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_BLUE.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_BROWN.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_GREEN.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_RED.get())
            .add(AnimusItems.BLOCK_WILLFUL_STONE_BLACK.get());
    }
}
