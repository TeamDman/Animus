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
                "__i_j_k_l__",
                "__m_n_o____"
        };
    }

    @Override
    protected void generateEntries() {
        // Ars Nouveau
        var arcaneRune = this.add(new ArcaneRuneEntry(this).generate('a'));
        var livingArmorArs = this.add(new LivingArmorArsNouveauEntry(this).generate('b'));
        var magi = this.add(new MagiEntry(this).generate('c'));
        // Iron's Spells
        var bloodInfusedSpellbook = this.add(new BloodInfusedSpellbookEntry(this).generate('d'));
        var livingArmorSpells = this.add(new LivingArmorSpellsEntry(this).generate('e'));
        var ritualArcaneMastery = this.add(new RitualArcaneMasteryEntry(this).generate('f'));
        var sanguineScrolls = this.add(new SanguineScrollsEntry(this).generate('g'));
        var sigilCrimsonWill = this.add(new SigilCrimsonWillCompatEntry(this).generate('h'));
        var ironHeart = this.add(new IronHeartEntry(this).generate('i'));
        // Botania
        var runeUnleashedNature = this.add(new RuneUnleashedNatureEntry(this).generate('j'));
        var ritualFloralSupremacy = this.add(new RitualFloralSupremacyEntry(this).generate('k'));
        var manasteelSoul = this.add(new ManasteelSoulEntry(this).generate('l'));
        // Malum
        var runicSentientScythe = this.add(new RunicSentientScytheEntry(this).generate('m'));
        var handOfDeath = this.add(new HandOfDeathEntry(this).generate('n'));
        var soulStainedBlood = this.add(new SoulStainedBloodEntry(this).generate('o'));
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
