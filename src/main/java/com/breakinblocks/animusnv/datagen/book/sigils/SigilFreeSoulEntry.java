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

public class SigilFreeSoulEntry extends EntryProvider {

    public SigilFreeSoulEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/reagentfreesoul")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "array/sigil_free_soul")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of the Free Soul");
        this.pageText("A powerful protective sigil that grants temporary [#](4A0080)spectator mode[#]() and "
                + "can prevent death outright. It functions as a reusable Totem of Undying, powered by "
                + "your reserves of [#](4A0080)Essentia Vitae[#]().");

        this.page("manual", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Manual Activation");
        this.pageText("Right-click to enter spectator mode for [#](4A0080)10 seconds[#]() (configurable), "
                + "at a cost of [#](4A0080)5,000 EV[#]() (configurable). You may scout areas, pass through "
                + "walls, and escape danger freely.\\\n\\\n"
                + "You are teleported back to your original position before the effect ends.");

        this.page("death_prevention", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Death Prevention");
        this.pageText("When you would die with this sigil in your inventory, it automatically activates "
                + "if you have sufficient [#](4A0080)EV[#](). Upon returning from spectator mode, you "
                + "receive 5 hearts of healing.\\\n\\\n"
                + "A [#](4A0080)60-second cooldown[#]() (configurable) separates death prevention triggers.");

        this.page("safety", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Position Lock");
        this.pageText("[#](2E8B57)You are teleported back to your original position 0.5 seconds before "
                + "spectator mode ends, preventing wall-clipping exploits.[#]()\\\n\\\n"
                + "[#](4A0080)The soul slips free of death's grasp... for a price.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sigil of the Free Soul";
    }

    @Override
    protected String entryDescription() {
        return "A reusable Totem of Undying that grants spectator mode and prevents death.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_FREE_SOUL.get());
    }

    @Override
    protected String entryId() {
        return "sigil_free_soul";
    }
}
