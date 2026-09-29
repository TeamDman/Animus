package com.breakinblocks.animusnv.compat.dynamictrees;

import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BloodCoreGenFeature extends GenFeature {

    public static final ConfigurationProperty<Integer> MIN_HEIGHT = ConfigurationProperty.integer("min_height");

    public BloodCoreGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(MIN_HEIGHT, MAX_HEIGHT, PLACE_CHANCE);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
            .with(MIN_HEIGHT, 3)
            .with(MAX_HEIGHT, 32)
            .with(PLACE_CHANCE, 0.1F);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        return placeCore(configuration, context.level(), context.pos());
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.random().nextFloat() > configuration.get(PLACE_CHANCE)) {
            return false;
        }
        return placeCore(configuration, context.level(), context.pos());
    }

    private boolean placeCore(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos rootPos) {
        Block core = AnimusBlocks.BLOCK_BLOOD_CORE.get();
        int maxHeight = configuration.get(MAX_HEIGHT);

        BlockPos top = null;
        for (int y = 1; y <= maxHeight; y++) {
            BlockPos pos = rootPos.above(y);
            BlockState state = level.getBlockState(pos);
            if (state.is(core)) {
                return false;
            }
            if (!TreeHelper.isBranch(state)) {
                break;
            }
            top = pos;
        }

        if (top == null || top.getY() - rootPos.getY() < configuration.get(MIN_HEIGHT)) {
            return false;
        }

        BlockPos corePos = top.above();
        BlockState existing = level.getBlockState(corePos);
        if (!existing.isAir() && !(existing.getBlock() instanceof DynamicLeavesBlock)) {
            return false;
        }

        return level.setBlock(corePos, core.defaultBlockState(), 3);
    }

    public static int connectionRadius(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (side != Direction.UP) {
            return 0;
        }
        BlockState below = level.getBlockState(pos.below());
        BranchBlock branch = TreeHelper.getBranch(below);
        return branch != null ? Mth.clamp(branch.getRadius(below), 1, 8) : 1;
    }
}
