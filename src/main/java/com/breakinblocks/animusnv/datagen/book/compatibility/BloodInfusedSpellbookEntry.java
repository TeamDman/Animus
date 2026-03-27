package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class BloodInfusedSpellbookEntry extends EntryProvider {

    public BloodInfusedSpellbookEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Blood-Infused Spellbook");
        this.pageText("The [#](8B0000)Blood-Infused Spellbook[#]() combines the power of Iron's "
                + "Spells with the sacrificial arts. This hybrid tome can be progressively "
                + "enhanced through [#](B8860B)six tiers[#]() of blood infusion, each "
                + "granting powerful new abilities.\\\n\\\n"
                + "[#](4A0080)Unlike common spellbooks, this tome grows stronger as you feed it "
                + "Essentia Vitae at the Ara Vitae. It hungers, and it remembers.[#]()");

        this.page("creation", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Initial Infusion");
        this.pageText("To create a Blood-Infused Spellbook:\n\n"
                + "- Obtain any [#](8B0000)Spellbook[#]() from Iron's Spells\n\n"
                + "- Place the spellbook in the [#](8B0000)Ara Vitae[#]()\n\n"
                + "- Ensure the altar has [#](8B0000)5,000 EV[#]() stored\n\n"
                + "- Wait for the altar to complete the infusion\\\n\\\n"
                + "The spellbook transforms into a [#](B8860B)Tier 1[#]() Blood-Infused Spellbook. "
                + "[#](2E8B57)All your equipped spells are preserved during the transformation.[#]()");

        this.page("tiers_low", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Tiers 1-3");
        this.pageText("Each tier grants [#](8B0000)+50 Max Mana[#]() and additional spell slots:\\\n\\\n"
                + "[#](B8860B)Tier 1:[#]() Weak Orb, 5k EV. 6 slots, +50 mana\\\n\\\n"
                + "[#](B8860B)Tier 2:[#]() Apprentice Orb, 10k EV. 7 slots, +100 mana\\\n\\\n"
                + "[#](B8860B)Tier 3:[#]() Magician Orb, 25k EV. 8 slots, +150 mana");

        this.page("tiers_high", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Tiers 4-6");
        this.pageText("Advanced tiers add EV cost reduction and lifesteal:\\\n\\\n"
                + "[#](B8860B)Tier 4:[#]() Master Orb, 50k EV. 10 slots, +200 mana, "
                + "-10%% EV cost\\\n\\\n"
                + "[#](B8860B)Tier 5:[#]() Archmage Orb, 100k EV. 11 slots, +250 mana, "
                + "-20%% EV cost\\\n\\\n"
                + "[#](B8860B)Tier 6:[#]() Transcendent Orb, 175k EV. 12 slots, +300 mana, "
                + "+5%% lifesteal");

        this.page("upgrading", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Upgrading");
        this.pageText("To upgrade your spellbook to the next tier:\n\n"
                + "- Place the required Blood Orb tier (or higher) in the altar\n\n"
                + "- Ensure the altar has enough EV\n\n"
                + "- Right-click the altar while holding your Blood-Infused Spellbook\\\n\\\n"
                + "The spellbook's tooltip shows your current tier, bonuses, and the "
                + "requirements for the next upgrade.\\\n\\\n"
                + "[#](2E8B57)Requires Iron's Spellbooks to be installed. The spellbook's "
                + "rarity and enchantment glint change with each tier.[#]()");
    }

    @Override
    protected String entryName() {
        return "Blood-Infused Spellbook";
    }

    @Override
    protected String entryDescription() {
        return "A spellbook enhanced through six tiers of blood infusion. Requires Iron's Spells.";
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
        return "blood_infused_spellbook";
    }
}
