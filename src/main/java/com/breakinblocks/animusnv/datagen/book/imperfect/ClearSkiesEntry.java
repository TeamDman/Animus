package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class ClearSkiesEntry extends EntryProvider {

    public ClearSkiesEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Clear Skies");
        this.pageText("An [#](4A0080)imperfect ritual[#]() that commands the weather to cease its fury. Rain and thunderstorms are banished, bringing roughly five minutes of clear skies."
                + "\\\n\\\nPlace a [#](8B0000)Glowstone Block[#]() atop an [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Cost:[#]() 1,000 EV per activation"
                + "\\\n\\\nThe stone remains intact for reuse.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Right-click the [#](8B0000)Imperfect Ritual Stone[#]() to:"
                + "\n\n- Clear the weather instantly"
                + "\n\n- Stop all rain and thunder"
                + "\n\n- Maintain clear skies for approximately 5 minutes"
                + "\\\n\\\n[#](2E8B57)Useful for stopping rain during important outdoor tasks, clearing thunderstorms for safety, and ensuring ideal conditions for surface work.[#]()");
    }

    @Override
    protected String entryName() {
        return "Clear Skies";
    }

    @Override
    protected String entryDescription() {
        return "An imperfect ritual that clears the weather.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.GLOWSTONE);
    }

    @Override
    protected String entryId() {
        return "clear_skies";
    }
}
