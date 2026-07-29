package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class BoundlessSkiesEntry extends EntryProvider {

    public BoundlessSkiesEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Boundless Skies");
        this.pageText("An [#](4A0080)imperfect ritual[#]() that tears open a path to the heavens, granting you temporary creative flight. This costly rite buys fifteen minutes of unrestricted passage through the sky."
                + "\\\n\\\nPlace an [#](8B0000)Ancient Debris[#]() block atop an [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Cost:[#]() 10,000 EV per activation"
                + "\\\n\\\nThe stone remains intact for reuse.");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("Right-click the [#](8B0000)Imperfect Ritual Stone[#]() to receive:"
                + "\n\n- The [#](4A0080)Flight[#]() effect for 15 minutes (900 seconds)"
                + "\n\n- A lightning strike upon the stone as the rite completes"
                + "\\\n\\\nYou gain creative-style flight that can be reapplied before it expires."
                + "\\\n\\\n[#](2E8B57)The high EV cost makes this a premium rite. Ensure your reserves are deep before calling upon the boundless skies.[#]()");
    }

    @Override
    protected String entryName() {
        return "Boundless Skies";
    }

    @Override
    protected String entryDescription() {
        return "An imperfect ritual granting temporary creative flight.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.ANCIENT_DEBRIS);
    }

    @Override
    protected String entryId() {
        return "boundless_skies";
    }
}
