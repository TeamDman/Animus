package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class SigilLeachEntry extends EntryProvider {

    public SigilLeachEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/reagentleach")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "array/sigil_leach")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Nature's Leach");
        this.pageText("Forged in an [#](8B0000)Alchemy Array[#]() from a [#](8B0000)Reagent: Leach[#]() "
                + "and a [#](B8860B)Reinforced Slate[#](), this toggleable sigil drains the life from "
                + "nature itself to sustain your hunger. The green world withers so that you might thrive.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Sneak + right-click to toggle the sigil on or off. While active and you can eat, "
                + "the sigil consumes plant matter at a cost of [#](4A0080)5 EV[#]() per consumption:\n\n"
                + "- First searches your inventory for plant items\n\n"
                + "- Then scans nearby blocks within 8 blocks\n\n"
                + "- Destroys consumable blocks and restores 1-3 hunger + saturation");

        this.page("details", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Consumable Blocks");
        this.pageText("The sigil devours logs, leaves, saplings, flowers, crops, grass, kelp, seagrass, "
                + "vines, and most bonemealable plants.\\\n\\\n"
                + "Each block consumed generates [#](4A0080)Corrosive Spiritus[#]() (0.3-0.8 per block) "
                + "in the local chunk.\\\n\\\n"
                + "[#](2E8B57)The hungry mage need never farm again.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of Nature's Leach";
    }

    @Override
    protected String entryDescription() {
        return "Drains nearby plant life to automatically restore your hunger.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_LEACH.get());
    }

    @Override
    protected String entryId() {
        return "sigil_leach";
    }
}
