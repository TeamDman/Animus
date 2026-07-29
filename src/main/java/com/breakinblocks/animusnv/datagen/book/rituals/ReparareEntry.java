package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class ReparareEntry extends EntryProvider {

    public ReparareEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Reparare");
        this.pageText("The [#](4A0080)Ritual of Reparare[#]() mends damaged items stored in an inventory above the ritual stone. Place a chest directly above the [#](8B0000)Master Ritual Stone[#]() and items within will slowly be restored to full durability."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("mechanics", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Mechanics");
        this.pageText("[#](B8860B)Activation:[#]() 5,000 EV"
                + "\\\n[#](B8860B)Cost:[#]() Per durability point repaired (configurable)"
                + "\\\n[#](B8860B)Repair Rate:[#]() 20%% of max durability per cycle"
                + "\\\n[#](B8860B)Interval:[#]() Every 5 seconds (configurable)");

        this.page("setup", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Setup");
        this.pageText("Place any container with item capability directly above the [#](8B0000)Master Ritual Stone[#]()."
                + "\\\n\\\nItems tagged with [#](8B0000)animus:disallow_repair[#]() will be skipped by the ritual."
                + "\\\n\\\n[#](2E8B57)A passive, hands-free way to keep your best tools in fighting shape. Simply toss them in a chest and let blood mend what blood has broken.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Reparare";
    }

    @Override
    protected String entryDescription() {
        return "Repairs damaged items stored in a chest above the ritual.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.ANVIL);
    }

    @Override
    protected String entryId() {
        return "reparare";
    }
}
