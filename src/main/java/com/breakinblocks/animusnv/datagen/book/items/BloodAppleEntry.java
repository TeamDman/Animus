package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;

public class BloodAppleEntry extends EntryProvider {

    public BloodAppleEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Blood Apple");
        this.pageText("A crimson fruit infused with [#](4A0080)Vitaemancy[#](). Consuming this apple "
                + "provides [#](4A0080)Essentia Vitae[#]() to your [#](8B0000)Anima[#](), or to a "
                + "nearby [#](8B0000)Ara Vitae[#]() if one stands within range.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("The apple restores 3 hunger with low saturation, and has a 75%% chance to "
                + "inflict brief confusion.\\\n\\\n"
                + "If an [#](8B0000)Ara Vitae[#]() stands within an 11x21x11 area centered on you, "
                + "the EV is deposited at 2x value into the altar. Otherwise, it flows directly into "
                + "your [#](8B0000)Anima[#]().\\\n\\\n"
                + "[#](4A0080)A bittersweet taste of power.[#]()");

        this.page("key_binding", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Key of Binding Synergy");
        this.pageText("If you carry a bound [#](8B0000)Key of Binding[#]() in your inventory or Curios "
                + "slot while eating a Blood Apple (with no altar nearby), the EV is deposited into "
                + "the key owner's [#](8B0000)Anima[#]() instead of your own.\\\n\\\n"
                + "[#](2E8B57)This allows you to farm EV for other players. Simply carry their "
                + "bound key and eat apples to fill their network remotely.[#]()");
    }

    @Override
    protected String entryName() {
        return "Blood Apple";
    }

    @Override
    protected String entryDescription() {
        return "A crimson fruit that generates Essentia Vitae when consumed.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.BLOOD_APPLE.get());
    }

    @Override
    protected String entryId() {
        return "blood_apple";
    }
}
