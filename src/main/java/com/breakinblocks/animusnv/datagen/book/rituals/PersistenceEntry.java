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

public class PersistenceEntry extends EntryProvider {

    public PersistenceEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(Identifier.fromNamespaceAndPath("neovitae", "ritual/ritual_persistence"))
                .withMultiblockName("Ritual of Persistence")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Use a Ritual Diviner [Dusk] for easier construction.[#]()");

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Persistence");
        this.pageText("The [#](4A0080)Ritual of Persistence[#]() forces the world to remain aware of its surroundings, keeping chunks loaded even when no players are nearby. Essential for automation and remote bases."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner [Dusk][#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs");
        this.pageText("[#](B8860B)Activation:[#]() 50,000 EV"
                + "\\\n[#](B8860B)Upkeep:[#]() 100 EV per tick (configurable)"
                + "\\\n[#](B8860B)Range:[#]() 3 chunk radius (configurable)"
                + "\\\n\\\nThe ritual maintains chunk loading in a square area around itself. Chunks unload immediately if EV runs out.");

        this.page("notes", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage Notes");
        this.pageText("A radius of 3 means a 7x7 chunk area, 49 chunks total."
                + "\\\n\\\nChunks remain loaded even across server restarts, so long as the ritual is active and [#](4A0080)Essentia Vitae[#]() continues to flow."
                + "\\\n\\\n[#](2E8B57)Pair with a reliable EV generation system such as the Ritual of Nature's Leach to ensure your automation never goes dark.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Persistence";
    }

    @Override
    protected String entryDescription() {
        return "Keeps chunks loaded even without players nearby.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.ENDER_EYE);
    }

    @Override
    protected String entryId() {
        return "persistence";
    }
}
