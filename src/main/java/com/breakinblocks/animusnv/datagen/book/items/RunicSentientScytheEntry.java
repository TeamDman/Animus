package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.animusnv.datagen.book.page.BookAlchemyArrayRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.resources.Identifier;

public class RunicSentientScytheEntry extends EntryProvider {

    public RunicSentientScytheEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("recipe", () -> BookAlchemyArrayRecipePageModel.create()
                .withRecipeId1(Identifier.fromNamespaceAndPath("animusnv", "array/runic_sentient_scythe")));

        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Runic Sentient Scythe");
        this.pageText("The [#](8B0000)Runic Sentient Scythe[#]() combines the demonic power of "
                + "the Sentient Scythe with runic enhancements and Malum soul harvesting. "
                + "This deadly weapon attacks [#](8B0000)30%% faster[#]() than its predecessor "
                + "while reaping souls for the spirit altar.\\\n\\\n"
                + "[#](4A0080)Like the Sentient Scythe, this weapon scales with Spiritus in "
                + "the chunk. It inherits all Spiritus effects while adding Malum's spirit "
                + "harvesting capabilities.[#]()");

        this.page("speed", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Enhanced Speed");
        this.pageText("The runic modifications grant a [#](8B0000)30%% faster attack speed[#]() "
                + "compared to the base Sentient Scythe. This allows for more rapid strikes "
                + "and better Spiritus generation through combat.\\\n\\\n"
                + "[#](2E8B57)The faster attack speed synergizes well with Malum's soul "
                + "harvesting, allowing you to reap more spirits per encounter.[#]()");

        this.page("harvesting", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Soul Harvesting");
        this.pageText("When [#](8B0000)Malum[#]() is installed, killing mobs with this scythe "
                + "harvests their spirits directly:\n\n"
                + "- Spirits drop with native Malum-scythe behaviour\n\n"
                + "- Works with all spirit types\n\n"
                + "- No additional soul data required\n\n"
                + "- Compatible with spirit pouches\\\n\\\n"
                + "This makes it an excellent tool for both Spiritus farming and "
                + "Malum spirit collection.");

        this.page("crafting", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Crafting");
        this.pageText("The Runic Sentient Scythe is crafted at the [#](8B0000)Alchemy Array[#]() "
                + "using:\n\n"
                + "- [#](8B0000)Sentient Scythe[#]()\n\n"
                + "- [#](8B0000)Malum Spirit Components[#]()\n\n"
                + "- [#](4A0080)Essentia Vitae[#]()\\\n\\\n"
                + "The crafting process infuses Malum's spirit magic into the Vitaemancy "
                + "weapon. Consult the recipe for exact requirements.\\\n\\\n"
                + "[#](2E8B57)Requires Malum to be installed.[#]()");
    }

    @Override
    protected String entryName() {
        return "Runic Sentient Scythe";
    }

    @Override
    protected String entryDescription() {
        return "A faster Sentient Scythe with Malum spirit harvesting. Requires Malum.";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(AnimusItems.RUNIC_SENTIENT_SCYTHE.get());
    }

    @Override
    protected String entryId() {
        return "runic_sentient_scythe";
    }
}
