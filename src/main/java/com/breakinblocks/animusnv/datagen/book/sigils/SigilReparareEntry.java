package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class SigilReparareEntry extends EntryProvider {

    public SigilReparareEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/reagentreparare")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "array/sigil_reparare")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Reparare");
        this.pageText("A toggleable sigil that mends your equipment over time. While active, damaged "
                + "items throughout your inventory slowly repair themselves at the cost of "
                + "[#](4A0080)Essentia Vitae[#]().");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Right-click to toggle on or off. The sigil periodically scans all inventory "
                + "slots, armor, and offhand for damaged items and repairs them.\\\n\\\n"
                + "The [#](4A0080)EV[#]() cost per durability point is configurable. The sigil "
                + "deactivates if your reserves are exhausted.");

        this.page("limitations", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Limitations");
        this.pageText("Items tagged with [#](4A0080)animus:disallow_repair[#]() cannot be restored "
                + "by this sigil. The repair rate per interval is also configurable.\\\n\\\n"
                + "[#](4A0080)Blood mends what time breaks.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of Reparare";
    }

    @Override
    protected String entryDescription() {
        return "Passively repairs all damaged equipment in your inventory over time.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_REPARARE.get());
    }

    @Override
    protected String entryId() {
        return "sigil_reparare";
    }
}
