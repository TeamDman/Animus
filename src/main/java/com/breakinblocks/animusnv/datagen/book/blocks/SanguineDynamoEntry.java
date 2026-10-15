package com.breakinblocks.animusnv.datagen.book.blocks;

import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class SanguineDynamoEntry extends EntryProvider {

    public SanguineDynamoEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/sanguine_dynamo")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sanguine Dynamo");
        this.pageText("The [#](8B0000)Sanguine Dynamo[#]() turns [#](8B0000)Essentia Vitae[#]() "
                + "into Forge Energy, drawing straight from an Anima network.\\\n\\\n"
                + "Right-click it with a [#](B8860B)bound[#]() Orb of Vitae. The orb's owner pays the EV, "
                + "and the orb's tier sets the maximum rate. Right-click with an empty hand to take the orb back.\\\n\\\n"
                + "Energy is pushed into every adjacent block that accepts it, and cables can also pull from it.");

        this.page("rates", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Rates");
        this.pageText("Each EV becomes [#](B8860B)5 FE[#]().\\\n\\\n"
                + "Maximum output per orb:\\\n"
                + "Novicius: 80 FE/t\\\n"
                + "Discipulus: 320 FE/t\\\n"
                + "Veneficus: 1,280 FE/t\\\n"
                + "Magus: 5,120 FE/t\\\n"
                + "Dominus: 20,480 FE/t\\\n"
                + "Divinus: 81,920 FE/t\\\n"
                + "Transcendent: 100,000 FE/t\\\n"
                + "Aurichalcum (FTB Armory): 125,000 FE/t\\\n\\\n"
                + "[#](2E8B57)The dynamo only spends EV to refill its buffer, so nothing is wasted while "
                + "nothing draws power. Check JEI for the rates in your pack.[#]()");

        this.page("control", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Control");
        this.pageText("A redstone signal lowers the draw: signal strength 15 runs at the full rate, "
                + "strength 1 at a fifteenth of it. With no signal it runs at the full rate.\\\n\\\n"
                + "Sneak + right-click with an empty hand to see the current rate, the buffer, "
                + "and how much EV is left in the network.\\\n\\\n"
                + "A comparator reads how full the buffer is.");
    }

    @Override
    protected String entryName() {
        return "Sanguine Dynamo";
    }

    @Override
    protected String entryDescription() {
        return "Converts Essentia Vitae into Forge Energy.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusBlocks.BLOCK_SANGUINE_DYNAMO.get().asItem());
    }

    @Override
    protected String entryId() {
        return "sanguine_dynamo";
    }
}
