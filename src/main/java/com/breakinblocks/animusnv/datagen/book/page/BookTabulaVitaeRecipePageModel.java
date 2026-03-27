package com.breakinblocks.animusnv.datagen.book.page;

import com.breakinblocks.neovitae.compat.modonomicon.NVPageTypes;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;

public class BookTabulaVitaeRecipePageModel extends BookRecipePageModel<BookTabulaVitaeRecipePageModel> {

    protected BookTabulaVitaeRecipePageModel() {
        super(NVPageTypes.TABULA_VITAE);
    }

    public static BookTabulaVitaeRecipePageModel create() {
        return new BookTabulaVitaeRecipePageModel();
    }
}
