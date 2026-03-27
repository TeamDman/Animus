package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;

public class KeyBindingEntry extends EntryProvider {

    public KeyBindingEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Key of Binding");
        this.pageText("A mystical key bound to a blood mage's [#](8B0000)Anima[#](). Its primary "
                + "purpose is to share your [#](4A0080)Essentia Vitae[#]() with allies. Place a "
                + "[#](8B0000)Simple Key[#]() in a [#](B8860B)Tier 1[#]() Ara Vitae with 1,500 EV "
                + "to create one.");

        this.page("sharing", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sharing EV");
        this.pageText("To share your [#](4A0080)Essentia Vitae[#]() with another player:\n\n"
                + "- Bind the key to yourself (right-click)\n\n"
                + "- Give the bound key to a friend\n\n"
                + "- Your friend holds it in their offhand\n\n"
                + "- Any items they bind will draw from your Anima\\\n\\\n"
                + "This powers your allies' sigils and bound tools without them needing their own "
                + "EV infrastructure.");

        this.page("usage", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Usage");
        this.pageText("Right-click while holding the key to bind it to your [#](8B0000)Anima[#](). "
                + "Hold it in your offhand while binding items to use the key owner's network. "
                + "It can also be equipped in the Curios \"key\" slot for the same effect.");

        this.page("strategy", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Team Strategies");
        this.pageText("Consider these cooperative approaches:\n\n"
                + "- Designate one player as the EV generator\n\n"
                + "- Share keys with combat-focused teammates\n\n"
                + "- Power expensive rituals from a friend's network\n\n"
                + "- Let new players use advanced sigils immediately\\\n\\\n"
                + "[#](4A0080)The key to unlocking cooperation in the sanguine arts.[#]()");
    }

    @Override
    protected String entryName() {
        return "Key of Binding";
    }

    @Override
    protected String entryDescription() {
        return "A mystical key that lets allies draw from your Anima reserves.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.KEY_BINDING.get());
    }

    @Override
    protected String entryId() {
        return "key_binding";
    }
}
