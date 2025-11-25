package com.teamdman.animus.datagen;

import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnimusLootTableProvider extends LootTableProvider {
    public AnimusLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(AnimusBlockLoot::new, LootContextParamSets.BLOCK)
        ));
    }

    private static class AnimusBlockLoot extends BlockLootSubProvider {
        protected AnimusBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            // Blood Wood - drops itself
            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_WOOD.get());

            // Blood Sapling - drops itself
            this.dropSelf(AnimusBlocks.BLOCK_BLOOD_SAPLING.get());

            // Blood Core - drops blood wood, or itself with silk touch
            this.add(AnimusBlocks.BLOCK_BLOOD_CORE.get(),
                block -> createSingleItemTableWithSilkTouch(
                    block,
                    AnimusBlocks.BLOCK_BLOOD_WOOD.get()
                )
            );

            // Blood Leaves - complex drops like oak leaves
            LootItemCondition.Builder noShearsSilk = net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition.invert(
                net.minecraft.world.level.storage.loot.predicates.MatchTool.toolMatches(
                    net.minecraft.advancements.critereon.ItemPredicate.Builder.item()
                        .of(net.minecraft.world.item.Items.SHEARS)
                )
                .or(MatchTool.toolMatches(
                    net.minecraft.advancements.critereon.ItemPredicate.Builder.item()
                        .hasEnchantment(new net.minecraft.advancements.critereon.EnchantmentPredicate(
                            Enchantments.SILK_TOUCH,
                            net.minecraft.advancements.critereon.MinMaxBounds.Ints.atLeast(1)
                        ))
                ))
            );

            this.add(AnimusBlocks.BLOCK_BLOOD_LEAVES.get(), block ->
                createLeavesDrops(block, AnimusBlocks.BLOCK_BLOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES)
                    .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(noShearsSilk)
                        .add(this.applyExplosionCondition(block, LootItem.lootTableItem(net.minecraft.world.item.Items.STICK)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                            .when(net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition.bonusLevelFlatChance(
                                Enchantments.BLOCK_FORTUNE,
                                0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F
                            ))
                        )
                    )
                    .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(noShearsSilk)
                        .add(this.applyExplosionCondition(block, LootItem.lootTableItem(AnimusItems.BLOOD_APPLE.get()))
                            .when(net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition.bonusLevelFlatChance(
                                Enchantments.BLOCK_FORTUNE,
                                0.02F, 0.022222223F, 0.025F, 0.033333335F
                            ))
                        )
                    )
            );
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            // Return all registered blocks from AnimusBlocks
            return AnimusBlocks.BLOCKS.getEntries().stream()
                .map(RegistryObject::get)
                .collect(Collectors.toList());
        }
    }
}
