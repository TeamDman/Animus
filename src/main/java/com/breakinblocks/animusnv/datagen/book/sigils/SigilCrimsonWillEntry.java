package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookTabulaVitaeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;

public class SigilCrimsonWillEntry extends EntryProvider {

    public SigilCrimsonWillEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookTabulaVitaeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "alchemytable/reagentfist")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "array/sigil_crimson_will")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Crimson Will");
        this.pageText("Crafted in an [#](8B0000)Alchemy Array[#]() by combining a "
                + "[#](8B0000)Reagent: Crimson Will[#]() with a [#](B8860B)Tabula Robur[#](), this "
                + "sigil taps into the raw power of [#](4A0080)Crimson Will[#](). Its potency is tied "
                + "to the depth of your connection to Vitaemancy.");

        this.page("effect", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effect");
        this.pageText("When active, the sigil amplifies certain aspects of [#](4A0080)Vitaemancy[#](). "
                + "It can empower rituals, enhance sacrificial effects, and directly manipulate "
                + "blood energies.\\\n\\\n"
                + "[#](2E8B57)This sigil requires Iron's Spells 'n Spellbooks to be installed. "
                + "Its full capabilities reveal themselves through experimentation and advanced "
                + "Vitaemancy studies.[#]()");

        this.page("cost", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("EV Cost");
        this.pageText("The [#](4A0080)Essentia Vitae[#]() cost varies based on the effect being "
                + "channelled. Greater workings demand greater sacrifice.\\\n\\\n"
                + "[#](4A0080)The crimson tide answers to those bold enough to call upon it.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of Crimson Will";
    }

    @Override
    protected String entryDescription() {
        return "Channels raw crimson will to amplify Vitaemancy. Requires Iron's Spells.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Identifier.fromNamespaceAndPath("animusnv", "sigil_crimson_will"));
    }

    @Override
    protected String entryId() {
        return "sigil_crimson_will";
    }
}
