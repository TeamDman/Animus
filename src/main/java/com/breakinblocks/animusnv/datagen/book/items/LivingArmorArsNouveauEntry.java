package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class LivingArmorArsNouveauEntry extends EntryProvider {

    public LivingArmorArsNouveauEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Living Armor: Source Attunement");
        this.pageText("Living Armor adapts to your magical practices. When you cast Ars Nouveau "
                + "glyph-based magic, your armor learns and evolves, unlocking the "
                + "[#](8B0000)Source Attunement[#]() upgrade path designed for source-wielding "
                + "mages.\\\n\\\n"
                + "[#](4A0080)Cast Ars Nouveau spells while wearing Living Armor to gain "
                + "experience. The armor attunes to the flow of Source energy, growing "
                + "stronger with each glyph you cast.[#]()");

        this.page("xp", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("XP from Glyphs");
        this.pageText("Every Ars Nouveau spell you cast grants XP to your Living Armor "
                + "based on its complexity:\\\n\\\n"
                + "Base XP (5) x Number of Glyphs\\\n\\\n"
                + "[#](2E8B57)Example: A spell with 6 glyphs grants 5 x 6 = 30 XP[#]()\\\n\\\n"
                + "More complex spells with many glyphs level your armor faster. "
                + "Experiment with elaborate spell combinations to maximize progression.");

        this.page("levels_low", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Source Attunement Levels");
        this.pageText("The Source Attunement upgrade tree provides five levels:\\\n\\\n"
                + "[#](B8860B)Level 1:[#]() +5%% Spell Damage (5 points)\\\n\\\n"
                + "[#](B8860B)Level 2:[#]() +10%% Spell Damage (10 points)\\\n\\\n"
                + "[#](B8860B)Level 3:[#]() +15%% Spell Damage (15 points)\\\n\\\n"
                + "[#](B8860B)Level 4:[#]() +20%% Spell Damage, Mana Regen buff for 4s "
                + "(20 points)\\\n\\\n"
                + "[#](B8860B)Level 5:[#]() +25%% Spell Damage, -20%% Mana Cost, "
                + "Spell Damage III buff for 5s (25 points)");

        this.page("details", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Level Details");
        this.pageText("Levels 1-3 each add +5%% spell damage. By level 3, you deal "
                + "15%% more damage with all Ars Nouveau spells.\\\n\\\n"
                + "Level 4 grants [#](8B0000)Mana Regeneration[#]() for 4 seconds on every "
                + "spell cast, helping you recover mana faster.\\\n\\\n"
                + "Level 5 is peak performance: +25%% spell damage, "
                + "[#](8B0000)Spell Damage III[#]() for 5 seconds on cast, and 20%% mana "
                + "cost refund.\\\n\\\n"
                + "[#](B8860B)Total:[#]() 75 points for all levels.");

        this.page("synergies", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Synergies & Tips");
        this.pageText("Stack Spell Damage III with Ars Nouveau's native damage buffs. "
                + "Level 4 Mana Regen helps with sustained casting, and Level 5's "
                + "20%% mana refund makes expensive spells more viable.\\\n\\\n"
                + "To maximize XP gain:\n\n"
                + "- Create longer, more complex spells\n\n"
                + "- Cast frequently while exploring\n\n"
                + "- Each glyph counts toward XP\\\n\\\n"
                + "[#](2E8B57)XP is only granted while wearing Living Armor. "
                + "Requires Ars Nouveau to be installed.[#]()");
    }

    @Override
    protected String entryName() {
        return "Living Armor: Source Attunement";
    }

    @Override
    protected String entryDescription() {
        return "Living Armor evolves through Ars Nouveau spellcasting. Requires Ars Nouveau.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.DIAMOND_CHESTPLATE);
    }

    @Override
    protected String entryId() {
        return "living_armor_ars_nouveau";
    }
}
