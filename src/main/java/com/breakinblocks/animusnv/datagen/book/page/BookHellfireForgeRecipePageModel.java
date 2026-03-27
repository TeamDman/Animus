package com.breakinblocks.animusnv.datagen.book.page;

import com.breakinblocks.neovitae.compat.modonomicon.NVPageTypes;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;

public class BookHellfireForgeRecipePageModel extends BookRecipePageModel<BookHellfireForgeRecipePageModel> {

    protected BookHellfireForgeRecipePageModel() {
        super(NVPageTypes.HELLFIRE_FORGE);
    }

    public static BookHellfireForgeRecipePageModel create() {
        return new BookHellfireForgeRecipePageModel();
    }
}
