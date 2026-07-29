package com.breakinblocks.animusnv.datagen.book.page;

import com.breakinblocks.neovitae.compat.modonomicon.NVPageTypes;
import com.breakinblocks.neovitae.compat.modonomicon.page.BookAlchemyArrayRecipePage;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;

public class BookAlchemyArrayRecipePageModel extends BookRecipePageModel<BookAlchemyArrayRecipePageModel> {

    protected BookAlchemyArrayRecipePageModel() {
        super(NVPageTypes.ALCHEMY_ARRAY);
    }

    public static BookAlchemyArrayRecipePageModel create() {
        return new BookAlchemyArrayRecipePageModel();
    }

    @Override
    protected BookPage createPage(BookRecipePage.JsonDataHolder data) {
        return new BookAlchemyArrayRecipePage(data);
    }
}
