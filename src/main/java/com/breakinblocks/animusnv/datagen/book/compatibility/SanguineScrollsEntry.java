package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class SanguineScrollsEntry extends EntryProvider {

    public SanguineScrollsEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sanguine Scrolls");
        this.pageText("[#](8B0000)Sanguine Scrolls[#]() are reusable spell scrolls that consume "
                + "Essentia Vitae instead of being destroyed. Unlike regular scrolls, "
                + "these blood-infused parchments can cast spells dozens or hundreds of "
                + "times, limited only by their durability.\\\n\\\n"
                + "[#](4A0080)A single spell, bound in blood. The scroll remembers the "
                + "incantation and repeats it at the cost of your life force.[#]()");

        this.page("creation", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Creating Scrolls");
        this.pageText("To create a Sanguine Scroll:\n\n"
                + "- Obtain an [#](8B0000)Iron's Spells Scroll[#]() with the spell you want\n\n"
                + "- Place a [#](8B0000)NeoVitae Slate[#]() in your offhand\n\n"
                + "- Right-click an [#](8B0000)Ara Vitae[#]() with the scroll\n\n"
                + "- The altar consumes EV and both items\n\n"
                + "- Receive a Sanguine Scroll with the spell bound\\\n\\\n"
                + "[#](2E8B57)The slate tier determines scroll durability.[#]()");

        this.page("tiers", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Slate Tiers");
        this.pageText("Different slate types produce scrolls with varying durability:\n\n"
                + "- [#](B8860B)Tabula Rasa:[#]() 50 uses\n\n"
                + "- [#](B8860B)Tabula Robur:[#]() 100 uses\n\n"
                + "- [#](B8860B)Tabula Animata:[#]() 200 uses\n\n"
                + "- [#](B8860B)Tabula Spiritus:[#]() 400 uses\n\n"
                + "- [#](B8860B)Tabula Aetherea:[#]() 600 uses\\\n\\\n"
                + "Choose based on how frequently you intend to use the spell.");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("EV Costs");
        this.pageText("Sanguine Scrolls consume more EV than normal spell casting:\\\n\\\n"
                + "Base Cost: Mana cost x EV rate x 1.5 (default: 150 EV per mana point)\\\n\\\n"
                + "Creation cost scales with spell rarity and level:\n\n"
                + "- Common: 5,000 EV x level\n\n"
                + "- Uncommon: 10,000 EV x level\n\n"
                + "- Rare: 25,000 EV x level\n\n"
                + "- Epic: 50,000 EV x level\n\n"
                + "- Legendary: 100,000 EV x level");

        this.page("strategy", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Strategic Uses");
        this.pageText("Sanguine Scrolls excel for:\n\n"
                + "- [#](8B0000)Emergency spells[#](): quick access without spellbooks\n\n"
                + "- [#](8B0000)Powerful spells[#](): cast legendary spells anytime\n\n"
                + "- [#](8B0000)EV-heavy builds[#](): convert excess EV to magic\n\n"
                + "- [#](8B0000)Backup casting[#](): when out of mana\\\n\\\n"
                + "The 50%% EV premium is worth it for convenience and reliability. "
                + "Scrolls work from any inventory slot.\\\n\\\n"
                + "[#](2E8B57)Requires Iron's Spellbooks to be installed. Scrolls respect "
                + "spell cooldowns.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sanguine Scrolls";
    }

    @Override
    protected String entryDescription() {
        return "Reusable spell scrolls powered by Essentia Vitae. Requires Iron's Spells.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.PAPER);
    }

    @Override
    protected String entryId() {
        return "sanguine_scrolls";
    }
}
