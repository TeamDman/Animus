package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookTabulaVitaeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class SigilMonkEntry extends EntryProvider {

    public SigilMonkEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookTabulaVitaeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "alchemytable/reagentfist")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "array/sigil_monk")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of the Monk");
        this.pageText("A curio sigil that channels demonic power into devastating unarmed combat. "
                + "You master the way of the fist, empowered by [#](4A0080)Spiritus[#](). Craft the "
                + "[#](8B0000)Reagent of the Fist[#]() in a Tabula Vitae, then combine it with a "
                + "[#](B8860B)Reinforced Slate[#]() in an Alchemy Array.");

        this.page("abilities", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Basic Abilities");
        this.pageText("Right-click to toggle. While active, you gain:\n\n"
                + "- +10 unarmed damage with knockback\n\n"
                + "- Netherite + Efficiency V mining with bare hands\n\n"
                + "- On kill: Resistance II (5s) + Regeneration (2s)\n\n"
                + "- Applies Spiritus Snare II (2s) on hit\n\n"
                + "- Fall damage immunity (25 EV per fall)");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("EV Costs");
        this.pageText("The sigil draws from your [#](4A0080)Anima[#]() for various actions:\n\n"
                + "- [#](8B0000)5 EV[#]() per unarmed attack\n\n"
                + "- [#](8B0000)5 EV[#]() per block mined\n\n"
                + "- [#](8B0000)25 EV[#]() per fall damage negated\n\n"
                + "- [#](8B0000)50 EV[#]() per projectile caught\\\n\\\n"
                + "Ensure your Anima has sufficient reserves before extended combat or mining.");

        this.page("projectile", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Projectile Catch");
        this.pageText("Incoming projectiles can be caught when your main hand is empty or holding a "
                + "catchable projectile:\n\n"
                + "- Arrows (regular and spectral)\n\n"
                + "- Tridents\n\n"
                + "- Firework rockets\\\n\\\n"
                + "Caught projectiles go to your main hand, or stack if holding the same type.");

        this.page("spiritus_bonus", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Spiritus Scaling");
        this.pageText("Your unarmed attacks deal [#](4A0080)bonus damage[#]() equal to a percentage "
                + "of the target's maximum health, scaling with [#](4A0080)Spiritus[#]():\n\n"
                + "- [#](B8860B)1 Will:[#]() +1%% max HP damage\n\n"
                + "- [#](B8860B)4,096 Will:[#]() +15%% max HP damage\\\n\\\n"
                + "All will types (Raw, Corrosive, etc.) are combined for this calculation.");

        this.page("execute", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Execute");
        this.pageText("If your attack would leave an enemy below the execute threshold (equal to your "
                + "bonus damage percentage), you perform an [#](8B0000)Execute[#]():\n\n"
                + "- Instant kill\n\n"
                + "- +200 EV added to your Anima\n\n"
                + "- -5 Will consumed from inventory\\\n\\\n"
                + "Requires at least 5 will to trigger. A burst of soul particles marks the kill.");
    }

    @Override
    protected String entryName() {
        return "Sigil of the Monk";
    }

    @Override
    protected String entryDescription() {
        return "Channels demonic power into devastating unarmed combat and bare-hand mining.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_MONK.get());
    }

    @Override
    protected String entryId() {
        return "sigil_monk";
    }
}
