package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class RelentlessTidesEntry extends EntryProvider {

    public RelentlessTidesEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Relentless Tides");
        this.pageText("The inverse of the [#](4A0080)Ritual of Siphon[#](). Where Siphon drains the world below, Relentless Tides extracts fluid from a tank above and places it into the world. Create lakes, flood caves, or fill moats with any fluid you desire."
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
        this.pageText("The ritual places fluid source blocks, letting Minecraft physics handle the natural flow."
                + "\\\n\\\nFluid is only placed in air, replaceable blocks, or flowing fluid of the same type. The search pattern starts directly below the ritual and expands outward, skipping positions already filled."
                + "\\\n\\\n[#](2E8B57)Pair with the Ritual of Siphon to move entire bodies of fluid from one location to another.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Relentless Tides";
    }

    @Override
    protected String entryDescription() {
        return "Extracts fluid from a tank above and places it into the world.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.WATER_BUCKET);
    }

    @Override
    protected String entryId() {
        return "relentless_tides";
    }
}
