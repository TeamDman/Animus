package com.teamdman.animus.blocks;

import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

/**
 * Blood Wood Log block
 * Original: 1.12.2 Material.WOOD based block
 * Ported: 1.20.1 RotatedPillarBlock for proper log behavior (can be rotated and stripped)
 */
public class BlockBloodWood extends RotatedPillarBlock {
    public BlockBloodWood() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0F)
            .sound(SoundType.WOOD)
        );
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
        if (action == ToolActions.AXE_STRIP && context.getItemInHand().canPerformAction(action)) {
            return AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        }
        return super.getToolModifiedState(state, context, action, simulate);
    }
}
