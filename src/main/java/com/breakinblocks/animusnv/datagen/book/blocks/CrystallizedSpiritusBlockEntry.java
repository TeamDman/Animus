package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.mojang.datafixers.util.Pair;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.resources.ResourceLocation;

public class CrystallizedSpiritusBlockEntry extends EntryProvider {

    public CrystallizedSpiritusBlockEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/crystallized_spiritus_block")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Crystallized Spiritus Block");
        this.pageText("Solidified [#](4A0080)Spiritus[#]() in crystalline form. This decorative "
                + "pillar serves as a valid [#](B8860B)CRYSTAL[#]() component for "
                + "[#](B8860B)Tier 6[#]() Ara Vitaes.\\\n\\\n"
                + "[#](4A0080)Will made manifest. Spiritus compressed beyond the threshold "
                + "of dissolution, locked into permanent physical form.[#]()");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("The Crystallized Spiritus Block functions as a [#](B8860B)CRYSTAL[#]() "
                + "block for altar construction. It is required for the highest tier "
                + "[#](8B0000)Ara Vitae[#]().\\\n\\\n"
                + "See the [#](8B0000)Tier 6 Altar[#]() entry for full construction details.\\\n\\\n"
                + "[#](2E8B57)Beyond its functional role, the block radiates a faint demonic "
                + "light, making it a striking addition to any ritual chamber.[#]()");
    }

    @Override
    protected String entryName() {
        return "Crystallized Spiritus Block";
    }

    @Override
    protected String entryDescription() {
        return "Solidified demonic will, used as a CRYSTAL component for Tier 6 altars.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
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
