package com.breakinblocks.animusnv.datagen.book.items;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.breakinblocks.animusnv.registry.AnimusItems;

public class ItemsCategory extends CategoryProvider {

    public ItemsCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[]{
                "__a_b_c_d__",
                "___________",
                "__e_f_g____"
        };
    }

    @Override
    protected void generateEntries() {
        var bloodApple = this.add(new BloodAppleEntry(this).generate('a'));
        var fragmentHealing = this.add(new FragmentHealingEntry(this).generate('b'));
        var mobSoul = this.add(new MobSoulEntry(this).generate('c'));
        var keyBinding = this.add(new KeyBindingEntry(this).generate('d'));
        var activationCrystal = this.add(new ActivationCrystalFragileEntry(this).generate('e'));
        var spears = this.add(new SpearsEntry(this).generate('f'));
        var sentientShield = this.add(new SentientShieldEntry(this).generate('g'));
    }

    @Override
    protected String categoryName() {
        return "Items";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(AnimusItems.SPEAR_BOUND.get());
    }

    @Override
    public String categoryId() {
        return "items";
    }
}
