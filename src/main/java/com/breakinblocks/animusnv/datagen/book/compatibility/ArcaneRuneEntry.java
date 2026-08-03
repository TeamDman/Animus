package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.condition.BookModLoadedConditionModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class ArcaneRuneEntry extends EntryProvider {

    public ArcaneRuneEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "arcane_rune")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Arcane Rune");
        this.pageText("The [#](8B0000)Arcane Rune[#]() bridges Ars Nouveau's Source magic with "
                + "the altar system. Unlike traditional runes that provide static bonuses, "
                + "this rune's power fluctuates based on its internal Source supply.\\\n\\\n"
                + "[#](4A0080)Two disciplines of magic, woven into a single stone. The rune "
                + "hums with dual resonance, blood and arcana intertwined.[#]()");

        this.page("placement", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Source-Powered Rune");
        this.pageText("Place this rune in your [#](8B0000)Ara Vitae[#]() structure just like any "
                + "other rune. It stores up to 1000 Source internally and can receive "
                + "Source via Ars Nouveau's dominion wand linking system.\\\n\\\n"
                + "Crafted using a [#](8B0000)Blank Rune[#]() at the center, any Stone in "
                + "corners, [#](8B0000)Tabula Rasa[#]() top and bottom, and "
                + "[#](8B0000)Source Gems[#]() on the sides.");

        this.page("linking", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Linking Source");
        this.pageText("To supply the Arcane Rune with Source:\n\n"
                + "- Use an Ars Nouveau [#](8B0000)Dominion Wand[#]() to link a Source Jar "
                + "to the rune\n\n"
                + "- Source will automatically flow from the jar to the rune\n\n"
                + "- The rune can hold up to 1000 Source at a time\\\n\\\n"
                + "This gives you precise control over which Source Jars supply your "
                + "altar runes.");

        this.page("states", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Power States");
        this.pageText("The rune has two distinct states based on Source availability:\\\n\\\n"
                + "[#](8B0000)Powered State[#]() (20 Source consumed every 10 seconds):\n\n"
                + "- Acts as a Speed Rune but 20%% faster\n\n"
                + "- Provides Dislocation Rune bonus\\\n\\\n"
                + "[#](4A0080)Unpowered State[#]() (No Source available):\n\n"
                + "- Acts as a Speed Rune at 67.5%% effectiveness\n\n"
                + "- No Dislocation bonus");

        this.page("strategy", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Strategic Use");
        this.pageText("The Arcane Rune excels in hybrid builds:\n\n"
                + "- Provides superior altar speed when Source is plentiful\n\n"
                + "- Acts as a backup speed rune when Source runs out\n\n"
                + "- The dislocation bonus increases EV transfer rate\\\n\\\n"
                + "[#](2E8B57)Consider the Source consumption rate (2 Source/second per rune) "
                + "when planning your Source generation and storage. This rate is configurable.[#]()\\\n\\\n"
                + "The rune emits a subtle glow (light level 7) when placed.");
    }

    @Override
    protected String entryName() {
        return "Arcane Rune";
    }

    @Override
    protected String entryDescription() {
        return "A hybrid altar rune powered by Ars Nouveau Source magic.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.AMETHYST_SHARD);
    }

    @Override
    protected String entryId() {
        return "arcane_rune";
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return super.additionalSetup(entry)
                .withCondition(BookModLoadedConditionModel.create().withModId("ars_nouveau"));
    }
}
