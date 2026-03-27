package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class RegressionEntry extends EntryProvider {

    public RegressionEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Regression");
        this.pageText("An [#](4A0080)imperfect ritual[#]() that strips away the accumulated repair cost from tools and weapons. The anvil penalty that makes items \"Too Expensive\" is wiped clean, rendering them repairable once more."
                + "\\\n\\\nPlace a [#](8B0000)Bookshelf[#]() atop an [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Cost:[#]() 3,000 EV per activation"
                + "\\\n\\\nThe stone remains intact for reuse.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Hold the item you wish to reset in your main hand, then right-click the [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\n\n- The item's repair cost tag is removed"
                + "\n\n- The item becomes fully repairable again"
                + "\n\n- Works on any item with accumulated anvil penalty"
                + "\\\n\\\n[#](2E8B57)Invaluable for maintaining heavily-enchanted tools that have hit the \"Too Expensive\" ceiling. Keep your best gear in service indefinitely.[#]()");
    }

    @Override
    protected String entryName() {
        return "Regression";
    }

    @Override
    protected String entryDescription() {
        return "An imperfect ritual that removes anvil repair costs from items.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.BOOKSHELF);
    }

    @Override
    protected String entryId() {
        return "regression";
    }
}
