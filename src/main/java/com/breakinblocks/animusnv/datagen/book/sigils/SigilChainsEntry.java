package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;

public class SigilChainsEntry extends EntryProvider {

    public SigilChainsEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of the Phantom Chain");
        this.pageText("Forged in an [#](8B0000)Alchemy Array[#]() from a [#](8B0000)Reagent: Chains[#]() "
                + "and an [#](B8860B)Imbued Slate[#](), this sigil captures the essence of living creatures. "
                + "It binds entities into [#](8B0000)Mob Soul[#]() items, preserving their form and state "
                + "for later release.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Capture");
        this.pageText("Right-click on an entity to attempt capture. The creature is drawn into a "
                + "[#](8B0000)Mob Soul[#](), preserving:\n\n"
                + "- Health and active effects\n\n"
                + "- Equipment and inventory\n\n"
                + "- Custom names\n\n"
                + "- AI state\\\n\\\n"
                + "The [#](4A0080)Essentia Vitae[#]() cost varies by entity.");

        this.page("applications", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Applications");
        this.pageText("The sigil excels at relocating villagers, preserving rare mobs, creating "
                + "living displays, setting up mob farms, and ensuring safe transport of dangerous "
                + "creatures.\\\n\\\n"
                + "Release a captured entity by right-clicking the [#](8B0000)Mob Soul[#]() on any block.\\\n\\\n"
                + "[#](2E8B57)Boss entities and extremely powerful mobs resist capture entirely.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of the Phantom Chain";
    }

    @Override
    protected String entryDescription() {
        return "Captures living creatures into portable Mob Soul containers.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_CHAINS.get());
    }

    @Override
    protected String entryId() {
        return "sigil_chains";
    }
}
