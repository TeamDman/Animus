package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.breakinblocks.animusnv.datagen.book.page.BookAraVitaeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;

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
        this.pageText("Firearms from Iron's Arms 'n Artifice accept two modifiers that only a "
                + "Vitaemancer can make. Open a gun's modifier slots ([#](B8860B)G[#]() by default) "
                + "and fit them like any other modifier.\\\n\\\n"
                + "[#](8B0000)Blood Bullet[#]() feeds the gun from your [#](8B0000)Anima[#](). "
                + "[#](4A0080)Spirit Powder[#]() binds the souls of what you shoot.\\\n\\\n"
                + "[#](2E8B57)Both can share one gun.[#]()");

        this.page("blood_bullet", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Blood Bullet");
        this.pageText("A round of congealed life essence that never runs dry. Each shot draws "
                + "[#](B8860B)50 EV[#]() from your network in place of a bullet, so the gun fires "
                + "even with an empty magazine.\\\n\\\n"
                + "When your network cannot pay, the gun falls back on whatever bullets are loaded.\\\n\\\n"
                + "[#](2E8B57)The EV cost per shot is configurable.[#]()");

        this.page("blood_bullet_recipe", () -> BookAraVitaeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "ara_vitae/blood_bullet_modifier")));

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
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "hellfire_forge/spirit_powder_modifier")));
    }

    @Override
    protected String entryName() {
        return "Sanguine Gunsmithing";
    }

    @Override
    protected String entryDescription() {
        return "Blood Bullet and Spirit Powder gun modifiers. Requires Iron's Arms 'n Artifice.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Identifier.fromNamespaceAndPath("animusnv", "textures/item/blood_bullet_modifier.png"));
    }

    @Override
    protected String entryId() {
        return "artifice_modifiers";
    }
}
