package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class RuneUnleashedNatureEntry extends EntryProvider {

    public RuneUnleashedNatureEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Rune of Unleashed Nature");
        this.pageText("The [#](8B0000)Rune of Unleashed Nature[#]() is a hybrid altar component "
                + "that channels Botania's mana into the altar system. Unlike the Arcane "
                + "Rune which requires Source, this rune draws from an internal mana buffer "
                + "refilled by any Botania mana-transferring mechanism.\\\n\\\n"
                + "[#](4A0080)A living rune that responds to the flow of mana, nature's "
                + "power harnessed for the blood mage's altar.[#]()");

        this.page("crafting", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Crafting & Placement");
        this.pageText("Crafted using a [#](8B0000)Blank Rune[#]() surrounded by Botania's "
                + "[#](8B0000)Livingwood[#]() and [#](8B0000)Manasteel[#](). This creates a "
                + "living altar rune that responds to mana flow.\\\n\\\n"
                + "Place this rune in your [#](8B0000)Ara Vitae[#]() structure like any other "
                + "rune. It stores mana internally and can be charged by pointing a "
                + "Mana Spreader at it or placing it near a Mana Pool.");

        this.page("passive", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Passive Bonuses");
        this.pageText("The Rune of Unleashed Nature provides bonuses even when empty:\\\n\\\n"
                + "[#](8B0000)Always Active:[#]()\n\n"
                + "- Capacity Bonus: 135%% of a Capacity Rune's effect\n\n"
                + "- Orb Bonus: 67.5%% of a Rune of the Orb's effect, increasing "
                + "Anima storage\\\n\\\n"
                + "These passive bonuses make it valuable even without mana.");

        this.page("charged", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Charged Bonus");
        this.pageText("When the rune has mana in its internal buffer:\\\n\\\n"
                + "[#](4A0080)Mana-Powered:[#]() Acceleration bonus speeds up altar crafting "
                + "and EV generation. Consumes 10 mana per second while active.\\\n\\\n"
                + "The acceleration effect stacks with other speed-enhancing runes, making "
                + "high-tier crafting faster.");

        this.page("charging", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Charging Methods");
        this.pageText("Supply mana to the rune using any Botania mechanism:\n\n"
                + "- Point a [#](8B0000)Mana Spreader[#]() at the rune\n\n"
                + "- Place it adjacent to a [#](8B0000)Mana Pool[#]()\n\n"
                + "- Use a [#](8B0000)Mana Tablet[#]() or other mana items\\\n\\\n"
                + "[#](2E8B57)Consider the consumption rate (10 mana/second per rune) when "
                + "planning your mana infrastructure for multiple runes. "
                + "Requires Botania to be installed.[#]()");
    }

    @Override
    protected String entryName() {
        return "Rune of Unleashed Nature";
    }

    @Override
    protected String entryDescription() {
        return "A hybrid altar rune powered by Botania mana. Requires Botania.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.OAK_SAPLING);
    }

    @Override
    protected String entryId() {
        return "rune_unleashed_nature";
    }
}
