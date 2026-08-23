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

public class SigilConsumptionEntry extends EntryProvider {

    public SigilConsumptionEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "hellfire_forge/reagentconsumption")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "array/sigil_consumption")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Consumption");
        this.pageText("Forged in an [#](8B0000)Alchemy Array[#]() from a [#](8B0000)Reagent: Consumption[#]() "
                + "and a [#](B8860B)Tabula Animata[#](), this dire sigil corrupts the world itself. "
                + "Right-click to convert nearby blocks into [#](8B0000)AntiLife[#](), a spreading, "
                + "devouring substance that consumes all it touches.");

        this.page("mechanics", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Mechanics");
        this.pageText("Each activation costs [#](4A0080)25 EV[#]() per block converted (configurable) "
                + "and affects blocks within an 8-block range (configurable).\\\n\\\n"
                + "[#](8B0000)AntiLife cannot be reversed.[#]() Once the corruption takes hold, there is no "
                + "undoing what you have wrought. Plan your targets with care.");

        this.page("protection", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Protected Blocks");
        this.pageText("Certain blocks resist the corruption entirely. Anything tagged "
                + "[#](4A0080)animusnv:disallow_antilife[#]() cannot be converted:\n\n"
                + "- Barrier and Bedrock\n\n"
                + "- Command blocks\n\n"
                + "- Structure blocks\n\n"
                + "- End portal frames\n\n"
                + "- AntiLife itself");
    }

    @Override
    protected String entryName() {
        return "Sigil of Consumption";
    }

    @Override
    protected String entryDescription() {
        return "Corrupts the world with spreading AntiLife. Use with extreme caution.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_CONSUMPTION.get());
    }

    @Override
    protected String entryId() {
        return "sigil_consumption";
    }
}
