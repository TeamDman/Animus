package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class SteadfastHeartEntry extends EntryProvider {

    public SteadfastHeartEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of the Steadfast Heart");
        this.pageText("Fortify your allies. The [#](4A0080)Ritual of the Steadfast Heart[#]() grants escalating [#](4A0080)Absorption[#]() effects to all nearby players, providing extra health that regenerates over time. It can also buff players remotely through bound orbs of vitae."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner[#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Details");
        this.pageText("[#](B8860B)Activation:[#]() 20,000 EV"
                + "\\\n[#](B8860B)Refresh:[#]() 100 EV per player"
                + "\\\n[#](B8860B)Refresh Time:[#]() 60 ticks (3 seconds, configurable)"
                + "\\\n\\\n[#](B8860B)Effect Range:[#]() 128 blocks radius (configurable)"
                + "\\\n\\\nEach cycle extends and strengthens the [#](4A0080)Absorption[#]() effect on all nearby players.");

        this.page("remote", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Remote Buffing");
        this.pageText("Place a chest directly above the [#](8B0000)Master Ritual Stone[#]() and fill it with bound orbs of vitae of any tier."
                + "\\\n\\\nThe ritual will buff the bound player regardless of their location, even across dimensions, as long as they are online."
                + "\n\n- Each player is only buffed once per cycle"
                + "\n\n- Works with any tier orb of vitae"
                + "\n\n- EV cost applies per player buffed");

        this.page("scaling", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effect Scaling");
        this.pageText("The [#](4A0080)Absorption[#]() effect stacks duration and amplifier over time."
                + "\n\n- Duration increases each cycle"
                + "\n\n- Max duration: 30,000 ticks"
                + "\n\n- Max amplifier: 4 (10 absorption hearts, configurable)"
                + "\\\n\\\nThe ritual also generates [#](4A0080)Spiritus Invictus[#]() up to a maximum of 100 in the area.");
    }

    @Override
    protected String entryName() {
        return "Ritual of the Steadfast Heart";
    }

    @Override
    protected String entryDescription() {
        return "Grants scaling Absorption to nearby players, with remote buffing via orbs of vitae.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.GOLDEN_APPLE);
    }

    @Override
    protected String entryId() {
        return "steadfast_heart";
    }
}
