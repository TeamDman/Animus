package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class CullingEntry extends EntryProvider {

    public CullingEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(ResourceLocation.fromNamespaceAndPath("neovitae", "ritual/ritual_culling"))
                .withMultiblockName("Ritual of Culling")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Use a Ritual Diviner [Dusk] for easier construction.[#]()");

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual of Culling");
        this.pageText("A ritual of absolute dominion over life and death. The [#](4A0080)Ritual of Culling[#]() silently destroys all living things within its domain, channeling their life force into your [#](8B0000)Ara Vitae[#]()."
                + "\\\n\\\nConstruct this circle using a [#](8B0000)Ritual Diviner [Dusk][#]().");

        this.page("costs", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Costs and Range");
        this.pageText("[#](B8860B)Activation:[#]() 50,000 EV"
                + "\\\n[#](B8860B)Refresh:[#]() 75 EV per entity"
                + "\\\n[#](B8860B)Refresh Time:[#]() 25 ticks"
                + "\\\n\\\n[#](B8860B)Effect Range:[#]() 21x21x10 blocks (configurable)"
                + "\\\n[#](B8860B)Horizontal:[#]() 10 blocks radius"
                + "\\\n[#](B8860B)Vertical:[#]() 10 blocks upward from one block above the ritual stone"
                + "\\\n[#](B8860B)Altar Range:[#]() Searches 11x21x11 for an [#](8B0000)Ara Vitae[#]()."
                + "\\\n\\\nEV per kill varies by entity type and is datapack-customizable. Works on all non-boss entities by default.");

        this.page("boss", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Boss Killing");
        this.pageText("With sufficient [#](4A0080)Spiritus Nihilum[#](), even boss entities bow to the culling."
                + "\n\n- Requires 99+ [#](4A0080)Spiritus Nihilum[#]() in the area"
                + "\n\n- Incurs additional EV cost (configurable)"
                + "\n\n- Makes otherwise-invulnerable bosses vulnerable"
                + "\\\n\\\nEntities tagged [#](8B0000)animusnv:disallow_culling[#]() are protected from this ritual regardless of [#](4A0080)Spiritus[#]() levels.");

        this.page("will", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Spiritus and TNT");
        this.pageText("The ritual generates [#](4A0080)Spiritus Nihilum[#]() from kills at a 3%% chance per cycle, up to 100 [#](4A0080)Spiritus[#]() in the area."
                + "\\\n\\\nIf configured, the ritual can also destroy [#](8B0000)Primed TNT[#]() entities, making it a potent defensive measure against explosive threats.");
    }

    @Override
    protected String entryName() {
        return "Ritual of Culling";
    }

    @Override
    protected String entryDescription() {
        return "Destroys all living things within range, harvesting their life force.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.WITHER_SKELETON_SKULL);
    }

    @Override
    protected String entryId() {
        return "culling";
    }
}
