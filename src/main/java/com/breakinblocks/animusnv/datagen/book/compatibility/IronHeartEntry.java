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

public class IronHeartEntry extends EntryProvider {

    public IronHeartEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Imperfect Ritual: Iron Heart");
        this.pageText("An imperfect ritual that channels the arcane power of Iron's Spellbooks "
                + "through Vitaemancy, granting devastating echoing strikes in combat.\\\n\\\n"
                + "[#](8B0000)Setup:[#]() Place an Arcane Anvil atop an Imperfect Ritual Stone.\\\n\\\n"
                + "[#](4A0080)Cost:[#]() 3,500 EV per use\\\n\\\n"
                + "[#](B8860B)Duration:[#]() 15 minutes");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("Right-click the Imperfect Ritual Stone to receive:\n\n"
                + "- [#](8B0000)Echoing Strikes III[#]() for 15 minutes\n\n"
                + "- No visible particles\\\n\\\n"
                + "Iron's Spellbooks' Echoing Strikes effect causes your melee attacks "
                + "to echo, dealing additional damage to targets.\\\n\\\n"
                + "[#](2E8B57)Ideal for extended combat encounters, boss fights, and "
                + "melee-focused builds.[#]()\\\n\\\n"
                + "[#](4A0080)Blood and steel become one.[#]()");
    }

    @Override
    protected String entryName() {
        return "Imperfect Ritual: Iron Heart";
    }

    @Override
    protected String entryDescription() {
        return "Grants echoing melee strikes through blood-forged arcane power. Requires Iron's Spells.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.IRON_BLOCK);
    }

    @Override
    protected String entryId() {
        return "iron_heart";
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return super.additionalSetup(entry)
                .withCondition(BookModLoadedConditionModel.create().withModId("irons_spellbooks"));
    }
}
