package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import com.breakinblocks.animusnv.registry.AnimusItems;

public class AntiLifeFluidEntry extends EntryProvider {

    public AntiLifeFluidEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("AntiLife Fluid");
        this.pageText("A corrupting liquid that spreads destruction. [#](8B0000)AntiLife fluid[#]() "
                + "is created when lightning strikes [#](8B0000)Essentia Vitae[#](), "
                + "transforming it into a dark, consuming force.\\\n\\\n"
                + "[#](4A0080)Where Essentia Vitae carries the spark of life, this fluid "
                + "carries its negation, a tide of unmaking.[#]()");

        this.page("creation", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Creation");
        this.pageText("Place [#](8B0000)Essentia Vitae[#]() fluid and strike it with lightning. "
                + "Use a [#](8B0000)Lightning Rod[#]() or the [#](8B0000)Sigil of the Storm[#]() "
                + "to call down the bolt.\\\n\\\n"
                + "The Essentia Vitae instantly transforms into AntiLife fluid, which:\n\n"
                + "- Spreads like water\n\n"
                + "- Consumes blocks it touches\n\n"
                + "- Cannot be reversed\n\n"
                + "- Can be collected in buckets");

        this.page("behavior", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Behavior");
        this.pageText("When AntiLife fluid touches blocks, it converts them to "
                + "[#](8B0000)AntiLife blocks[#](), which continue to spread the corruption.\\\n\\\n"
                + "When AntiLife fluid contacts [#](8B0000)Essentia Vitae[#]() fluid, "
                + "it spreads as fluid instead of converting to blocks, rapidly corrupting "
                + "large volumes.\\\n\\\n"
                + "[#](2E8B57)Handle with extreme care. This fluid is not easily contained "
                + "once released.[#]()");
    }

    @Override
    protected String entryName() {
        return "AntiLife Fluid";
    }

    @Override
    protected String entryDescription() {
        return "A corrupting liquid born when lightning strikes Essentia Vitae.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.ANTILIFE_BUCKET.get());
    }

    @Override
    protected String entryId() {
        return "antilife_fluid";
    }
}
