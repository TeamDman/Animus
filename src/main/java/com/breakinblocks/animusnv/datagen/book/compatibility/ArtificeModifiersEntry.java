package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.breakinblocks.animusnv.datagen.book.page.BookAraVitaeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.condition.BookModLoadedConditionModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class ArtificeModifiersEntry extends EntryProvider {

    public ArtificeModifiersEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sanguine Gunsmithing");
        this.pageText("Firearms from Iron's Arms 'n Artifice accept modifiers that only a "
                + "Vitaemancer can make. Open a gun's modifier slots ([#](B8860B)G[#]() by default) "
                + "and fit them like any other modifier.\\\n\\\n"
                + "[#](8B0000)Blood Bullet[#]() reloads the gun from your [#](8B0000)Anima[#](). "
                + "[#](4A0080)Spirit Powder[#]() binds the souls of what you shoot.\\\n\\\n"
                + "[#](2E8B57)They can all share one gun.[#]()");

        this.page("blood_bullet", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Blood Bullet");
        this.pageText("Congealed life essence pressed into the breech. The gun still fires from its "
                + "magazine, but reloading draws [#](B8860B)100 EV[#]() per round from your network "
                + "instead of taking bullets from your inventory.\\\n\\\n"
                + "If your network cannot pay for a single round, the gun reloads from your bullets as normal.\\\n\\\n"
                + "[#](2E8B57)The EV cost per round is configurable.[#]()");

        this.page("blood_bullet_recipe", () -> BookAraVitaeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "ara_vitae/blood_bullet_modifier")));

        this.page("spirit_powder", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Spirit Powder");
        this.pageText("Blackpowder cut with Spiritus. Every creature it strikes is caught in a "
                + "[#](4A0080)Soul Snare[#](), and monsters slain while snared drop Spiritus for you to gather.\\\n\\\n"
                + "Its shots also grow stronger with the Spiritus in your gems, from "
                + "[#](B8860B)+20%%[#]() damage at 16 Spiritus up to [#](B8860B)+80%%[#]() at 4,000, "
                + "measured by your strongest aspect.\\\n\\\n"
                + "[#](2E8B57)Forging it needs at least 300 Spiritus in the forge, so a Common Spiritus Gem or better.[#]()");

        this.page("spirit_powder_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/spirit_powder_modifier")));

        this.page("vengeance_bullet", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText())
                .withCondition(BookModLoadedConditionModel.create().withModId("evilcraft")));
        this.pageTitle("Vengeance Bullet");
        this.pageText("An Enchanted Bullet steeped in blood until it carries a grudge of its own. "
                + "Shots fitted with it strike [#](4A0080)Vengeance Spirits[#]() instead of passing "
                + "through them, and wound them with the shot's full damage.\\\n\\\n"
                + "Infuse an [#](B8860B)Enchanted Bullet[#]() modifier with [#](B8860B)10,000 mB[#]() "
                + "of Blood in a [#](8B0000)Blood Infuser[#]() upgraded with a Promise of Tenacity III.");
    }

    @Override
    protected String entryName() {
        return "Sanguine Gunsmithing";
    }

    @Override
    protected String entryDescription() {
        return "Blood-forged gun modifiers. Requires Iron's Arms 'n Artifice.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ResourceLocation.fromNamespaceAndPath("animusnv", "textures/item/blood_bullet_modifier.png"));
    }

    @Override
    protected String entryId() {
        return "artifice_modifiers";
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return super.additionalSetup(entry)
                .withCondition(BookModLoadedConditionModel.create().withModId("irons_artifice"));
    }
}
