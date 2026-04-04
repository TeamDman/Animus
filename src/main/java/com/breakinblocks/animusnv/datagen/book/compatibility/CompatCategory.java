package com.breakinblocks.animusnv.datagen.book.compatibility;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class CompatCategory extends CategoryProvider {

    public CompatCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[]{
                "__a_b_c_d__",
                "__e_f_g_h__",
                "__i________"
        };
    }

    @Override
    protected void generateEntries() {
        // Ars Nouveau
        var arcaneRune = this.add(new ArcaneRuneEntry(this).generate('a'));
        var runeUnleashedNature = this.add(new RuneUnleashedNatureEntry(this).generate('b'));
        var magi = this.add(new MagiEntry(this).generate('c'));
        // Iron's Spells
        var bloodInfusedSpellbook = this.add(new BloodInfusedSpellbookEntry(this).generate('d'));
        var sanguineScrolls = this.add(new SanguineScrollsEntry(this).generate('e'));
        var ironHeart = this.add(new IronHeartEntry(this).generate('f'));
        // Botania
        var manasteelSoul = this.add(new ManasteelSoulEntry(this).generate('g'));
        // Malum
        var soulStainedBlood = this.add(new SoulStainedBloodEntry(this).generate('h'));
        // EvilCraft
        var sanguineRectifier = this.add(new SanguineRectifierEntry(this).generate('i'));
    }

    @Override
    protected String categoryName() {
        return "Compatibility";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.ENDER_PEARL);
    }

    @Override
    public String categoryId() {
        return "compatibility";
    }
}
