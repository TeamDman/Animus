package com.breakinblocks.animusnv.datagen.book.imperfect;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class ImperfectRitualsCategory extends CategoryProvider {

    public ImperfectRitualsCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[]{
                "__a_b_c_d__",
                "__e_f_g_h__"
        };
    }

    @Override
    protected void generateEntries() {
        var boundlessSkies = this.add(new BoundlessSkiesEntry(this).generate('a'));
        var clearSkies = this.add(new ClearSkiesEntry(this).generate('b'));
        var enhancement = this.add(new EnhancementEntry(this).generate('c'));
        var hunger = this.add(new HungerEntry(this).generate('d'));
        var neptuneBlessing = this.add(new NeptuneBlessingEntry(this).generate('e'));
        var reduction = this.add(new ReductionEntry(this).generate('f'));
        var regression = this.add(new RegressionEntry(this).generate('g'));
        var warden = this.add(new WardenEntry(this).generate('h'));
    }

    @Override
    protected String categoryName() {
        return "Imperfect Rituals";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.STONE);
    }

    @Override
    public String categoryId() {
        return "imperfect_rituals";
    }
}
