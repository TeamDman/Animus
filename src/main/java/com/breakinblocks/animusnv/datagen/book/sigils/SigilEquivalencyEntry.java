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

public class SigilEquivalencyEntry extends EntryProvider {

    public SigilEquivalencyEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/reagentequivalency")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "array/sigil_equivalency")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Equivalency");
        this.pageText("An equal-exchange sigil for rapid block replacement. You select a palette of "
                + "blocks, then swap connected blocks of the same type within a configurable radius. "
                + "The foundation of all alchemical transformation begins here.");

        this.page("selection", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Selection Mode");
        this.pageText("Build your replacement palette:\n\n"
                + "- Sneak + Right-click a block to add it to your selection\n\n"
                + "- Sneak + Right-click air to clear the entire selection\n\n"
                + "- You may select up to 20 different block types\\\n\\\n"
                + "Only blocks present in your inventory (survival) or currently selected (creative) "
                + "will be used as replacements.");

        this.page("replacement", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Replacement Mode");
        this.pageText("Right-click a block to begin replacement. The sigil flood-fills connected blocks "
                + "of the same type along the clicked face's plane, replacing them with random blocks "
                + "from your selection.\\\n\\\n"
                + "Scroll while sneaking to adjust the radius (1-32 blocks). Each replacement costs "
                + "[#](4A0080)EV[#]() per block (configurable).");

        this.page("features", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Features");
        this.pageText("Replaced blocks drop with [#](8B0000)Silk Touch[#]() behaviour.\\\n\\\n"
                + "Replacements are [#](4A0080)plane-locked[#](). Only blocks on the same plane as the "
                + "clicked face (floor, wall, or ceiling) are affected.\\\n\\\n"
                + "[#](2E8B57)Replacements happen gradually over time to reduce server strain.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of Equivalency";
    }

    @Override
    protected String entryDescription() {
        return "An equal-exchange sigil for rapid block replacement across surfaces.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_EQUIVALENCY.get());
    }

    @Override
    protected String entryId() {
        return "sigil_equivalency";
    }
}
