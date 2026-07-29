package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class HungerEntry extends EntryProvider {

    public HungerEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Hunger");
        this.pageText("An [#](4A0080)imperfect ritual[#]() that induces extreme hunger in the caster. Your food level plummets to near-starvation, leaving you ravenous."
                + "\\\n\\\nPlace a [#](8B0000)Bone Block[#]() atop an [#](8B0000)Imperfect Ritual Stone[#]()."
                + "\\\n\\\n[#](B8860B)Cost:[#]() 500 EV per activation"
                + "\\\n\\\nThe stone remains intact for reuse.");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("Right-click the [#](8B0000)Imperfect Ritual Stone[#]() to:"
                + "\n\n- Set your food level to 1"
                + "\n\n- Set your saturation to 10"
                + "\n\n- Hear an ominous sound as the rite completes"
                + "\\\n\\\nUseful for preparing food-based buffs, testing hunger mechanics, or strategic hunger management."
                + "\\\n\\\n[#](2E8B57)Have food ready before activating. You will be on the verge of starvation.[#]()");
    }

    @Override
    protected String entryName() {
        return "Hunger";
    }

    @Override
    protected String entryDescription() {
        return "An imperfect ritual that drains your food to near-starvation.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.BONE_BLOCK);
    }

    @Override
    protected String entryId() {
        return "hunger";
    }
}
