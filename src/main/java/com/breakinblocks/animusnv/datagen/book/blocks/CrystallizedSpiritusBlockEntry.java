package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookStonecuttingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.resources.Identifier;

public class CrystallizedSpiritusBlockEntry extends EntryProvider {

    public CrystallizedSpiritusBlockEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookStonecuttingRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "crystallized_spiritus_block_from_crystal_cluster"))
                .withRecipeId2(Identifier.fromNamespaceAndPath("animusnv", "crystal_cluster_from_crystallized_spiritus_block")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Crystallized Spiritus Block");
        this.pageText("Solidified [#](4A0080)Spiritus[#]() in crystalline form. This decorative "
                + "pillar serves as a valid [#](B8860B)CRYSTAL[#]() capstone for the "
                + "[#](B8860B)Transcendent[#]() Ara Vitae.\\\n\\\n"
                + "Cut it from a [#](4A0080)Crystal Cluster[#]() on a stonecutter, or cut it "
                + "back again if you prefer the other shape.\\\n\\\n"
                + "[#](4A0080)Will made manifest. Spiritus compressed beyond the threshold "
                + "of dissolution, locked into permanent physical form.[#]()");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("The Crystallized Spiritus Block functions as a [#](B8860B)CRYSTAL[#]() "
                + "block for altar construction. It can stand in for the crystal cluster "
                + "capstones on the highest tier [#](8B0000)Ara Vitae[#]().\\\n\\\n"
                + "See the Scriptura Vitae for full altar construction details.\\\n\\\n"
                + "[#](2E8B57)Beyond its functional role, the block radiates a faint demonic "
                + "light, making it a striking addition to any ritual chamber.[#]()");
    }

    @Override
    protected String entryName() {
        return "Crystallized Spiritus Block";
    }

    @Override
    protected String entryDescription() {
        return "Solidified Spiritus, an alternate CRYSTAL capstone for the Transcendent Ara Vitae.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusBlocks.BLOCK_CRYSTALLIZED_SPIRITUS.get().asItem());
    }

    @Override
    protected String entryId() {
        return "crystallized_spiritus_block";
    }
}
