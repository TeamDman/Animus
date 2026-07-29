package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.resources.Identifier;

public class SentientShieldEntry extends EntryProvider {

    public SentientShieldEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "hellfire_forge/sentient_shield")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sentient Shield");
        this.pageText("A demonic shield infused with [#](4A0080)Spiritus[#](), forged in the "
                + "[#](8B0000)Tabula Vitae[#](). It boasts 4x the durability of a normal shield "
                + "(1,344 vs 336) and grants special effects when blocking based on the type of "
                + "Spiritus available to you.");

        this.page("will_effects", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Spiritus Effects");
        this.pageText("The shield automatically detects your highest [#](4A0080)Spiritus[#]() "
                + "type and adapts:\n\n"
                + "- [#](8B0000)Raw Spiritus:[#]() Standard blocking\n\n"
                + "- [#](4A0080)Invictus:[#]() Increased knockback resistance\n\n"
                + "- [#](2E8B57)Ruina:[#]() Attackers take damage\n\n"
                + "- [#](B8860B)Vindicta:[#]() Reflect damage back at attackers");

        this.page("spiritus_bonus", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Spiritus Gain Bonus");
        this.pageText("While equipped in your main or offhand, the Sentient Shield increases the "
                + "amount of [#](4A0080)Spiritus[#]() you gain from all sources by "
                + "[#](2E8B57)+30%%[#]().\\\n\\\n"
                + "[#](4A0080)A shield that hungers for more.[#]()");
    }

    @Override
    protected String entryName() {
        return "Sentient Shield";
    }

    @Override
    protected String entryDescription() {
        return "A demonic shield with 4x durability and Spiritus-aspected blocking effects.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.SENTIENT_SHIELD.get());
    }

    @Override
    protected String entryId() {
        return "sentient_shield";
    }
}
