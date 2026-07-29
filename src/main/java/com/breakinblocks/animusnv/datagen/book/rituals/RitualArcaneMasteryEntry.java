package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class RitualArcaneMasteryEntry extends EntryProvider {

    public RitualArcaneMasteryEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Arcane Mastery");
        this.pageText("The [#](8B0000)Ritual of Arcane Mastery[#]() bridges the ritual system "
                + "with Iron's Spells learning mechanics. This ritual can teach you new "
                + "spells or upgrade existing ones by consuming scrolls placed in nearby "
                + "chests.\\\n\\\n"
                + "[#](4A0080)Place spell scrolls in chests within 5 blocks of the Master "
                + "Ritual Stone. The ritual automatically processes scrolls, teaching you "
                + "new spells or upgrading ones you already know.[#]()");

        this.page("mechanics", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("How It Works");
        this.pageText("The ritual searches for [#](8B0000)Iron's Spells Scrolls[#]() in chests "
                + "within a 5-block radius (horizontal and vertical).\\\n\\\n"
                + "If you do not know the spell: consumes the scroll, teaches you the "
                + "spell at level 1, costs EV based on spell rarity.\\\n\\\n"
                + "If you already know the spell: consumes the scroll, upgrades spell "
                + "level by +1, EV cost = base cost x current level.");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("EV Costs");
        this.pageText("EV costs scale with spell rarity:\n\n"
                + "- [#](8B0000)Common:[#]() 5,000 EV\n\n"
                + "- [#](8B0000)Uncommon:[#]() 10,000 EV\n\n"
                + "- [#](8B0000)Rare:[#]() 25,000 EV\n\n"
                + "- [#](8B0000)Epic:[#]() 50,000 EV\n\n"
                + "- [#](8B0000)Legendary:[#]() 100,000 EV\\\n\\\n"
                + "Upgrade cost formula: Base cost x current spell level.\\\n\\\n"
                + "[#](2E8B57)Example: Upgrading a Rare spell from level 2 to 3 costs "
                + "25,000 x 2 = 50,000 EV.[#]()");

        this.page("setup", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Setup & Pattern");
        this.pageText("Build the ritual pattern, activate with [#](8B0000)10,000 EV[#](), "
                + "then place chests within 5 blocks and fill them with spell scrolls.\\\n\\\n"
                + "The ritual checks every 2 seconds, processes one scroll at a time, "
                + "emits smoke if no scrolls found, and enchantment particles on success.\\\n\\\n"
                + "Rune pattern: 8 Dusk Runes and 8 Air Runes arranged in inner, "
                + "middle, and outer rings around the Master Ritual Stone.");

        this.page("tips", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Tips");
        this.pageText("Group common spells together and keep high-value scrolls separate. "
                + "Monitor your EV levels and use activation crystals to start and stop.\\\n\\\n"
                + "For bulk processing, fill multiple chests with scrolls. The ritual "
                + "processes continuously. Check back periodically.\\\n\\\n"
                + "[#](2E8B57)The ritual respects spell max levels. You cannot upgrade "
                + "beyond a spell's natural maximum. Requires Iron's Spellbooks to be "
                + "installed.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Arcane Mastery";
    }

    @Override
    protected String entryDescription() {
        return "Learn and upgrade spells by consuming scrolls through ritual power. Requires Iron's Spells.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.ENCHANTED_BOOK);
    }

    @Override
    protected String entryId() {
        return "ritual_arcane_mastery";
    }
}
