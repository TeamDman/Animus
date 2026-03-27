package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class ReductionEntry extends EntryProvider {

    public ReductionEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Reduction");
        this.pageText("An [#](4A0080)imperfect ritual[#]() that reverses enchantment growth and strips the enhancement mark from items. All enchantments on the held item are reduced by one level."
                + "\\\n\\\nPlace a [#](8B0000)Block of Quartz[#]() atop an [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Cost:[#]() 1,000 EV per activation"
                + "\\\n\\\nThe stone remains intact for reuse.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Hold the item you wish to reduce in your main hand, then right-click the [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\n\n- All enchantments lose 1 level"
                + "\n\n- The enhancement mark is removed"
                + "\n\n- Enchantments will not drop below level 1"
                + "\\\n\\\n[#](2E8B57)This is the counterpart to the Imperfect Ritual of Enhancement. Use it to reverse an enhancement or carefully downgrade enchantments.[#]()");
    }

    @Override
    protected String entryName() {
        return "Reduction";
    }

    @Override
    protected String entryDescription() {
        return "An imperfect ritual that lowers enchantment levels by one.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.QUARTZ_BLOCK);
    }

    @Override
    protected String entryId() {
        return "reduction";
    }
}
