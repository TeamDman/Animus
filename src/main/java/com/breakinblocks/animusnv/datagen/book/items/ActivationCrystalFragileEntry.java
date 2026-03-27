package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAraVitaeRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class ActivationCrystalFragileEntry extends EntryProvider {

    public ActivationCrystalFragileEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookAraVitaeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "ara_vitae/activation_crystal_fragile")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Fragile Activation Crystal");
        this.pageText("A delicate activation crystal that shatters after a single use. Where a standard "
                + "crystal endures, this fragile variant is consumed upon successful ritual activation "
                + ", disposable power for those who need it only once.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("The crystal must be bound to your [#](4A0080)Anima[#]() before use. "
                + "Right-click a Master Ritual Stone with a valid ritual pattern arranged around it.\\\n\\\n"
                + "[#](8B0000)The crystal shatters upon successful activation.[#]()\\\n\\\n"
                + "[#](2E8B57)Use this when you need a one-time ritual activation without investing "
                + "in a permanent crystal.[#]()");
    }

    @Override
    protected String entryName() {
        return "Fragile Activation Crystal";
    }

    @Override
    protected String entryDescription() {
        return "A single-use activation crystal that shatters after one ritual activation.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.ACTIVATION_CRYSTAL_FRAGILE.get());
    }

    @Override
    protected String entryId() {
        return "activation_crystal_fragile";
    }
}
