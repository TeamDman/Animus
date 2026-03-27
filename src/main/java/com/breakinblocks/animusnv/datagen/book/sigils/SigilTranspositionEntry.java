package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class SigilTranspositionEntry extends EntryProvider {

    public SigilTranspositionEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("reagent_recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "hellfire_forge/reagenttransposition")));

        this.page("array_recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath("animusnv", "array/sigil_transposition")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sigil of Transposition");
        this.pageText("Forged in an [#](8B0000)Alchemy Array[#]() from a [#](8B0000)Reagent: Transposition[#]() "
                + "and a [#](B8860B)Demon Slate[#](), this sigil moves blocks through space without "
                + "breaking them, and transports entities to bound Teleposers.");

        this.page("block_mode", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Block Transposition");
        this.pageText("At a cost of [#](4A0080)5,000 EV[#]() per transposition:\n\n"
                + "- Right-click a block to select it as the source (the sigil activates)\n\n"
                + "- Right-click another block's face to move the source block adjacent to it\n\n"
                + "- Right-click air to clear your selection\\\n\\\n"
                + "The sigil chunk-loads the source area during transposition for safety.");

        this.page("entity_mode", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Entity Teleportation");
        this.pageText("At a cost of [#](4A0080)2,000 EV[#]() per teleportation:\n\n"
                + "- Sneak + right-click a [#](8B0000)Teleposer[#]() to bind its location\n\n"
                + "- Attack an entity to teleport it 1 block above the bound Teleposer\\\n\\\n"
                + "[#](2E8B57)Players can only be teleported if they are sneaking. The Teleposer must "
                + "still exist at the bound location.[#]()");

        this.page("restrictions", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Restrictions");
        this.pageText("The sigil respects chunk protection (FTB Chunks, etc.) and cannot move blocks "
                + "tagged [#](4A0080)forge:relocation_not_supported[#](). The destination must be air.\\\n\\\n"
                + "Perfect for moving complex machinery, filled chests, spawners, and tile entities "
                + ", or for teleporting mobs to farms and creating trap systems.");
    }

    @Override
    protected String entryName() {
        return "Sigil of Transposition";
    }

    @Override
    protected String entryDescription() {
        return "Relocates blocks without breaking them and teleports entities to Teleposers.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_TRANSPOSITION.get());
    }

    @Override
    protected String entryId() {
        return "sigil_transposition";
    }
}
