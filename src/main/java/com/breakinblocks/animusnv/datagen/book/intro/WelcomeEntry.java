package com.breakinblocks.animusnv.datagen.book.intro;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import com.breakinblocks.animusnv.registry.AnimusItems;

public class WelcomeEntry extends EntryProvider {

    public WelcomeEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Codex Animus");
        this.pageText("Welcome, practitioner of the sanguine arts.\\\n\\\n"
                + "You hold the [#](4A0080)Codex Animus[#](), a companion volume to the "
                + "[#](8B0000)Scriptura Vitae[#](). Where the Scriptura teaches the foundations "
                + "of Vitaemancy, this codex delves into advanced techniques, experimental "
                + "workings, and the integration of foreign magical disciplines into the "
                + "blood mage's repertoire.\\\n\\\n"
                + "Within these pages you will find darker applications of [#](4A0080)Anima[#](), "
                + "rituals to summon and destroy, sigils to command the elements, and secrets "
                + "of creating life from blood.");

        this.page("prerequisites", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Prerequisites");
        this.pageText("This tome assumes you have mastered the basics of Vitaemancy. "
                + "You should be familiar with:\n\n"
                + "- The [#](8B0000)Ara Vitae[#]() and Essentia Vitae\n\n"
                + "- [#](8B0000)Orbs of Vitae[#]() and Anima binding\n\n"
                + "- Basic [#](8B0000)Rituals[#]() and Ritual Stones\n\n"
                + "- [#](4A0080)Spiritus[#]() and the Spiritus system\\\n\\\n"
                + "If you are new to Vitaemancy, consult the [#](8B0000)Scriptura Vitae[#]() first. "
                + "The knowledge within this codex builds upon that foundation.");

        this.page("organization", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Organization");
        this.pageText("The Codex Animus is organized into chapters:\n\n"
                + "- [#](8B0000)Blocks[#](): Unique materials, fluids, and flora\n\n"
                + "- [#](8B0000)Items[#](): Weapons, tools, and implements\n\n"
                + "- [#](8B0000)Sigils[#](): Portable instruments of Vitaemancy\n\n"
                + "- [#](8B0000)Rituals[#](): Ceremonial workings of power\n\n"
                + "- [#](8B0000)Imperfect Rituals[#](): Simpler rites for a single stone\n\n"
                + "- [#](8B0000)Compatibility[#](): Bridges to foreign magics");
    }

    @Override
    protected String entryName() {
        return "Welcome";
    }

    @Override
    protected String entryDescription() {
        return "The opening pages of the Codex Animus, a companion to the Scriptura Vitae.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_TRANSPOSITION.get());
    }

    @Override
    protected String entryId() {
        return "welcome";
    }
}
