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

public class SigilHeavenlyWrathEntry extends EntryProvider {

    public SigilHeavenlyWrathEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "hellfire_forge/reagentheavelywrath")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "array/sigil_heavenly_wrath")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Heavenly Wrath");
        this.pageText("A devastating sigil that calls down heaven's judgment upon your enemies. Those "
                + "caught within its reach are lifted skyward before being slammed back to earth with "
                + "deadly force. The cost is [#](4A0080)1,000 EV[#]() per activation, with a range of "
                + "16 blocks.");

        this.page("mechanics", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Mechanics");
        this.pageText("Right-click to activate. All enemies within range receive "
                + "[#](4A0080)Levitation II[#]() for 3 seconds with an additional upward velocity boost. "
                + "Once the effect ends, they plummet.\\\n\\\n"
                + "Targets also receive the [#](8B0000)Heavy Heart[#]() effect for 2 seconds, "
                + "preventing flight of any kind.");

        this.page("scaling", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Velocity Scaling");
        this.pageText("Flying creatures or those already at great height receive stronger downward "
                + "velocity based on their distance from the ground. The higher they are, the harder "
                + "they fall.\\\n\\\n"
                + "[#](4A0080)What goes up must come down... violently.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of Heavenly Wrath";
    }

    @Override
    protected String entryDescription() {
        return "Lifts enemies skyward then slams them earthward with lethal force.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_HEAVENLY_WRATH.get());
    }

    @Override
    protected String entryId() {
        return "sigil_heavenly_wrath";
    }
}
