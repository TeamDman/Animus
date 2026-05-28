package com.breakinblocks.animusnv.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.breakinblocks.animusnv.registry.AnimusItems;

public class RitualsCategory extends CategoryProvider {

    public RitualsCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[]{
                "__a_b_c_d_e_f______",
                "____________________",
                "__g_h_i_j_k________",
                "____________________",
                "__m_n_o_p_q________"
        };
    }

    @Override
    protected void generateEntries() {
        // Basic rituals (no dusk stones)
        var animalLuring = this.add(new AnimalLuringEntry(this).generate('a'));
        var naturesLeach = this.add(new NaturesLeachEntry(this).generate('b'));
        var entropy = this.add(new EntropyEntry(this).generate('c'));
        var sol = this.add(new SolEntry(this).generate('d'));
        var luna = this.add(new LunaEntry(this).generate('e'));
        var steadfastHeart = this.add(new SteadfastHeartEntry(this).generate('f'));

        var serenity = this.add(new SerenityEntry(this).generate('g'));
        var noliteIgnem = this.add(new NoliteIgnemEntry(this).generate('h'));
        var siphon = this.add(new SiphonEntry(this).generate('i'));
        var relentlessTides = this.add(new RelentlessTidesEntry(this).generate('j'));
        var reparare = this.add(new ReparareEntry(this).generate('k'));

        var ritualArcaneMastery = this.add(new RitualArcaneMasteryEntry(this).generate('m'));
        var ritualFloralSupremacy = this.add(new RitualFloralSupremacyEntry(this).generate('n'));

        // Dusk rituals (uses dusk stones)
        var culling = this.add(new CullingEntry(this).generate('o'));
        var persistence = this.add(new PersistenceEntry(this).generate('p'));
        var unmaking = this.add(new UnmakingEntry(this).generate('q'));
    }

    @Override
    protected String categoryName() {
        return "Rituals";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(AnimusItems.ACTIVATION_CRYSTAL_FRAGILE.get());
    }

    @Override
    public String categoryId() {
        return "rituals";
    }
}
