package com.teamdman.animus.blocks;

import com.teamdman.animus.worldgen.AnimusConfiguredFeatures;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Optional;

/**
 * Blood Sapling - grows into blood trees with a blood core at the top
 */
public class BlockBloodSapling extends SaplingBlock {

    // TreeGrower for blood trees - uses our configured feature
    private static final TreeGrower BLOOD_TREE_GROWER = new TreeGrower(
        "blood_tree",
        Optional.empty(), // no mega tree
        Optional.of(AnimusConfiguredFeatures.BLOOD_TREE), // regular tree
        Optional.empty()  // no flower variant
    );

    public BlockBloodSapling() {
        super(BLOOD_TREE_GROWER, BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .strength(0.0F)
            .sound(SoundType.GRASS)
        );
    }
}
