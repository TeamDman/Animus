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

public class AnimalLuringEntry extends EntryProvider {

    public AnimalLuringEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(ResourceLocation.fromNamespaceAndPath("neovitae", "ritual/ritual_animal_luring"))
                .withMultiblockName("Ritual of Animal Luring")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Built from standard runes; any Ritual Diviner will serve.[#]()");

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Animal Luring");
        this.pageText("You call forth peaceful creatures from the ether. The [#](4A0080)Ritual of Animal Luring[#]() spawns passive mobs in the area around the [#](8B0000)Master Ritual Stone[#](), useful for populating farms or seeding the world with life."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Details");
        this.pageText("[#](B8860B)Activation:[#]() 5,000 EV"
                + "\\\n[#](B8860B)Refresh:[#]() 1,000 EV (configurable)"
                + "\\\n[#](B8860B)Refresh Time:[#]() 400 ticks (20 seconds)"
                + "\\\n\\\nSpawns peaceful mobs randomly within a 4-8 block radius of the ritual stone."
                + "\\\n\\\n[#](B8860B)Mob Types:[#]() All peaceful creatures: Creature, Ambient, Water Creature, and Water Ambient categories."
                + "\\\n\\\n[#](2E8B57)Useful for repopulating areas after a catastrophe or seeding a new animal farm without hunting.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Animal Luring";
    }

    @Override
    protected String entryDescription() {
        return "Spawns passive mobs around the ritual stone.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.WHEAT);
    }

    @Override
    protected String entryId() {
        return "animal_luring";
    }
}
