package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;

public class SigilRemediumEntry extends EntryProvider {

    public SigilRemediumEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "hellfire_forge/reagentremendium")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "array/sigil_remedium")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Remedium");
        this.pageText("A toggleable sigil that continuously cleanses your body of harmful afflictions. "
                + "While active, negative status effects are purged at a steady cost of "
                + "[#](4A0080)Essentia Vitae[#]().");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Right-click to toggle the sigil on or off. While active, all harmful effects "
                + "are automatically removed once per second, at a cost of [#](4A0080)50 EV[#]() per "
                + "effect removed.\\\n\\\n"
                + "The sigil deactivates if your reserves are exhausted.");

        this.page("notes", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Notes");
        this.pageText("Only harmful effects are removed. Beneficial effects such as Regeneration or "
                + "Strength remain untouched.\\\n\\\n"
                + "The cost scales with the number of afflictions. Cleansing 3 effects simultaneously "
                + "costs 150 EV.\\\n\\\n"
                + "[#](4A0080)Purity through sacrifice.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of Remedium";
    }

    @Override
    protected String entryDescription() {
        return "Continuously purges harmful status effects while active.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_REMEDIUM.get());
    }

    @Override
    protected String entryId() {
        return "sigil_remedium";
    }
}
