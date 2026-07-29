package com.breakinblocks.animusnv.datagen.book.page;

import com.breakinblocks.neovitae.compat.modonomicon.NVPageTypes;
import com.breakinblocks.neovitae.compat.modonomicon.page.BookHellfireForgeRecipePage;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;

public class BookHellfireForgeRecipePageModel extends BookRecipePageModel<BookHellfireForgeRecipePageModel> {

    protected BookHellfireForgeRecipePageModel() {
        super(NVPageTypes.HELLFIRE_FORGE);
    }

    public static BookHellfireForgeRecipePageModel create() {
        return new BookHellfireForgeRecipePageModel();
    }

    @Override
    protected BookPage createPage(BookRecipePage.JsonDataHolder data) {
        return new BookHellfireForgeRecipePage(data);
    }
}
