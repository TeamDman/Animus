package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class UnmakingEntry extends EntryProvider {

    public UnmakingEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Unmaking");
        this.pageText("Break the bonds of magic. The [#](4A0080)Ritual of Unmaking[#]() extracts enchantments from items and places them onto books, allowing you to repurpose or duplicate enchantments at will."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Details");
        this.pageText("[#](B8860B)Activation:[#]() 3,000 EV"
                + "\\\n[#](B8860B)Refresh:[#]() 0 EV (one-time use)"
                + "\\\n[#](B8860B)Refresh Time:[#]() 20 ticks"
                + "\\\n\\\n[#](B8860B)Effect Range:[#]() 5x5x5 blocks around the ritual"
                + "\\\n\\\nThrow books and enchanted items near the ritual. It deactivates automatically after extracting enchantments.");

        this.page("mechanics", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Mechanics");
        this.pageText("For regular enchanted items, the ritual extracts all enchantments to separate enchanted books. The item is left unenchanted."
                + "\\\n\\\nFor enchanted books with multiple enchantments, each enchantment is split and duplicated. You receive two copies of each at a reduced level (level -1, minimum 1)."
                + "\\\n\\\nItems enhanced by the [#](4A0080)Imperfect Ritual of Enhancement[#]() are ignored by default (configurable)."
                + "\\\n\\\n[#](2E8B57)An invaluable tool for recycling enchantments from unwanted loot or duplicating rare enchantments from books.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Unmaking";
    }

    @Override
    protected String entryDescription() {
        return "Extracts and duplicates enchantments from items onto books.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.ENCHANTED_BOOK);
    }

    @Override
    protected String entryId() {
        return "unmaking";
    }
}
