package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class EntropyEntry extends EntryProvider {

    public EntropyEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(ResourceLocation.fromNamespaceAndPath("neovitae", "ritual/ritual_entropy"))
                .withMultiblockName("Ritual of Entropy")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Built from standard runes; any Ritual Diviner will serve.[#]()");

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Entropy");
        this.pageText("All matter yearns to return to its most basic form. The [#](4A0080)Ritual of Entropy[#]() obliges, converting any items in a chest directly above it into cobblestone."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Details");
        this.pageText("[#](B8860B)Activation:[#]() 1,000 EV"
                + "\\\n[#](B8860B)Refresh:[#]() 1 EV per item"
                + "\\\n[#](B8860B)Refresh Time:[#]() 1 tick"
                + "\\\n\\\nPlace a chest one block above the [#](8B0000)Master Ritual Stone[#](). The ritual converts items in the chest to cobblestone at a rate of one item per tick."
                + "\\\n\\\nCobblestone itself is not converted.");

        this.page("applications", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Applications");
        this.pageText("This ritual excels at:"
                + "\n\n- Trash disposal for unwanted items"
                + "\n\n- Converting excess mob drops into building material"
                + "\n\n- Generating cobblestone from renewable farm output"
                + "\n\n- Clearing accumulated junk from storage"
                + "\\\n\\\n[#](2E8B57)Pair with a mob farm for a steady cobblestone supply at minimal EV cost.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Entropy";
    }

    @Override
    protected String entryDescription() {
        return "Reduces all items in a chest to cobblestone.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.COBBLESTONE);
    }

    @Override
    protected String entryId() {
        return "entropy";
    }
}
