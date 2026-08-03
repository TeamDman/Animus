package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.condition.BookModLoadedConditionModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class MagiEntry extends EntryProvider {

    public MagiEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Imperfect Ritual: Magi");
        this.pageText("An imperfect ritual that taps into the arcane energies of Ars Nouveau's "
                + "source gems, greatly enhancing your mana regeneration capabilities.\\\n\\\n"
                + "[#](8B0000)Setup:[#]() Place a Source Gem Block atop an Imperfect Ritual "
                + "Stone.\\\n\\\n"
                + "[#](4A0080)Cost:[#]() 2,500 EV per use\\\n\\\n"
                + "[#](B8860B)Duration:[#]() 15 minutes");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("Right-click the Imperfect Ritual Stone to receive:\n\n"
                + "- [#](8B0000)Mana Regen[#]() for 15 minutes\n\n"
                + "- No visible particles\\\n\\\n"
                + "Ars Nouveau's Mana Regen effect significantly increases your mana "
                + "regeneration rate, allowing for more frequent spellcasting.\\\n\\\n"
                + "[#](2E8B57)Ideal for extended spellcasting sessions, mana-intensive "
                + "rituals, and combat preparation.[#]()\\\n\\\n"
                + "[#](4A0080)Blood fuels the arcane flow.[#]()");
    }

    @Override
    protected String entryName() {
        return "Imperfect Ritual: Magi";
    }

    @Override
    protected String entryDescription() {
        return "Enhances mana regeneration through blood-infused source gems. Requires Ars Nouveau.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.AMETHYST_SHARD);
    }

    @Override
    protected String entryId() {
        return "magi";
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return super.additionalSetup(entry)
                .withCondition(BookModLoadedConditionModel.create().withModId("ars_nouveau"));
    }
}
