package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.client.BuilderSigilClientHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;

/**
 * Sigil of the Phantom Builder - places blocks from the offhand.
 * When activated (toggleable), removes right-click delay for fast building.
 * Right-click to place blocks from offhand in front of player.
 * Right-click on a block while sneaking to fill area.
 */
public record BuilderSigilEffect() implements ISigilEffect {
    public static final MapCodec<BuilderSigilEffect> CODEC = MapCodec.unit(BuilderSigilEffect::new);

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean isToggleable() {
        return true;
    }

    @Override
    public void activeTick(Level level, Player player, ItemStack stack, int itemSlot, boolean isSelected) {
        if (level.isClientSide() && FMLEnvironment.getDist() == Dist.CLIENT) {
            BuilderSigilClientHelper.resetRightClickDelay();
        }
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return false;
        }

        // Determine which hand has the sigil and get the other hand's item
        InteractionHand sigilHand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack buildStack = sigilHand == InteractionHand.MAIN_HAND
                ? player.getOffhandItem()
                : player.getMainHandItem();

        if (buildStack.isEmpty() || !(buildStack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        BlockPos placePos = player.blockPosition()
                .relative(player.getDirection(), 2)
                .above();

        if (!level.isEmptyBlock(placePos)) {
            return false;
        }

        // Place the block
        Block block = blockItem.getBlock();
        BlockState state = block.defaultBlockState();
        level.setBlock(placePos, state, 3);

        // Consume item
        buildStack.shrink(1);
        if (buildStack.isEmpty() && sigilHand == InteractionHand.MAIN_HAND) {
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }

        return true;
    }

    @Override
    public boolean useOnBlock(Level level, Player player, ItemStack stack, BlockPos blockPos, Direction side, Vec3 hitVec) {
        if (level.isClientSide()) {
            return false;
        }

        // Determine which hand has the sigil and get the other hand's item
        InteractionHand sigilHand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack buildStack = sigilHand == InteractionHand.MAIN_HAND
                ? player.getOffhandItem()
                : player.getMainHandItem();

        if (buildStack.isEmpty() || !(buildStack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        Block block = blockItem.getBlock();

        if (player.isShiftKeyDown()) {
            // Fill area
            int radius = (int) Math.sqrt(AnimusConfig.sigils.builderRange.get());
            boolean placedAny = false;

            for (int r = 1; r <= radius; r++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos placePos = switch (side.getAxis()) {
                            case X -> blockPos.offset(0, x, z);
                            case Y -> blockPos.offset(x, 0, z);
                            case Z -> blockPos.offset(x, z, 0);
                        };

                        if (level.isEmptyBlock(placePos) && !buildStack.isEmpty()) {
                            BlockState state = block.defaultBlockState();
                            level.setBlock(placePos, state, 3);

                            buildStack.shrink(1);
                            if (buildStack.isEmpty() && sigilHand == InteractionHand.MAIN_HAND) {
                                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                            }
                            placedAny = true;
                        }
                    }
                }
            }

            return placedAny;
        } else {
            // Place single block along face direction
            BlockPos placePos = blockPos;
            int distance = 0;

            do {
                placePos = placePos.relative(side.getOpposite());
                distance++;
                if (distance > AnimusConfig.sigils.builderRange.get()) {
                    return false;
                }
            } while (!level.isEmptyBlock(placePos) && placePos.getY() > level.getMinY());

            if (level.isEmptyBlock(placePos) && !buildStack.isEmpty()) {
                BlockState state = block.defaultBlockState();
                level.setBlock(placePos, state, 3);

                buildStack.shrink(1);
                if (buildStack.isEmpty() && sigilHand == InteractionHand.MAIN_HAND) {
                    player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }

                return true;
            }
        }

        return false;
    }
}
