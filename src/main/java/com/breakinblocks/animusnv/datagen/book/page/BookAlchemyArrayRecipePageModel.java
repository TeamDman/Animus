package com.breakinblocks.animusnv.datagen.book.page;

import com.breakinblocks.neovitae.compat.modonomicon.NVPageTypes;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;

public class BookAlchemyArrayRecipePageModel extends BookRecipePageModel<BookAlchemyArrayRecipePageModel> {

    protected BookAlchemyArrayRecipePageModel() {
        super(NVPageTypes.ALCHEMY_ARRAY);
    }

    public static BookAlchemyArrayRecipePageModel create() {
        return new BookAlchemyArrayRecipePageModel();
    }
}
