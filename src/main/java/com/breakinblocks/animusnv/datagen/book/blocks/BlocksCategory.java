package com.breakinblocks.animusnv.datagen.book.blocks;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.breakinblocks.animusnv.registry.AnimusBlocks;

public class BlocksCategory extends CategoryProvider {

    public BlocksCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[]{
                "__a_b__",
                "__c_d__",
                "__e_f__",
                "____g__"
        };
    }

    @Override
    protected void generateEntries() {
        var antilife = this.add(new AntiLifeEntry(this).generate('a'));
        var antilifeFluid = this.add(new AntiLifeFluidEntry(this).generate('b'));
        var crystallizedSpiritus = this.add(new CrystallizedSpiritusBlockEntry(this).generate('c'));
        var livingTerra = this.add(new LivingTerraEntry(this).generate('d'));
        var willfulStone = this.add(new WillfulStoneEntry(this).generate('e'));
        var bloodTrees = this.add(new BloodTreesEntry(this).generate('f'));
        var decorativeBlocks = this.add(new DecorativeBlocksEntry(this).generate('g'));
    }

    @Override
    protected String categoryName() {
        return "Blocks";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(AnimusBlocks.BLOCK_ANTILIFE.get().asItem());
    }

    @Override
    public String categoryId() {
        return "blocks";
    }
}
