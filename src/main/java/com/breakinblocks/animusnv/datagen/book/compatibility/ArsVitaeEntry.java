package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.condition.BookModLoadedConditionModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class ArsVitaeEntry extends EntryProvider {

    public ArsVitaeEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(ResourceLocation.fromNamespaceAndPath("neovitae", "ritual/ritual_ars_vitae"))
                .withMultiblockName("Ritual of Ars Vitae")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Built from standard runes; any Ritual Diviner will serve.[#]()");

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Ars Vitae");
        this.pageText("The [#](4A0080)Ritual of Ars Vitae[#]() pours life essence back into the arcane, draining EV from a nearby Ara Vitae and filling a Source Jar with Ars Nouveau Source."
                + "\\\n\\\nIt is the mirror of the [#](8B0000)Ritual of Source Vitaeum[#](), which runs the trade the other way.");

        this.page("setup", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Setup");
        this.pageText("Place a [#](8B0000)Source Jar[#]() directly above the Master Ritual Stone, with an Ara Vitae within 8 blocks (configurable)."
                + "\\\n\\\n[#](B8860B)Activation:[#]() 10,000 EV"
                + "\\\n[#](B8860B)Exchange:[#]() 10 EV per Source (configurable)"
                + "\\\n[#](B8860B)Throughput:[#]() up to 100 Source every 2 seconds (configurable)");

        this.page("behavior", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Behavior");
        this.pageText("EV is only taken for Source the jar actually accepts, so a full jar costs nothing. The ritual idles when the altar cannot pay for a single point of Source."
                + "\\\n\\\nEvery other Master Ritual Stone within 10 blocks of the altar [#](8B0000)doubles[#]() the price, so keep converters apart."
                + "\\\n\\\n[#](2E8B57)The exchange rate is shared with the Ritual of Source Vitaeum, and both directions pay it, so trading back and forth loses value.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Ars Vitae";
    }

    @Override
    protected String entryDescription() {
        return "Converts EV from an Ara Vitae into Source. Requires Ars Nouveau.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.GLASS_BOTTLE);
    }

    @Override
    protected String entryId() {
        return "ars_vitae";
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return super.additionalSetup(entry)
                .withCondition(BookModLoadedConditionModel.create().withModId("ars_nouveau"));
    }
}
