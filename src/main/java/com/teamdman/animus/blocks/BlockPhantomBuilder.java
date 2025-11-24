package com.teamdman.animus.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Phantom Builder block - temporary block that can be replaced with items
 * Similar to Blood Magic's phantom block but with custom builder logic
 */
public class BlockPhantomBuilder extends Block {

    public BlockPhantomBuilder() {
        super(BlockBehaviour.Properties.of()
            .strength(0.0F)
            .sound(SoundType.WOOL)
            .noCollission()
            .noOcclusion()
        );
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (heldItem.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            BlockState newState = block.defaultBlockState();

            level.setBlock(pos, newState, 3);

            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    // TODO: Add tile entity with timer to remove phantom blocks after timeout
    // This would require implementing a BlockEntity similar to Blood Magic's TilePhantomBlock
}
