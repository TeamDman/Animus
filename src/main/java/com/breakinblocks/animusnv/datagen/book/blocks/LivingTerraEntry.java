package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookTabulaVitaeRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.resources.Identifier;

public class LivingTerraEntry extends EntryProvider {

    public LivingTerraEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookTabulaVitaeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "alchemytable/living_terra_bucket")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Living Terra");
        this.pageText("A mystical fluid infused with Essentia Vitae. [#](8B0000)Living Terra[#]() "
                + "is a rare liquid created through specialized processes, radiating with "
                + "vital energy.\\\n\\\n"
                + "[#](4A0080)Where AntiLife carries the negation of existence, Living Terra "
                + "pulses with its affirmation, raw potential given liquid form.[#]()");

        this.page("properties", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Properties");
        this.pageText("Living Terra exhibits unique properties:\n\n"
                + "- Flows like water\n\n"
                + "- Glows with inner light\n\n"
                + "- Can be collected in buckets\n\n"
                + "- Used in advanced Vitaemancy\\\n\\\n"
                + "This fluid represents the positive aspect of life force, contrasting "
                + "sharply with the corruption of [#](8B0000)AntiLife[#]().");

        this.page("applications", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Applications");
        this.pageText("Living Terra can be employed for:\n\n"
                + "- Decorative purposes\n\n"
                + "- Advanced rituals\n\n"
                + "- Crafting components\n\n"
                + "- Environmental effects\\\n\\\n"
                + "[#](4A0080)The essence of life itself, contained. Handle it with the "
                + "reverence such power demands.[#]()");
    }

    @Override
    protected String entryName() {
        return "Living Terra";
    }

    @Override
    protected String entryDescription() {
        return "A mystical fluid infused with vital energy, the antithesis of AntiLife.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.LIVING_TERRA_BUCKET.get());
    }

    @Override
    protected String entryId() {
        return "living_terra";
    }
}
