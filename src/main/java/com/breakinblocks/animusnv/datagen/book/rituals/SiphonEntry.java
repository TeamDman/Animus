package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class SiphonEntry extends EntryProvider {

    public SiphonEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Siphon");
        this.pageText("The [#](4A0080)Ritual of Siphon[#]() extracts fluids from the world below and deposits them into a tank above. It searches from center outward, draining oceans, lava lakes, or any fluid source it encounters."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner [Dusk][#]().");

        this.page("setup", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Setup");
        this.pageText("Place any fluid container directly above the [#](8B0000)Master Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Activation:[#]() 5,000 EV"
                + "\\\n[#](B8860B)Per Bucket:[#]() 50 EV (configurable)"
                + "\\\n[#](B8860B)Range:[#]() 32 blocks horizontal, 128 blocks deep (configurable)");

        this.page("behavior", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Behavior");
        this.pageText("The search pattern starts directly below the ritual and expands outward in rings."
                + "\\\n\\\nExtracted fluid blocks are replaced with [#](8B0000)Antilife Block[#]() by default (configurable). Smoke particles appear when the tank is full or no fluid is found."
                + "\\\n\\\n[#](2E8B57)Pair with the Ritual of Relentless Tides to relocate entire bodies of fluid from one location to another.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Siphon";
    }

    @Override
    protected String entryDescription() {
        return "Extracts fluids from the world below into a tank above.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.BUCKET);
    }

    @Override
    protected String entryId() {
        return "siphon";
    }
}
