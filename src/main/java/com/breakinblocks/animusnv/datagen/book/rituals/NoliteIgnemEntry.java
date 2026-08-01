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

public class NoliteIgnemEntry extends EntryProvider {

    public NoliteIgnemEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(ResourceLocation.fromNamespaceAndPath("neovitae", "ritual/ritual_nolite_ignem"))
                .withMultiblockName("Ritual of Nolite Ignem")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Built from standard runes; any Ritual Diviner will serve.[#]()");

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Nolite Ignem");
        this.pageText("[#](4A0080)Nolite Ignem[#](), Do Not Burn. This ritual continuously extinguishes all fires within its range, protecting your constructions from accidental or intentional destruction by flame."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs");
        this.pageText("[#](B8860B)Activation:[#]() 5,000 EV"
                + "\\\n[#](B8860B)Per Fire:[#]() 10 EV per fire extinguished"
                + "\\\n[#](B8860B)Range:[#]() 64 blocks (configurable)"
                + "\\\n[#](B8860B)Interval:[#]() Every second");

        this.page("coverage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Coverage");
        this.pageText("The ritual extinguishes both regular fire and [#](8B0000)soul fire[#]() within a spherical range, not a cube."
                + "\\\n\\\nIf your [#](4A0080)Essentia Vitae[#]() runs low, the ritual will extinguish as many fires as it can afford before pausing."
                + "\\\n\\\n[#](2E8B57)An excellent safeguard for wooden builds, libraries, or any structure vulnerable to flame.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Nolite Ignem";
    }

    @Override
    protected String entryDescription() {
        return "Continuously extinguishes all fires within range.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.FIRE_CHARGE);
    }

    @Override
    protected String entryId() {
        return "nolite_ignem";
    }
}
