package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class EnhancementEntry extends EntryProvider {

    public EnhancementEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Enhancement");
        this.pageText("An [#](4A0080)imperfect ritual[#]() that pushes enchantments beyond their natural limits. Every enchantment on the held item gains one additional level, and the item is permanently marked to prevent further enhancement."
                + "\\\n\\\nPlace an [#](8B0000)Amethyst Block[#]() atop an [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Cost:[#]() 5,000 EV per activation"
                + "\\\n\\\nThe stone remains intact for reuse.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Hold the item you wish to enhance in your main hand, then right-click the [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\n\n- All enchantments gain +1 level"
                + "\n\n- The item is marked as enhanced"
                + "\n\n- Items can only be enhanced once"
                + "\n\n- The enhancement mark cannot be removed through normal means"
                + "\\\n\\\n[#](2E8B57)Enhanced items are ignored by the Ritual of Unmaking by default (configurable). Use the Imperfect Ritual of Reduction to reverse this process.[#]()");
    }

    @Override
    protected String entryName() {
        return "Enhancement";
    }

    @Override
    protected String entryDescription() {
        return "An imperfect ritual that raises all enchantment levels by one.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.AMETHYST_BLOCK);
    }

    @Override
    protected String entryId() {
        return "enhancement";
    }
}
