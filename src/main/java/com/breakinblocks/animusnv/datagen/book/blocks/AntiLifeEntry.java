package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import com.breakinblocks.animusnv.registry.AnimusBlocks;

public class AntiLifeEntry extends EntryProvider {

    public AntiLifeEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("AntiLife");
        this.pageText("A corrupting substance that spreads like a plague, consuming all blocks "
                + "of a specific type. You create it using the [#](8B0000)Sigil of Consumption[#]().\\\n\\\n"
                + "[#](4A0080)AntiLife is the antithesis of creation, matter unmade, given form "
                + "and purpose by your will alone.[#]()");

        this.page("spreading", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Spreading Mechanics");
        this.pageText("AntiLife spreads to adjacent blocks of the same type as the original "
                + "converted block.\\\n\\\n"
                + "Each spread consumes [#](4A0080)Essentia Vitae[#]() from the caster who "
                + "created it (configurable).\\\n\\\n"
                + "The corruption reaches up to 8 blocks from its origin, though this range "
                + "can be configured through the [#](8B0000)Sigil of Consumption[#]().");

        this.page("decay", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Decay System");
        this.pageText("When any AntiLife block is broken, it triggers a decay chain reaction:\n\n"
                + "- All adjacent AntiLife blocks begin decaying\n\n"
                + "- Decay spreads outward to connected AntiLife\n\n"
                + "- Decayed blocks are removed entirely\\\n\\\n"
                + "This prevents infinite expansion and allows cleanup of corrupted areas.\\\n\\\n"
                + "[#](2E8B57)Blocks tagged animusnv:disallow_antilife are protected from conversion.[#]()");
    }

    @Override
    protected String entryName() {
        return "AntiLife";
    }

    @Override
    protected String entryDescription() {
        return "A corrupting substance that consumes blocks and spreads like plague.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusBlocks.BLOCK_ANTILIFE.get().asItem());
    }

    @Override
    protected String entryId() {
        return "antilife";
    }
}
