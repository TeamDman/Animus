package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;

public class SigilTemporalDominanceEntry extends EntryProvider {

    public SigilTemporalDominanceEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "hellfire_forge/reagenttemporaldominance")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "array/sigil_temporal_dominance")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Temporal Dominance");
        this.pageText("A sigil that accelerates time for block entities. Furnaces smelt faster, crops "
                + "grow more quickly, and machines process at up to 32x normal speed, all bent to your will "
                + "through the expenditure of [#](4A0080)Essentia Vitae[#]().");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Sneak + right-click on a block entity to activate. Each activation lasts "
                + "30 seconds and increases the speed level. Repeated clicks on the same block stack "
                + "the acceleration up to [#](B8860B)32x speed[#]().");

        this.page("progression", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Progression");
        this.pageText("Each tier costs more [#](4A0080)EV[#]():\n\n"
                + "- [#](B8860B)Level 1:[#]() 2x speed, 1,000 EV\n\n"
                + "- [#](B8860B)Level 2:[#]() 4x speed, 2,000 EV\n\n"
                + "- [#](B8860B)Level 3:[#]() 8x speed, 4,000 EV\n\n"
                + "- [#](B8860B)Level 4:[#]() 16x speed, 8,000 EV\n\n"
                + "- [#](B8860B)Level 5:[#]() 32x speed, 16,000 EV\\\n\\\n"
                + "Refreshing the timer at max level costs 15,000 EV.");

        this.page("compat", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Compatibility");
        this.pageText("Blocks tagged with [#](4A0080)animus:disallow_acceleration[#]() cannot be "
                + "accelerated.\\\n\\\n"
                + "The sigil does not stack with GAG's Temporal Pouch, Time in a Bottle, or "
                + "JustDireThings' Time Wand.\\\n\\\n"
                + "[#](4A0080)Time bends to the will of blood.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of Temporal Dominance";
    }

    @Override
    protected String entryDescription() {
        return "Accelerates block entities up to 32x speed through stacking time dilation.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_TEMPORAL_DOMINANCE.get());
    }

    @Override
    protected String entryId() {
        return "sigil_temporal_dominance";
    }
}
