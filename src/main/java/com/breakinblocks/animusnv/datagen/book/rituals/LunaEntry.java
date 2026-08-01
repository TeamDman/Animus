package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class LunaEntry extends EntryProvider {

    public LunaEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(Identifier.fromNamespaceAndPath("neovitae", "ritual/ritual_luna"))
                .withMultiblockName("Ritual of Luna")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Built from standard runes; any Ritual Diviner will serve.[#]()");

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Luna");
        this.pageText("The counterpart to the [#](4A0080)Ritual of Sol[#](). Where Sol banishes darkness, Luna harvests the light. This ritual collects all light-emitting blocks within its range and stores them in a chest above the ritual stone."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("mechanics", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("How It Works");
        this.pageText("Place a chest (or any block with item storage) directly above the [#](8B0000)Master Ritual Stone[#](). Every 5 ticks, the ritual attempts to:"
                + "\n\n- Find a light-emitting block (light level > 0)"
                + "\n\n- Harvest the block"
                + "\n\n- Store it in the chest (or drop at the stone if no chest)"
                + "\n\n- Consume EV for the operation");

        this.page("search", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Search Algorithm");
        this.pageText("The ritual uses a center-outward search to find light sources. It starts at the [#](8B0000)Master Ritual Stone[#](), searches downward only, and expands in square rings. Each column is scanned from top to bottom, prioritizing nearby lights before distant ones."
                + "\\\n\\\nUp to 1,024 positions are processed per tick, and the search resumes where it left off between ticks.");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Range");
        this.pageText("[#](B8860B)Activation:[#]() 1,000 EV"
                + "\\\n[#](B8860B)Refresh:[#]() 1 EV per block harvested"
                + "\\\n[#](B8860B)Refresh Time:[#]() 5 ticks"
                + "\\\n\\\n[#](B8860B)Horizontal Range:[#]() 32 blocks (configurable)"
                + "\\\n[#](B8860B)Vertical Range:[#]() 64 blocks (configurable, -1 for world bottom)"
                + "\\\n\\\nHarvests any light-emitting block. If no chest is present, items drop at the stone."
                + "\\\n\\\n[#](2E8B57)Pair with the Ritual of Sol for an automated cycle of light placement and reclamation.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Luna";
    }

    @Override
    protected String entryDescription() {
        return "Harvests all light-emitting blocks within range.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.GLOWSTONE);
    }

    @Override
    protected String entryId() {
        return "luna";
    }
}
