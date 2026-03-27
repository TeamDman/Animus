package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class WardenEntry extends EntryProvider {

    public WardenEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Warden's Cloak");
        this.pageText("An [#](4A0080)imperfect ritual[#]() that channels the defensive power of the Warden itself, wrapping you in an impenetrable cloak of obsidian force."
                + "\\\n\\\nPlace a [#](8B0000)Sculk Block[#]() atop an [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Cost:[#]() 3,000 EV per activation"
                + "\\\n[#](B8860B)Duration:[#]() 15 minutes"
                + "\\\n\\\nThe stone remains intact for reuse.");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("Right-click the [#](8B0000)Imperfect Ritual Stone[#]() to receive the [#](4A0080)Obsidian Cloak[#]() effect for 15 minutes."
                + "\\\n\\\nThis effect dramatically reduces incoming damage, making you nearly invulnerable to most attacks."
                + "\\\n\\\n[#](2E8B57)Ideal for boss fights, dangerous exploration, PvP combat, and mining in hazardous areas. The darkness itself becomes your shield.[#]()");
    }

    @Override
    protected String entryName() {
        return "Warden's Cloak";
    }

    @Override
    protected String entryDescription() {
        return "An imperfect ritual granting the Obsidian Cloak damage reduction effect.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.SCULK);
    }

    @Override
    protected String entryId() {
        return "warden";
    }
}
