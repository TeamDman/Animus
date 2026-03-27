package com.breakinblocks.animusnv.datagen.book.sigils;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.breakinblocks.animusnv.registry.AnimusItems;

public class SigilsCategory extends CategoryProvider {

    public SigilsCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[]{
                "__a_b_c_d_e__",
                "_____________",
                "__f_g_h_i_j__",
                "_____________",
                "__k_l_m_n_o__"
        };
    }

    @Override
    protected void generateEntries() {
        var builder = this.add(new SigilBuilderEntry(this).generate('a'));
        var chains = this.add(new SigilChainsEntry(this).generate('b'));
        var consumption = this.add(new SigilConsumptionEntry(this).generate('c'));
        var crimsonWill = this.add(new SigilCrimsonWillEntry(this).generate('d'));
        var equivalency = this.add(new SigilEquivalencyEntry(this).generate('e'));
        var freeSoul = this.add(new SigilFreeSoulEntry(this).generate('f'));
        var heavenlyWrath = this.add(new SigilHeavenlyWrathEntry(this).generate('g'));
        var leach = this.add(new SigilLeachEntry(this).generate('h'));
        var monk = this.add(new SigilMonkEntry(this).generate('i'));
        var remedium = this.add(new SigilRemediumEntry(this).generate('j'));
        var reparare = this.add(new SigilReparareEntry(this).generate('k'));
        var storm = this.add(new SigilStormEntry(this).generate('l'));
        var temporalDominance = this.add(new SigilTemporalDominanceEntry(this).generate('m'));
        var transposition = this.add(new SigilTranspositionEntry(this).generate('n'));
        var sigilCrimsonWillCompat = this.add(new SigilCrimsonWillCompatEntry(this).generate('o'));
    }

    @Override
    protected String categoryName() {
        return "Sigils";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(AnimusItems.SIGIL_STORM.get());
    }

    @Override
    public String categoryId() {
        return "sigils";
    }
}
