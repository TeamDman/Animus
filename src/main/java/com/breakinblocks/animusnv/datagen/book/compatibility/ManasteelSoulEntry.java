package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class ManasteelSoulEntry extends EntryProvider {

    public ManasteelSoulEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Imperfect Ritual: Manasteel Soul");
        this.pageText("An imperfect ritual that infuses your soul with the essence of "
                + "Botania's mana, granting supernatural swiftness and the ability to "
                + "phase through matter.\\\n\\\n"
                + "[#](8B0000)Setup:[#]() Place a Manasteel Block atop an Imperfect Ritual "
                + "Stone.\\\n\\\n"
                + "[#](4A0080)Cost:[#]() 2,500 EV per use\\\n\\\n"
                + "[#](B8860B)Duration:[#]() 15 minutes");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("Right-click the Imperfect Ritual Stone to receive:\n\n"
                + "- [#](8B0000)Emptiness[#]() for 15 minutes\n\n"
                + "- [#](8B0000)Speed[#]() for 15 minutes\n\n"
                + "- Both effects have no visible particles\\\n\\\n"
                + "Botania's Emptiness effect allows you to phase through certain blocks "
                + "and reduces aggro from mobs.\\\n\\\n"
                + "[#](2E8B57)Ideal for exploration, traversal, escaping dangerous situations, "
                + "and stealth gameplay.[#]()\\\n\\\n"
                + "[#](4A0080)One with the flow of mana.[#]()");
    }

    @Override
    protected String entryName() {
        return "Imperfect Ritual: Manasteel Soul";
    }

    @Override
    protected String entryDescription() {
        return "Grants phasing and speed through mana-infused Vitaemancy. Requires Botania.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.IRON_INGOT);
    }

    @Override
    protected String entryId() {
        return "manasteel_soul";
    }
}
