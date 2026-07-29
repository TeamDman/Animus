package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAraVitaeRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;

public class FragmentHealingEntry extends EntryProvider {

    public FragmentHealingEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookAraVitaeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "ara_vitae/fragment_healing")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Fragment of Healing");
        this.pageText("A crystallized shard of restorative energy. Once acquired, this fragment binds "
                + "permanently to your inventory and provides passive regeneration. It cannot be "
                + "dropped, moved, or discarded. Only death will release its grip.");

        this.page("mechanics", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Mechanics");
        this.pageText("The fragment restores [#](2E8B57)1 HP every 10 seconds[#]() as a base rate. "
                + "Each additional fragment reduces the healing interval by 0.25 seconds, down to a "
                + "minimum of 1 second with many fragments.\\\n\\\n"
                + "[#](8B0000)Fragments are permanently bound to your inventory. They can only be "
                + "removed on death.[#]()\\\n\\\n"
                + "[#](4A0080)A blessing... or a curse?[#]()");
    }

    @Override
    protected String entryName() {
        return "Fragment of Healing";
    }

    @Override
    protected String entryDescription() {
        return "A permanently bound shard that passively regenerates your health.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.FRAGMENT_HEALING.get());
    }

    @Override
    protected String entryId() {
        return "fragment_healing";
    }
}
