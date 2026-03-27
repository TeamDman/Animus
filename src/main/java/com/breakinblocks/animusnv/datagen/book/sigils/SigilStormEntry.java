package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;

public class SigilStormEntry extends EntryProvider {

    public SigilStormEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of the Storm");
        this.pageText("Forged in an [#](8B0000)Alchemy Array[#]() from a [#](8B0000)Reagent: Storm[#]() "
                + "and a [#](B8860B)Reinforced Slate[#](), this sigil harnesses the fury of the heavens. "
                + "Right-click to call down lightning at a targeted location up to 64 blocks away, at "
                + "a cost of [#](4A0080)500 EV[#]() per strike.");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("When you target water, the lightning yields fishing loot (2-5 rolls by default). "
                + "Loot spawns 1 second after the strike to prevent items from burning.\\\n\\\n"
                + "During rain, the strike deals area damage to entities within 5 blocks of the "
                + "impact point.");

        this.page("config", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Configuration");
        this.pageText("The fish loot spawning can be configured:\n\n"
                + "- Minimum rolls: 2 (default)\n\n"
                + "- Maximum rolls: 5 (default)\n\n"
                + "- Set both to 0 to disable\\\n\\\n"
                + "[#](2E8B57)This sigil excels at fishing, combat, and demonstrating your mastery "
                + "of the elements.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of the Storm";
    }

    @Override
    protected String entryDescription() {
        return "Calls down targeted lightning strikes. Also useful for fishing.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_STORM.get());
    }

    @Override
    protected String entryId() {
        return "sigil_storm";
    }
}
