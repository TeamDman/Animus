package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;

public class MobSoulEntry extends EntryProvider {

    public MobSoulEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Mob Soul");
        this.pageText("A container holding the essence of a captured creature. Created by the "
                + "[#](8B0000)Sigil of the Phantom Chain[#](), this item preserves an entity's "
                + "complete state: health, equipment, custom names, and AI.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Right-click on a block to release the captured entity at that location. "
                + "The [#](8B0000)Mob Soul[#]() is consumed when the entity is released.\\\n\\\n"
                + "All preserved data is restored:\n\n"
                + "- Entity type and health\n\n"
                + "- Equipment and inventory\n\n"
                + "- Custom names\n\n"
                + "- AI state and attributes\\\n\\\n"
                + "[#](4A0080)Souls bound to your will.[#]()");
    }

    @Override
    protected String entryName() {
        return "Mob Soul";
    }

    @Override
    protected String entryDescription() {
        return "A portable container preserving a captured entity's complete state.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.MOBSOUL.get());
    }

    @Override
    protected String entryId() {
        return "mob_soul";
    }
}
