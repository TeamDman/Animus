package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAraVitaeRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.resources.Identifier;

public class BloodTreesEntry extends EntryProvider {

    public BloodTreesEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookAraVitaeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "ara_vitae/blood_sapling")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Blood Trees");
        this.pageText("Trees infused with Vitaemancy. [#](8B0000)Blood Saplings[#]() grow into "
                + "unique trees topped with a [#](8B0000)Blood Core[#]() that can spread to "
                + "create entire blood forests.\\\n\\\n"
                + "[#](4A0080)Life forced into unnatural growth. The wood remembers the "
                + "sacrifice that birthed it, and seeks to propagate that memory.[#]()");

        this.page("growing", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Growing Blood Trees");
        this.pageText("Plant a [#](8B0000)Blood Sapling[#]() and wait for it to grow, or hasten "
                + "the process with bonemeal. The tree grows 4-6 blocks tall.\\\n\\\n"
                + "The resulting structure consists of:\n\n"
                + "- [#](8B0000)Blood Wood[#]() trunk\n\n"
                + "- [#](8B0000)Blood Leaves[#]() canopy\n\n"
                + "- [#](8B0000)Blood Core[#]() at the apex\\\n\\\n"
                + "Harvest the wood for building materials. See the Decorative Blocks "
                + "entry for crafting options.");

        this.page("core", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Blood Core");
        this.pageText("The heart of every blood tree. When active (right-click to toggle), "
                + "the core automatically spreads blood trees in the surrounding area.\\\n\\\n"
                + "An active core:\n\n"
                + "- Searches for grass or dirt nearby\n\n"
                + "- Places Blood Saplings\n\n"
                + "- Instantly grows them into trees\n\n"
                + "- Creates a blood forest over time");

        this.page("tips", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Tips");
        this.pageText("[#](2E8B57)You can apply bonemeal to active cores to trigger immediate "
                + "spreading.[#]()\\\n\\\n"
                + "[#](8B0000)Blood Leaves[#]() drop Blood Saplings when broken, just like "
                + "normal trees.\\\n\\\n"
                + "[#](2E8B57)Combine with tree farms for automated blood wood production.[#]()\\\n\\\n"
                + "[#](4A0080)Let the blood forest grow.[#]()");
    }

    @Override
    protected String entryName() {
        return "Blood Trees";
    }

    @Override
    protected String entryDescription() {
        return "Trees born of sacrifice, crowned with spreading Blood Cores.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusBlocks.BLOCK_BLOOD_SAPLING.get().asItem());
    }

    @Override
    protected String entryId() {
        return "blood_trees";
    }
}
