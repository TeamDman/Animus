package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class SerenityEntry extends EntryProvider {

    public SerenityEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Serenity");
        this.pageText("The [#](4A0080)Ritual of Serenity[#]() creates a zone of peace where hostile creatures cannot spawn. Perfect for protecting your base without relying on torches or other lighting."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner [Dusk][#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs");
        this.pageText("[#](B8860B)Activation:[#]() 10,000 EV"
                + "\\\n[#](B8860B)Upkeep:[#]() 50 EV per tick (configurable)"
                + "\\\n[#](B8860B)Range:[#]() 48 blocks (configurable)"
                + "\\\n\\\nHostile mob spawning is completely prevented within the ritual's spherical range.");

        this.page("notes", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Notes");
        this.pageText("The ritual does not remove mobs that are already present. It only prevents new spawns from occurring."
                + "\\\n\\\nMultiple rituals can overlap to cover larger areas."
                + "\\\n\\\n[#](2E8B57)An elegant alternative to flooding your base with light sources. Let the ambient darkness remain for atmosphere while blood keeps you safe.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Serenity";
    }

    @Override
    protected String entryDescription() {
        return "Creates a zone that prevents hostile mob spawning.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.WHITE_BED);
    }

    @Override
    protected String entryId() {
        return "serenity";
    }
}
