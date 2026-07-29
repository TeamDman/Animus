package com.breakinblocks.animusnv.datagen.book.page;

import com.breakinblocks.neovitae.compat.modonomicon.NVPageTypes;
import com.breakinblocks.neovitae.compat.modonomicon.page.BookTabulaVitaeRecipePage;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;

public class BookTabulaVitaeRecipePageModel extends BookRecipePageModel<BookTabulaVitaeRecipePageModel> {

    protected BookTabulaVitaeRecipePageModel() {
        super(NVPageTypes.TABULA_VITAE);
    }

    public static BookTabulaVitaeRecipePageModel create() {
        return new BookTabulaVitaeRecipePageModel();
    }

    @Override
    protected BookPage createPage(BookRecipePage.JsonDataHolder data) {
        return new BookTabulaVitaeRecipePage(data);
    }
}
