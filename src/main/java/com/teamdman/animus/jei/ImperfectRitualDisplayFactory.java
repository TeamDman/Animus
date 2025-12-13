package com.teamdman.animus.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create all ImperfectRitualDisplay objects for JEI
 * Handles both vanilla and mod-dependent rituals
 */
public class ImperfectRitualDisplayFactory {

    public static List<ImperfectRitualDisplay> createAllDisplays() {
        List<ImperfectRitualDisplay> displays = new ArrayList<>();

        // === Vanilla Rituals (always available) ===

        // Ritual of Regression - Bookshelf
        displays.add(new ImperfectRitualDisplay(
            "regression",
            Blocks.BOOKSHELF.defaultBlockState(),
            3000,
            Component.translatable("jei.animus.ritual.regression.name"),
            Component.translatable("jei.animus.ritual.regression.desc"),
            null
        ));

        // Ritual of Hunger - Bone Block
        displays.add(new ImperfectRitualDisplay(
            "hunger",
            Blocks.BONE_BLOCK.defaultBlockState(),
            500,
            Component.translatable("jei.animus.ritual.hunger.name"),
            Component.translatable("jei.animus.ritual.hunger.desc"),
            null
        ));

        // Ritual of Enhancement - Amethyst Block
        displays.add(new ImperfectRitualDisplay(
            "enhancement",
            Blocks.AMETHYST_BLOCK.defaultBlockState(),
            5000,
            Component.translatable("jei.animus.ritual.enhancement.name"),
            Component.translatable("jei.animus.ritual.enhancement.desc"),
            null
        ));

        // Ritual of Reduction - Bookshelf (same trigger as Regression, different effect)
        displays.add(new ImperfectRitualDisplay(
            "reduction",
            Blocks.BOOKSHELF.defaultBlockState(),
            1000,
            Component.translatable("jei.animus.ritual.reduction.name"),
            Component.translatable("jei.animus.ritual.reduction.desc"),
            null
        ));

        // Ritual of Boundless Skies - Ancient Debris
        displays.add(new ImperfectRitualDisplay(
            "boundless_skies",
            Blocks.ANCIENT_DEBRIS.defaultBlockState(),
            10000,
            Component.translatable("jei.animus.ritual.boundless_skies.name"),
            Component.translatable("jei.animus.ritual.boundless_skies.desc"),
            null
        ));

        // Ritual of Clear Skies - Glowstone
        displays.add(new ImperfectRitualDisplay(
            "clear_skies",
            Blocks.GLOWSTONE.defaultBlockState(),
            1000,
            Component.translatable("jei.animus.ritual.clear_skies.name"),
            Component.translatable("jei.animus.ritual.clear_skies.desc"),
            null
        ));

        // Ritual of Neptune's Blessing - Prismarine
        displays.add(new ImperfectRitualDisplay(
            "neptune_blessing",
            Blocks.PRISMARINE.defaultBlockState(),
            2000,
            Component.translatable("jei.animus.ritual.neptune_blessing.name"),
            Component.translatable("jei.animus.ritual.neptune_blessing.desc"),
            null
        ));

        // Ritual of the Warden - Sculk
        displays.add(new ImperfectRitualDisplay(
            "warden",
            Blocks.SCULK.defaultBlockState(),
            3000,
            Component.translatable("jei.animus.ritual.warden.name"),
            Component.translatable("jei.animus.ritual.warden.desc"),
            null
        ));

        // === Mod-Dependent Rituals ===

        // Botania - Manasteel Soul
        if (ModList.get().isLoaded("botania")) {
            Block manasteelBlock = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("botania", "manasteel_block")).orElse(null);
            if (manasteelBlock != null && manasteelBlock != Blocks.AIR) {
                displays.add(new ImperfectRitualDisplay(
                    "manasteel_soul",
                    manasteelBlock.defaultBlockState(),
                    2500,
                    Component.translatable("jei.animus.ritual.manasteel_soul.name"),
                    Component.translatable("jei.animus.ritual.manasteel_soul.desc"),
                    "Botania"
                ));
            }
        }

        // Malum - Soul Stained Blood
        if (ModList.get().isLoaded("malum")) {
            Block hallowedGoldBlock = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("malum", "block_of_hallowed_gold")).orElse(null);
            if (hallowedGoldBlock != null && hallowedGoldBlock != Blocks.AIR) {
                displays.add(new ImperfectRitualDisplay(
                    "soul_stained_blood",
                    hallowedGoldBlock.defaultBlockState(),
                    2000,
                    Component.translatable("jei.animus.ritual.soul_stained_blood.name"),
                    Component.translatable("jei.animus.ritual.soul_stained_blood.desc"),
                    "Malum"
                ));
            }
        }

        // Ars Nouveau - Magi
        if (ModList.get().isLoaded("ars_nouveau")) {
            Block sourceGemBlock = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("ars_nouveau", "source_gem_block")).orElse(null);
            if (sourceGemBlock != null && sourceGemBlock != Blocks.AIR) {
                displays.add(new ImperfectRitualDisplay(
                    "magi",
                    sourceGemBlock.defaultBlockState(),
                    2500,
                    Component.translatable("jei.animus.ritual.magi.name"),
                    Component.translatable("jei.animus.ritual.magi.desc"),
                    "Ars Nouveau"
                ));
            }
        }

        // Iron's Spellbooks - Iron Heart
        if (ModList.get().isLoaded("irons_spellbooks")) {
            Block arcaneAnvil = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "arcane_anvil")).orElse(null);
            if (arcaneAnvil != null && arcaneAnvil != Blocks.AIR) {
                displays.add(new ImperfectRitualDisplay(
                    "iron_heart",
                    arcaneAnvil.defaultBlockState(),
                    3500,
                    Component.translatable("jei.animus.ritual.iron_heart.name"),
                    Component.translatable("jei.animus.ritual.iron_heart.desc"),
                    "Iron's Spellbooks"
                ));
            }
        }

        return displays;
    }
}
