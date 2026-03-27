package com.breakinblocks.animusnv.datagen.book.intro;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class IntroCategory extends CategoryProvider {

    public IntroCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[]{
                "___a___"
        };
    }

    @Override
    protected void generateEntries() {
        var welcome = this.add(new WelcomeEntry(this).generate('a'));
    }

    @Override
    protected String categoryName() {
        return "Welcome";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.BOOK);
    }

    @Override
    public String categoryId() {
        return "intro";
    }
}
