package com.breakinblocks.animusnv.datagen.book;

import com.klikli_dev.modonomicon.api.datagen.ModonomiconLanguageProvider;
import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookModel;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.datagen.book.blocks.BlocksCategory;
import com.breakinblocks.animusnv.datagen.book.compatibility.CompatCategory;
import com.breakinblocks.animusnv.datagen.book.imperfect.ImperfectRitualsCategory;
import com.breakinblocks.animusnv.datagen.book.intro.IntroCategory;
import com.breakinblocks.animusnv.datagen.book.items.ItemsCategory;
import com.breakinblocks.animusnv.datagen.book.rituals.RitualsCategory;
import com.breakinblocks.animusnv.datagen.book.sigils.SigilsCategory;
import net.minecraft.resources.ResourceLocation;

public class AnimusBookProvider extends SingleBookSubProvider {

    public AnimusBookProvider(ModonomiconLanguageProvider lang) {
        super("guide", Constants.Mod.MODID, lang);
    }

    @Override
    protected void registerDefaultMacros() {
    }

    @Override
    protected void generateCategories() {
        this.add(new IntroCategory(this).generate());
        this.add(new BlocksCategory(this).generate());
        this.add(new ItemsCategory(this).generate());
        this.add(new SigilsCategory(this).generate());
        this.add(new RitualsCategory(this).generate());
        this.add(new ImperfectRitualsCategory(this).generate());
        this.add(new CompatCategory(this).generate());
    }

    @Override
    protected BookModel additionalSetup(BookModel book) {
        return super.additionalSetup(book)
                .withGenerateBookItem(false)
                .withCustomBookItem(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "guide_book"))
                .withModel(ResourceLocation.fromNamespaceAndPath("modonomicon", "modonomicon_red"))
                .withCreativeTab(ResourceLocation.fromNamespaceAndPath("neovitae", "main"));
    }

    @Override
    protected String bookName() {
        return "Codex Animus";
    }

    @Override
    protected String bookTooltip() {
        return "Supplementary Grimoire of Vitaemancy";
    }
}
