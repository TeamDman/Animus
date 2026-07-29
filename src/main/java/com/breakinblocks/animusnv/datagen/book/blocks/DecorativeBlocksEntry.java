package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.breakinblocks.animusnv.registry.AnimusItems;

public class DecorativeBlocksEntry extends EntryProvider {

    public DecorativeBlocksEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Decorative Blood Wood");
        this.pageText("[#](8B0000)Blood Wood[#]() harvested from Blood Trees can be processed into "
                + "a variety of decorative building blocks. The crimson wood makes for "
                + "striking architectural accents.\\\n\\\n"
                + "[#](4A0080)Every plank remembers the tree it came from, and the sacrifice "
                + "that grew it.[#]()");

        this.page("variants", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Wood Variants");
        this.pageText("Use an axe on [#](8B0000)Blood Wood[#]() logs to strip the bark, "
                + "revealing a smoother texture.\\\n\\\n"
                + "Craft Blood Wood logs into [#](8B0000)Planks[#]() (4 planks per log). "
                + "These planks serve as the base for all other decorative variants:\n\n"
                + "- [#](8B0000)Blood Wood Stairs[#](): 6 planks in a stair pattern\n\n"
                + "- [#](8B0000)Blood Wood Slabs[#](): 3 planks in a row\n\n"
                + "- [#](8B0000)Blood Wood Fence[#](): planks and sticks\n\n"
                + "- [#](8B0000)Blood Wood Fence Gate[#](): sticks and planks");

        this.page("crafting", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Crafting Summary");
        this.pageText("Standard wood recipes apply:\n\n"
                + "- Log -> 4 Planks\n\n"
                + "- 6 Planks -> 4 Stairs\n\n"
                + "- 3 Planks -> 6 Slabs\n\n"
                + "- 4 Planks + 2 Sticks -> 3 Fences\n\n"
                + "- 2 Planks + 4 Sticks -> 1 Gate\\\n\\\n"
                + "All Blood Wood blocks work with standard wood recipes and can be used "
                + "as fuel.\\\n\\\n"
                + "[#](4A0080)Build your dark empire in crimson.[#]()");
    }

    @Override
    protected String entryName() {
        return "Decorative Blocks";
    }

    @Override
    protected String entryDescription() {
        return "Crimson building materials crafted from Blood Wood.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get());
    }

    @Override
    protected String entryId() {
        return "decorative_blocks";
    }
}
