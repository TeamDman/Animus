package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;

public class RitualFloralSupremacyEntry extends EntryProvider {

    public RitualFloralSupremacyEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Floral Supremacy");
        this.pageText("The [#](8B0000)Ritual of Floral Supremacy[#]() supercharges nearby "
                + "Botania mana-generating flowers, doubling their production rate as if "
                + "they were planted on enchanted soil.\\\n\\\n"
                + "[#](4A0080)Nature's bloom feeds on sacrifice. The flowers respond to the "
                + "pulse of Essentia Vitae flowing through the ritual stones, accelerating "
                + "their natural mana generation.[#]()");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs & Range");
        this.pageText("[#](8B0000)Activation:[#]() 10,000 EV\\\n\\\n"
                + "[#](4A0080)Upkeep:[#]() 50 EV per flower (configurable)\\\n\\\n"
                + "[#](B8860B)Range:[#]() 8 blocks (configurable)\\\n\\\n"
                + "[#](8B0000)Interval:[#]() Every second");

        this.page("effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Effects");
        this.pageText("All mana-generating flowers in range tick twice per second, "
                + "effectively doubling their output.\\\n\\\n"
                + "Mana Spreaders in range are also accelerated to 2x speed using "
                + "temporal dominance.\\\n\\\n"
                + "[#](2E8B57)Plan your flower garden layout within the ritual's range for "
                + "maximum efficiency. Requires Botania to be installed.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Floral Supremacy";
    }

    @Override
    protected String entryDescription() {
        return "Doubles Botania flower mana generation through blood sacrifice. Requires Botania.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.SUNFLOWER);
    }

    @Override
    protected String entryId() {
        return "ritual_floral_supremacy";
    }
}
