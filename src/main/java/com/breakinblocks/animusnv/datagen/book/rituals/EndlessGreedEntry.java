package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class EndlessGreedEntry extends EntryProvider {

    public EndlessGreedEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Endless Greed");
        this.pageText("The [#](4A0080)Ritual of Endless Greed[#]() is the perfect companion for any mob farm. It intercepts drops from slain creatures and transfers them directly into a container, keeping your farm clean and efficient."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("container", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Container Setup");
        this.pageText("Place a container (chest, barrel, hopper, or any block with item handler capability) directly on top of the [#](8B0000)Master Ritual Stone[#](), exactly one block above."
                + "\\\n\\\nModded storage blocks work as well. [#](2E8B57)Connect the container to a larger storage system via hopper or pipe for seamless automation.[#]()");

        this.page("items", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Item Collection");
        this.pageText("When a mob dies within range, its drops are immediately transferred to the container instead of spawning in the world."
                + "\n\n- Container present: items inserted"
                + "\n\n- Container full: overflow is destroyed"
                + "\n\n- No container: all items are destroyed"
                + "\\\n\\\nItems already on the ground within range are also collected periodically.");

        this.page("xp", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("XP Collection");
        this.pageText("Experience orbs within range are automatically collected every second."
                + "\\\n\\\nIf a [#](8B0000)Tome of Peritia[#]() is present in the container, all collected XP is stored in the first tome found. If no tome exists, XP orbs are simply removed to prevent lag.");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Range");
        this.pageText("[#](B8860B)Activation:[#]() 5,000 EV"
                + "\\\n[#](B8860B)Upkeep:[#]() 5 EV per second"
                + "\\\n[#](B8860B)Per Item:[#]() 1 EV per item collected"
                + "\\\n\\\n[#](B8860B)Range:[#]() 15 blocks horizontal"
                + "\\\n[#](B8860B)Height:[#]() 5 blocks above the ritual"
                + "\\\n\\\nAll values are configurable.");
    }

    @Override
    protected String entryName() {
        return "Ritual of Endless Greed";
    }

    @Override
    protected String entryDescription() {
        return "Intercepts mob drops and XP, funneling them into a container.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.HOPPER);
    }

    @Override
    protected String entryId() {
        return "endless_greed";
    }
}
