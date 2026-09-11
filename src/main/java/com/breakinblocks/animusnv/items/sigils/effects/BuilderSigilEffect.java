package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.client.BuilderSigilClientHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        if (level.isClientSide() && FMLEnvironment.dist == Dist.CLIENT) {
            BuilderSigilClientHelper.resetRightClickDelay();
        }
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return false;
        }

        // Determine which hand has the sigil and get the other hand's item
        InteractionHand sigilHand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack buildStack = sigilHand == InteractionHand.MAIN_HAND
                ? player.getOffhandItem()
                : player.getMainHandItem();

        if (buildStack.isEmpty() || !(buildStack.getItem() instanceof BlockItem)) {
            return false;
        }

        BlockPos placePos = player.blockPosition()
                .relative(player.getDirection(), 2)
                .above();

        if (!level.isEmptyBlock(placePos)) {
            return false;
        }

        return placeBlock(player, buildStack, sigilHand, placePos, Direction.UP);
    }

    @Override
    public boolean useOnBlock(Level level, Player player, ItemStack stack, BlockPos blockPos, Direction side, Vec3 hitVec) {
        if (level.isClientSide) {
            return false;
        }

        // Determine which hand has the sigil and get the other hand's item
        InteractionHand sigilHand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack buildStack = sigilHand == InteractionHand.MAIN_HAND
                ? player.getOffhandItem()
                : player.getMainHandItem();

        if (buildStack.isEmpty() || !(buildStack.getItem() instanceof BlockItem)) {
            return false;
        }

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
                            placedAny |= placeBlock(player, buildStack, sigilHand, placePos, side);
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
                placePos = placePos.relative(side);
                distance++;
                if (distance > AnimusConfig.sigils.builderRange.get()) {
                    return false;
                }
            } while (!level.isEmptyBlock(placePos) && placePos.getY() > level.getMinBuildHeight());

            if (level.isEmptyBlock(placePos) && !buildStack.isEmpty()) {
                return placeBlock(player, buildStack, sigilHand, placePos, side);
            }
        }

        return false;
    }

    private static boolean placeBlock(Player player, ItemStack stack, InteractionHand sigilHand,
                                      BlockPos pos, Direction side) {
        Level level = player.level();
        if (player.isSpectator() || !player.mayBuild() || !level.hasChunkAt(pos)
            || !level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)
            || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, side, stack)) {
            return false;
        }
        InteractionHand hand = sigilHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false);
        return stack.useOn(new BlockPlaceContext(player, hand, stack, hit)).consumesAction();
    }
}
