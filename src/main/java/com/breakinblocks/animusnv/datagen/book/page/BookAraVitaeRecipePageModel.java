package com.breakinblocks.animusnv.datagen.book.page;

import com.breakinblocks.neovitae.compat.modonomicon.NVPageTypes;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;

public class BookAraVitaeRecipePageModel extends BookRecipePageModel<BookAraVitaeRecipePageModel> {

    protected BookAraVitaeRecipePageModel() {
        super(NVPageTypes.ARA_VITAE);
    }

    public static BookAraVitaeRecipePageModel create() {
        return new BookAraVitaeRecipePageModel();
    }
}
