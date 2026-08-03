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

public class SoulStainedBloodEntry extends EntryProvider {

    public SoulStainedBloodEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Imperfect Ritual: Soul-Stained Blood");
        this.pageText("An imperfect ritual that channels the protective essence of Malum's "
                + "hallowed gold, surrounding the bearer in an oaken carapace.\\\n\\\n"
                + "[#](8B0000)Setup:[#]() Place a Block of Hallowed Gold atop an Imperfect "
                + "Ritual Stone.\\\n\\\n"
                + "[#](4A0080)Cost:[#]() 3,000 EV per use\\\n\\\n"
                + "[#](B8860B)Duration:[#]() 15 minutes");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("Right-click the Imperfect Ritual Stone to receive:\n\n"
                + "- [#](8B0000)Stone Ward[#]() for 15 minutes\\\n\\\n"
                + "Malum's Stone Ward effect surrounds you in an oaken carapace, "
                + "increasing your defenses and providing significant protection "
                + "against damage.\\\n\\\n"
                + "[#](2E8B57)Ideal for defensive combat preparation, dangerous exploration, "
                + "and boss encounters.[#]()\\\n\\\n"
                + "[#](4A0080)Blood and spirit unite as one.[#]()");
    }

    @Override
    protected String entryName() {
        return "Imperfect Ritual: Soul-Stained Blood";
    }

    @Override
    protected String entryDescription() {
        return "Grants Stone Ward protection through hallowed gold Vitaemancy. Requires Malum.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.SCULK);
    }

    @Override
    protected String entryId() {
        return "soul_stained_blood";
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return super.additionalSetup(entry)
                .withCondition(BookModLoadedConditionModel.create().withModId("malum"));
    }
}
