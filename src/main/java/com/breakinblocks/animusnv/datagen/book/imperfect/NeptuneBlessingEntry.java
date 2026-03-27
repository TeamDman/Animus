package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class NeptuneBlessingEntry extends EntryProvider {

    public NeptuneBlessingEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Neptune's Blessing");
        this.pageText("An [#](4A0080)imperfect ritual[#]() that grants the blessing of the deep, allowing you to breathe underwater and swim with the grace of dolphins."
                + "\\\n\\\nPlace a [#](8B0000)Prismarine Block[#]() atop an [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Cost:[#]() 2,000 EV per activation"
                + "\\\n[#](B8860B)Duration:[#]() 15 minutes"
                + "\\\n\\\nThe stone remains intact for reuse.");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("Right-click the [#](8B0000)Imperfect Ritual Stone[#]() to receive:"
                + "\n\n- [#](4A0080)Water Breathing[#]() for 15 minutes"
                + "\n\n- [#](4A0080)Dolphin's Grace[#]() for 15 minutes"
                + "\n\n- Both effects have no visible particles"
                + "\\\n\\\n[#](2E8B57)Ideal for extended underwater exploration, ocean monument raids, underwater construction, and searching for shipwrecks.[#]()");
    }

    @Override
    protected String entryName() {
        return "Neptune's Blessing";
    }

    @Override
    protected String entryDescription() {
        return "An imperfect ritual granting water breathing and Dolphin's Grace.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.PRISMARINE);
    }

    @Override
    protected String entryId() {
        return "neptune_blessing";
    }
}
