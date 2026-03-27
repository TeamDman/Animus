package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class SolEntry extends EntryProvider {

    public SolEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Sol");
        this.pageText("Banish the darkness. The [#](4A0080)Ritual of Sol[#]() automatically places light sources in dark areas, preventing mob spawns and illuminating your surroundings. Place light-emitting blocks in a chest above the ritual."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("mechanics", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("How It Works");
        this.pageText("The ritual requires a chest (or any block with item storage) placed directly above the [#](8B0000)Master Ritual Stone[#](). Every 5 ticks, it attempts to:"
                + "\n\n- Find a valid block item from the chest"
                + "\n\n- Search for a dark spot (light level < 8) with solid ground below"
                + "\n\n- Place the block"
                + "\n\n- Consume EV for the operation");

        this.page("search", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Search Algorithm");
        this.pageText("The ritual searches from center outward, starting at the [#](8B0000)Master Ritual Stone[#]() and expanding in square rings. It scans downward only, processing each column from top to bottom. Up to 64 positions are checked per tick."
                + "\\\n\\\nThe search resumes where it left off between ticks, ensuring efficient coverage.");

        this.page("blood_light", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Blood Light Synergy");
        this.pageText("If you place a [#](8B0000)Sigil of Blood Light[#]() in the chest, the ritual will place [#](8B0000)Blood Lights[#]() without consuming the sigil. This allows for infinite blood light placement using only EV."
                + "\\\n\\\n[#](2E8B57)An incredibly efficient way to light vast areas with a single sigil and a steady supply of Essentia Vitae.[#]()");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Range");
        this.pageText("[#](B8860B)Activation:[#]() 1,000 EV"
                + "\\\n[#](B8860B)Refresh:[#]() 1 EV per block placed"
                + "\\\n[#](B8860B)Refresh Time:[#]() 5 ticks"
                + "\\\n\\\n[#](B8860B)Horizontal Range:[#]() 32 blocks (configurable)"
                + "\\\n[#](B8860B)Vertical Range:[#]() 64 blocks (configurable, -1 for world bottom)"
                + "\\\n\\\nRequires dark spots (light level < 8) with solid ground below to place blocks.");
    }

    @Override
    protected String entryName() {
        return "Ritual of Sol";
    }

    @Override
    protected String entryDescription() {
        return "Automatically places light sources in dark areas.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.TORCH);
    }

    @Override
    protected String entryId() {
        return "sol";
    }
}
