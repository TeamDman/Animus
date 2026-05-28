package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class SpearsEntry extends EntryProvider {

    public SpearsEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("bound_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "array/spear_bound")));

        this.page("sentient_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/spear_sentient")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Spears");
        this.pageText("Roman-style throwable javelins. Hold right-click to charge, then release to "
                + "throw. Spears deal area-of-effect damage where they land, making them effective "
                + "against clustered enemies.");

        this.page("iron", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Iron Spear");
        this.pageText("The [#](8B0000)Iron Spear[#]() is the basic variant, with serviceable damage and "
                + "durability for its cost. Supports enchantments such as Sharpness and Looting.");

        this.page("diamond", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Diamond Spear");
        this.pageText("The [#](8B0000)Diamond Spear[#]() offers higher damage and durability than "
                + "iron, but functions identically. A worthy upgrade for those who can afford it.");

        this.page("bound", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Bound Spear");
        this.pageText("The [#](8B0000)Bound Spear[#]() is a soul-bound weapon with two modes. "
                + "While deactivated, it behaves like a diamond spear. Sneak + right-click to toggle "
                + "its activated state.");

        this.page("bound_active", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Activated Features");
        this.pageText("When activated, the Bound Spear costs [#](4A0080)50 EV[#]() per attack or "
                + "throw, and gains:\n\n"
                + "- AOE melee damage (5 block radius)\n\n"
                + "- Enemies near an Ara Vitae are sacrificed (instant kill + EV to altar)\n\n"
                + "- Unbreakable and fireproof\\\n\\\n"
                + "[#](4A0080)A capstone weapon for Vitaemancy.[#]()");
    }

    @Override
    protected String entryName() {
        return "Spears";
    }

    @Override
    protected String entryDescription() {
        return "Throwable javelins in iron, diamond, and soul-bound variants.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SPEAR_BOUND.get());
    }

    @Override
    protected String entryId() {
        return "spears";
    }
}
