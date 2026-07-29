package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookHellfireForgeRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.resources.Identifier;

public class HandOfDeathEntry extends EntryProvider {

    public HandOfDeathEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookHellfireForgeRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "hellfire_forge/hand_of_death")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Hand of Death");
        this.pageText("The [#](8B0000)Hand of Death[#]() is the final evolution of the "
                + "Runic Sentient Scythe. Forged with demon steel and infused with "
                + "deathly power, this weapon executes weakened foes and heals its "
                + "wielder through the lifeforce of the slain.\\\n\\\n"
                + "[#](4A0080)It combines every feature of the Runic Sentient Scythe with "
                + "lifesteal and an execute mechanic.[#]()");

        this.page("lifesteal", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Lifesteal");
        this.pageText("Every strike with the Hand of Death heals you for [#](8B0000)20%%[#]() "
                + "of the damage dealt:\n\n"
                + "- Minimum heal of 1 health\n\n"
                + "- Works on all damage types\n\n"
                + "- Stacks with other healing\n\n"
                + "- No cooldown\\\n\\\n"
                + "[#](2E8B57)Combined with the weapon's high damage, this provides "
                + "substantial sustain in prolonged fights.[#]()");

        this.page("execute", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Execute");
        this.pageText("When an enemy falls below [#](8B0000)15%%[#]() health, the Hand of Death "
                + "can instantly execute them:\n\n"
                + "- Triggers on your next hit\n\n"
                + "- Bypasses remaining health\n\n"
                + "- Full soul and spirit drops\\\n\\\n"
                + "This makes the weapon devastating against high-health targets and bosses.");

        this.page("inherited", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Inherited Powers");
        this.pageText("The Hand of Death retains all features from the "
                + "[#](8B0000)Runic Sentient Scythe[#]():\n\n"
                + "- [#](4A0080)+30%% attack speed[#]()\n\n"
                + "- Full Spiritus scaling\n\n"
                + "- Malum spirit harvesting\n\n"
                + "- All sentient weapon effects\\\n\\\n"
                + "[#](8B0000)+14 bonus base damage[#]() over the Runic Sentient Scythe, on top of "
                + "lifesteal and execute, makes this the highest-damage melee weapon Animus ships.\\\n\\\n"
                + "[#](2E8B57)Requires Malum to be installed.[#]()");

        this.page("crafting", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Crafting");
        this.pageText("The Hand of Death requires advanced materials:\n\n"
                + "- [#](8B0000)Runic Sentient Scythe[#]()\n\n"
                + "- [#](8B0000)Demon Forged Steel[#]()\n\n"
                + "- Significant [#](4A0080)Essentia Vitae[#]()\\\n\\\n"
                + "This is an end-game weapon requiring mastery of both Vitaemancy and "
                + "Malum systems. Consult the recipe for exact requirements.");
    }

    @Override
    protected String entryName() {
        return "Hand of Death";
    }

    @Override
    protected String entryDescription() {
        return "Demonic weapon with lifesteal and an execute mechanic. Requires Malum.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.HAND_OF_DEATH.get());
    }

    @Override
    protected String entryId() {
        return "hand_of_death";
    }
}
