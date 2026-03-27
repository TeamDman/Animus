package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class NaturesLeachEntry extends EntryProvider {

    public NaturesLeachEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Nature's Leach");
        this.pageText("You drain the life force from the green world itself. The [#](4A0080)Ritual of Nature's Leach[#]() scans for plant matter within its range and consumes it, converting organic energy into [#](4A0080)Essentia Vitae[#]() for your [#](8B0000)Ara Vitae[#]()."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Details");
        this.pageText("[#](B8860B)Activation:[#]() 3,000 EV"
                + "\\\n[#](B8860B)Refresh:[#]() 10 EV"
                + "\\\n[#](B8860B)Refresh Time:[#]() 80 ticks (configurable); faster with less [#](4A0080)Corrosive Spiritus[#]()"
                + "\\\n\\\n[#](B8860B)Effect Range:[#]() 32 blocks radius (configurable)"
                + "\\\n[#](B8860B)Altar Range:[#]() 32 blocks horizontal, 10 blocks vertical"
                + "\\\n\\\nConsumes 1-3 plants per cycle, generating 50 EV (configurable) per plant consumed. The altar location is cached for performance.");

        this.page("will", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Spiritus Generation");
        this.pageText("This ritual generates [#](4A0080)Corrosive Spiritus[#]() as it consumes plants."
                + "\n\n- 0.5 to 1.5 [#](4A0080)Spiritus[#]() per plant consumed"
                + "\n\n- Maximum of 100 [#](4A0080)Spiritus[#]() in the area"
                + "\\\n\\\nNote that more [#](4A0080)Corrosive Spiritus[#]() in the area slows the ritual. Clear the [#](4A0080)Spiritus[#]() periodically to maintain peak efficiency."
                + "\\\n\\\n[#](2E8B57)Pair with an automated tree farm for a reliable, passive source of EV.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Nature's Leach";
    }

    @Override
    protected String entryDescription() {
        return "Drains the life from plants to fill your Ara Vitae.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.OAK_SAPLING);
    }

    @Override
    protected String entryId() {
        return "natures_leach";
    }
}
