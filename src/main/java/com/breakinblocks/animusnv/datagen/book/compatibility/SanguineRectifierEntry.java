package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class SanguineRectifierEntry extends EntryProvider {

    public SanguineRectifierEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sanguine Rectifier");
        this.pageText("The [#](8B0000)Sanguine Rectifier[#]() bridges the worlds of Vitaemancy and "
                + "EvilCraft, converting between [#](8B0000)Essentia Vitae[#]() and EvilCraft's "
                + "[#](8B0000)Blood[#]() fluid.\\\n\\\n"
                + "It operates in two modes simultaneously:\\\n\\\n"
                + "[#](B8860B)EV \u2192 Blood:[#]() Insert a bound [#](8B0000)Orb of Vitae[#]() to "
                + "drain EV from the bound network and produce Blood into adjacent fluid tanks.\\\n\\\n"
                + "[#](B8860B)Blood \u2192 Altar:[#]() Pipe EvilCraft Blood into the rectifier and it "
                + "transfers directly into the linked [#](8B0000)Ara Vitae[#]().");

        this.page("setup", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Setup");
        this.pageText("1. Place the rectifier within 10 blocks of an [#](8B0000)Ara Vitae[#](). "
                + "It will automatically link on placement.\\\n\\\n"
                + "2. Right-click a [#](B8860B)bound[#]() Orb of Vitae onto the rectifier. "
                + "The orb will float above the block.\\\n\\\n"
                + "3. Place fluid tanks (from any mod) adjacent to the rectifier to collect "
                + "the produced Blood.\\\n\\\n"
                + "[#](2E8B57)Sneak + right-click with an empty hand to re-scan for a nearby altar. "
                + "Right-click with an empty hand to retrieve the orb.[#]()");

        this.page("scaling", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Speed & Scaling");
        this.pageText("Transfer rates scale with your altar's [#](8B0000)Speed Runes[#]().\\\n\\\n"
                + "[#](B8860B)EV \u2192 Blood:[#]() Base rate of 100 mB/tick, scaled by altar speed bonus.\\\n\\\n"
                + "[#](B8860B)Blood \u2192 Altar:[#]() Transfers at [#](B8860B)4x[#]() the altar's "
                + "normal rate (also scaled by speed runes).\\\n\\\n"
                + "The conversion ratio between EV and Blood is 1:1 by default. "
                + "All rates are configurable.\\\n\\\n"
                + "[#](2E8B57)Crafted in the Tabula Vitae from a Cauldron, Dark Gem, "
                + "Imbued Slate, and Bloodstained Glass.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sanguine Rectifier";
    }

    @Override
    protected String entryDescription() {
        return "Converts between Essentia Vitae and EvilCraft Blood.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.CAULDRON);
    }

    @Override
    protected String entryId() {
        return "sanguine_rectifier";
    }
}
