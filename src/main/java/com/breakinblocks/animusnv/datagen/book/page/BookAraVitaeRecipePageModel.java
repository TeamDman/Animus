package com.breakinblocks.animusnv.datagen.book.page;

import com.breakinblocks.neovitae.compat.modonomicon.NVPageTypes;
import com.breakinblocks.neovitae.compat.modonomicon.page.BookAraVitaeRecipePage;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;

public class BookAraVitaeRecipePageModel extends BookRecipePageModel<BookAraVitaeRecipePageModel> {

    protected BookAraVitaeRecipePageModel() {
        super(NVPageTypes.ARA_VITAE);
    }

    public static BookAraVitaeRecipePageModel create() {
        return new BookAraVitaeRecipePageModel();
    }

    @Override
    protected BookPage createPage(BookRecipePage.JsonDataHolder data) {
        return new BookAraVitaeRecipePage(data);
    }
}
