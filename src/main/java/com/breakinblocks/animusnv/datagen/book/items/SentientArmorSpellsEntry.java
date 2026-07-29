package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class SentientArmorSpellsEntry extends EntryProvider {

    public SentientArmorSpellsEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sentient Armor: Arcane Channeling");
        this.pageText("Sentient Armor evolves with you, and now it can grow stronger through "
                + "spellcasting. Every spell you cast grants experience to your Sentient "
                + "Armor, unlocking the [#](8B0000)Arcane Channeling[#]() upgrade path for "
                + "Iron's Spellbooks practitioners.\\\n\\\n"
                + "[#](4A0080)Wear Sentient Armor while casting spells. The armor learns from "
                + "your magical prowess and adapts accordingly.[#]()");

        this.page("xp", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("XP from Spellcasting");
        this.pageText("Every spell you cast grants XP to your Sentient Armor:\\\n\\\n"
                + "Base XP (10) x Spell Level x Rarity Multiplier\\\n\\\n"
                + "Rarity Multipliers:\n\n"
                + "- Common: x1.0\n\n"
                + "- Uncommon: x1.5\n\n"
                + "- Rare: x2.0\n\n"
                + "- Epic: x3.0\n\n"
                + "- Legendary: x5.0\\\n\\\n"
                + "[#](2E8B57)Example: A level 3 Rare spell grants 10 x 3 x 2.0 = 60 XP[#]()");

        this.page("levels", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Arcane Channeling Levels");
        this.pageText("The Arcane Channeling upgrade tree provides five levels:\\\n\\\n"
                + "[#](B8860B)Level 1:[#]() -5%% Mana Cost (5 points)\\\n\\\n"
                + "[#](B8860B)Level 2:[#]() -10%% Mana Cost (10 points)\\\n\\\n"
                + "[#](B8860B)Level 3:[#]() -5%% Cooldown (15 points)\\\n\\\n"
                + "[#](B8860B)Level 4:[#]() -10%% Cooldown (20 points)\\\n\\\n"
                + "[#](B8860B)Level 5:[#]() Resistance on Cast (25 points)\\\n\\\n"
                + "[#](B8860B)Total:[#]() 75 points for all levels.");

        this.page("details", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Level Details");
        this.pageText("Levels 1-2 reduce mana cost of spells when cast. At level 2, "
                + "you save 10%% mana on every spell, stacking with other mana "
                + "reduction effects.\\\n\\\n"
                + "Levels 3-4 apply cooldown reduction. At level 4, spells recharge "
                + "10%% faster, letting you cast more often.\\\n\\\n"
                + "Level 5 grants [#](8B0000)Resistance I[#]() for 2 seconds every time "
                + "you begin casting, providing a defensive boost while channeling.");

        this.page("synergies", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Synergies & Tips");
        this.pageText("Arcane Channeling works well with:\n\n"
                + "- [#](8B0000)Blood-Infused Spellbook[#](): stack mana cost reductions\n\n"
                + "- [#](8B0000)Sigil of Crimson Will[#](): damage boost plus mana efficiency\n\n"
                + "- [#](8B0000)Sanguine Scrolls[#](): cast from scrolls while gaining XP\\\n\\\n"
                + "To maximize XP: cast higher-level spells, seek rare and legendary "
                + "spells, and cast frequently while exploring.\\\n\\\n"
                + "[#](2E8B57)The armor gains XP from any spell, including those cast with EV. "
                + "Requires Iron's Spellbooks to be installed.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sentient Armor: Arcane Channeling";
    }

    @Override
    protected String entryDescription() {
        return "Sentient Armor evolves through Iron's Spells casting. Requires Iron's Spells.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.DIAMOND_CHESTPLATE);
    }

    @Override
    protected String entryId() {
        return "living_armor_spells";
    }
}
