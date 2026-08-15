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
                + "[#](8B0000)Reagent: Fist[#]() with a [#](B8860B)Tabula Animata[#](), this sigil "
                + "empowers your spellcasting with both Essentia Vitae and Spiritus. While active it "
                + "raises spell power and summon damage, scaling with the raw Spiritus stored in "
                + "your Anima.\\\n\\\n"
                + "[#](4A0080)Right-click to activate. Every spell you cast while it is active "
                + "consumes EV and briefly empowers you.[#]()");

        this.page("scaling", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Power Scaling");
        this.pageText("[#](8B0000)Base Bonus:[#]() +30%% spell power and summon damage\\\n\\\n"
                + "[#](4A0080)Spiritus Scaling:[#]()\n\n"
                + "- Scales up to +50%% total at 4096 Spiritus\n\n"
                + "- Uses Spiritus from your Anima\n\n"
                + "- Works with 0 Spiritus (base 30%% only)\\\n\\\n"
                + "The bonus is applied only during spell casting and removed "
                + "immediately after.");

        this.page("cost", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Cost Breakdown");
        this.pageText("Each spell cast with the sigil active consumes:\\\n\\\n"
                + "[#](8B0000)Essentia Vitae:[#]()\n\n"
                + "- Configurable per mana point (default: 50 EV)\n\n"
                + "- Scales with spell level\n\n"
                + "- Example: 10-level spell = 500 EV\\\n\\\n"
                + "[#](4A0080)Spiritus:[#]()\n\n"
                + "- 5 Spiritus per cast (if available)\n\n"
                + "- Consumes from Anima\n\n"
                + "- Optional. Sigil works without Spiritus");

        this.page("states", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Activation States");
        this.pageText("[#](8B0000)Active[#]() (glowing red icon):\n\n"
                + "- Consumes resources per spell\n\n"
                + "- Boosts spell power and summon damage\n\n"
                + "- Shows dynamic power bonus in tooltip\\\n\\\n"
                + "[#](4A0080)Inactive[#]() (gray icon):\n\n"
                + "- No resource consumption\n\n"
                + "- No power boost\n\n"
                + "- Right-click to toggle\\\n\\\n"
                + "The sigil must be bound to your [#](8B0000)Anima[#]() to activate. "
                + "Keep it in any inventory slot. Deactivate between fights to conserve EV.\\\n\\\n"
                + "[#](2E8B57)Requires Iron's Spells 'n Spellbooks to be installed.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of Crimson Will";
    }

    @Override
    protected String entryDescription() {
        return "Empowers spellcasting with Essentia Vitae and Spiritus. Requires Iron's Spells.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Identifier.fromNamespaceAndPath("animusnv", "textures/item/sigil_crimson_will.png"));
    }

    @Override
    protected String entryId() {
        return "sigil_crimson_will";
    }
}
