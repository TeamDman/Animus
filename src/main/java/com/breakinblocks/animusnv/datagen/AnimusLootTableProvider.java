package com.breakinblocks.animusnv.datagen;

import com.breakinblocks.animusnv.compat.EvilCraftCompat;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AnimusLootTableProvider extends LootTableProvider {
    public AnimusLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(AnimusBlockLoot::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    private static class AnimusBlockLoot extends BlockLootSubProvider {
        protected AnimusBlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_WOOD.get());
            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get());
            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS.get());
            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS.get());

            this.add(AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB.get(),
                block -> createSlabItemTable(AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB.get()));

            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE.get());
            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE.get());
            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_SAPLING.get());

            this.add(AnimusBlocks.BLOCK_BLOOD_CORE.get(),
                block -> createSingleItemTableWithSilkTouch(
                    block,
                    AnimusBlocks.BLOCK_BLOOD_WOOD.get()
                )
            );

            this.dropSelf(AnimusBlocks.BLOCK_CRYSTALLIZED_SPIRITUS.get());
            // EvilCraft compat block (registered in EvilCraftCompat)
            if (net.neoforged.fml.ModList.get().isLoaded("evilcraft")) {
                this.dropSelf(EvilCraftCompat.SANGUINE_RECTIFIER.get());
            }

            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_WHITE.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_ORANGE.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_MAGENTA.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_BLUE.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_YELLOW.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_LIME.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_PINK.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_GRAY.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_CYAN.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_PURPLE.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_BLUE.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_BROWN.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_GREEN.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_RED.get());
            this.dropSelf(AnimusBlocks.BLOCK_WILLFUL_STONE_BLACK.get());

            HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

            this.add(AnimusBlocks.BLOCK_BLOOD_LEAVES.get(), block ->
                createLeavesDrops(block, AnimusBlocks.BLOCK_BLOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES)
                    .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(this.noShearsOrSilkTouch())
                        .add(this.applyExplosionCondition(block, LootItem.lootTableItem(Items.STICK)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                enchantmentLookup.getOrThrow(Enchantments.FORTUNE),
                                0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F
                            ))
                        )
                    )
                    .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(this.noShearsOrSilkTouch())
                        .add(this.applyExplosionCondition(block, LootItem.lootTableItem(AnimusItems.BLOOD_APPLE.get()))
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                enchantmentLookup.getOrThrow(Enchantments.FORTUNE),
                                0.02F, 0.022222223F, 0.025F, 0.033333335F
                            ))
                        )
                    )
            );
        }

        // Parent class constants for this condition aren't accessible, so we construct it manually
        private LootItemCondition.Builder noShearsOrSilkTouch() {
            HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
            return InvertedLootItemCondition.invert(
                AnyOfCondition.anyOf(
                    MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)),
                    MatchTool.toolMatches(ItemPredicate.Builder.item().withSubPredicate(
                        ItemSubPredicates.ENCHANTMENTS,
                        ItemEnchantmentsPredicate.enchantments(
                            List.of(new EnchantmentPredicate(
                                enchantmentLookup.getOrThrow(Enchantments.SILK_TOUCH),
                                MinMaxBounds.Ints.atLeast(1)
                            ))
                        )
                    ))
                )
            );
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            var blocks = new ArrayList<Block>();
            AnimusBlocks.BLOCKS.getEntries().stream()
                .filter(entry -> entry != AnimusBlocks.BLOCK_FLUID_ANTILIFE
                    && entry != AnimusBlocks.BLOCK_FLUID_LIVING_TERRA
                    && entry != AnimusBlocks.BLOCK_ANTILIFE)
                .map(DeferredHolder::get)
                .forEach(blocks::add);

            if (ModList.get().isLoaded("evilcraft")) {
                EvilCraftCompat.BLOCKS.getEntries().stream()
                    .map(DeferredHolder::get)
                    .forEach(blocks::add);
            }

            return blocks;
        }
    }
}
