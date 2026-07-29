package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;

public class WillfulStoneEntry extends EntryProvider {

    public WillfulStoneEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "hellfire_forge/willful_stone")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Willful Stone");
        this.pageText("Stone saturated with [#](4A0080)Spiritus[#]() takes on a strange, "
                + "almost luminous quality, as though the demonic energy has fused with the mineral itself. "
                + "[#](8B0000)Willful Stone[#]() is a decorative building material prized by "
                + "Vitaemancers who wish their sanctums to reflect the power within.\\\n\\\n"
                + "[#](4A0080)Once placed, Willful Stone can only be broken by the Vitaemancer who laid it. "
                + "Others who attempt to mine it will find it unyielding. To grant an ally access, "
                + "craft a [#](8B0000)Key of Binding[#]() and bind it to yourself, then share "
                + "the key with your trusted companion.[#]()");

        this.page("crafting", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Forging");
        this.pageText("The base [#](8B0000)Willful Stone[#]() is forged in the [#](8B0000)Tabula Vitae[#]() "
                + "from stone, a [#](8B0000)Tabula Robur[#](), and redstone dust. "
                + "The Forge yields [#](B8860B)4 blocks[#]() per craft.\\\n\\\n"
                + "[#](2E8B57)Search for Willful Stone in JEI to see the exact recipe.[#]()");

        this.page("dyeing", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Colour Variants");
        this.pageText("Once you have the base grey [#](8B0000)Willful Stone[#](), surround any dye "
                + "with eight Willful Stones in a crafting table to produce [#](B8860B)8 coloured variants[#](). "
                + "All sixteen dye colours are supported.\\\n\\\n"
                + "[#](2E8B57)You can re-dye Willful Stone of any colour. Simply use a different "
                + "dye in the same 8-around-1 pattern.[#]()");
    }

    @Override
    protected String entryName() {
        return "Willful Stone";
    }

    @Override
    protected String entryDescription() {
        return "Spiritus-infused decorative stone in sixteen colours.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusBlocks.BLOCK_WILLFUL_STONE.get().asItem());
    }

    @Override
    protected String entryId() {
        return "willful_stone";
    }
}
