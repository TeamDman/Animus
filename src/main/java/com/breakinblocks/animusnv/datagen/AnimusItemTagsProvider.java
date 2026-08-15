package com.breakinblocks.animusnv.datagen;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class AnimusItemTagsProvider extends ItemTagsProvider {
    public AnimusItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, Constants.Mod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.FENCES, ItemTags.FENCES);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.SLABS, ItemTags.SLABS);

        tag(ItemTags.SWORDS)
            .add(AnimusItems.RUNIC_SENTIENT_SCYTHE.get())
            .add(AnimusItems.HAND_OF_DEATH.get());

        tag(ItemTags.TRIDENT_ENCHANTABLE)
            .add(AnimusItems.SPEAR_IRON.get())
            .add(AnimusItems.SPEAR_DIAMOND.get())
            .add(AnimusItems.SPEAR_SENTIENT.get())
            .add(AnimusItems.SPEAR_BOUND.get());

        tag(ItemTags.BOW_ENCHANTABLE)
            .add(AnimusItems.SENTIENT_BOW.get())
            .add(AnimusItems.HELLFORGED_BOW.get());

        tag(ItemTags.DURABILITY_ENCHANTABLE)
            .add(AnimusItems.SPEAR_IRON.get())
            .add(AnimusItems.SPEAR_DIAMOND.get())
            .add(AnimusItems.SPEAR_SENTIENT.get())
            .add(AnimusItems.SENTIENT_BOW.get())
            .add(AnimusItems.HELLFORGED_BOW.get())
            .add(AnimusItems.SENTIENT_SHIELD.get());

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
