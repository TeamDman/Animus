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

public class SigilBuilderEntry extends EntryProvider {

    public SigilBuilderEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/reagentbuilder")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "array/sigil_builder")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of the Fast Builder");
        this.pageText("You craft this sigil within an [#](8B0000)Alchemy Array[#]() by combining a "
                + "[#](8B0000)Reagent: Builder[#]() with a [#](B8860B)Reinforced Slate[#](). The reagent "
                + "must first be forged in the [#](8B0000)Tabula Vitae[#]().\\\n\\\n"
                + "When the array completes its working, the [#](8B0000)Sigil of the Fast Builder[#]() "
                + "emerges, a tool for those who would reshape the world at unnatural speed.");

        this.page("effect", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effect");
        this.pageText("The sigil dramatically accelerates your block placement speed, allowing you "
                + "to construct at a pace far beyond mortal hands.\\\n\\\n"
                + "[#](2E8B57)Simply carry it in your inventory or hotbar while placing blocks. "
                + "The sigil works passively. No activation required.[#]()");

        this.page("cost", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("EV Cost");
        this.pageText("The sigil draws a minimal amount of [#](4A0080)Essentia Vitae[#]() per block placed. "
                + "Even modest reserves will sustain extended building sessions.\\\n\\\n"
                + "Perfect for large construction projects, terraforming, and creative building endeavors "
                + "where mortal hands simply cannot keep pace with ambition.");
    }

    @Override
    protected String entryName() {
        return "Sigil of the Fast Builder";
    }

    @Override
    protected String entryDescription() {
        return "Accelerates block placement to supernatural speeds.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_BUILDER.get());
    }

    @Override
    protected String entryId() {
        return "sigil_builder";
    }
}
