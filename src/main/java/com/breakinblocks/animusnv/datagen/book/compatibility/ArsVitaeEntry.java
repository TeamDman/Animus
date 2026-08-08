package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class ArsVitaeEntry extends EntryProvider {

    public ArsVitaeEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(Identifier.fromNamespaceAndPath("neovitae", "ritual/ritual_ars_vitae"))
                .withMultiblockName("Ritual of Ars Vitae")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Built from standard runes; any Ritual Diviner will serve.[#]()");

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Ars Vitae");
        this.pageText("The [#](4A0080)Ritual of Ars Vitae[#]() pours life essence back into the arcane, drawing EV from the owner's network and filling a Source Jar with Ars Nouveau Source."
                + "\\\n\\\nIt is the mirror of the [#](8B0000)Ritual of Source Vitaeum[#](), which runs the trade the other way.");

        this.page("setup", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Setup");
        this.pageText("Place a [#](8B0000)Source Jar[#]() directly above the Master Ritual Stone. The EV comes from your network, so no altar is needed."
                + "\\\n\\\n[#](B8860B)Activation:[#]() 10,000 EV"
                + "\\\n[#](B8860B)Exchange:[#]() 10 EV per Source (configurable)"
                + "\\\n[#](B8860B)Throughput:[#]() up to 100 Source every 2 seconds (configurable)");

        this.page("behavior", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Behavior");
        this.pageText("A full jar makes the ritual skip its turn entirely, drawing nothing, and EV is only taken for Source the jar actually accepts. The ritual also idles when your network cannot pay for a single point of Source."
                + "\\\n\\\nEvery other Master Ritual Stone within 10 blocks [#](8B0000)doubles[#]() the price, so keep converters apart."
                + "\\\n\\\n[#](2E8B57)The exchange rate is shared with the Ritual of Source Vitaeum, and both directions pay it, so trading back and forth loses value.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Ars Vitae";
    }

    @Override
    protected String entryDescription() {
        return "Converts EV from your network into Source. Requires Ars Nouveau.";
    }

    @Override
    protected GuiSprite entryBackground() {
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
}
