package com.breakinblocks.animusnv.datagen;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class AnimusItemTagsProvider extends ItemTagsProvider {
    public AnimusItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider) {
        super(output, lookupProvider, Constants.Mod.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.LOGS)
            .add(AnimusItems.BLOCK_BLOOD_WOOD.get())
            .add(AnimusItems.BLOCK_BLOOD_WOOD_STRIPPED.get());

        tag(ItemTags.PLANKS)
            .add(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get());

        tag(ItemTags.LEAVES)
            .add(AnimusItems.BLOCK_BLOOD_LEAVES.get());

        tag(ItemTags.SAPLINGS)
            .add(AnimusItems.BLOCK_BLOOD_SAPLING.get());

        tag(ItemTags.WOODEN_FENCES)
            .add(AnimusItems.BLOCK_BLOOD_WOOD_FENCE.get());

        tag(ItemTags.FENCES)
            .add(AnimusItems.BLOCK_BLOOD_WOOD_FENCE.get());

        tag(ItemTags.FENCE_GATES)
            .add(AnimusItems.BLOCK_BLOOD_WOOD_FENCE_GATE.get());

        tag(ItemTags.WOODEN_STAIRS)
            .add(AnimusItems.BLOCK_BLOOD_WOOD_STAIRS.get());

        tag(ItemTags.STAIRS)
            .add(AnimusItems.BLOCK_BLOOD_WOOD_STAIRS.get());

        tag(ItemTags.WOODEN_SLABS)
            .add(AnimusItems.BLOCK_BLOOD_WOOD_SLAB.get());

        tag(ItemTags.SLABS)
            .add(AnimusItems.BLOCK_BLOOD_WOOD_SLAB.get());

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

        tag(Constants.Tags.MALUM_ENCHANTABLE_REBOUND)
            .addOptional(AnimusItems.RUNIC_SENTIENT_SCYTHE.get())
            .addOptional(AnimusItems.HAND_OF_DEATH.get());

        tag(Constants.Tags.MALUM_ENCHANTABLE_ASCENSION)
            .addOptional(AnimusItems.RUNIC_SENTIENT_SCYTHE.get())
            .addOptional(AnimusItems.HAND_OF_DEATH.get());

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
