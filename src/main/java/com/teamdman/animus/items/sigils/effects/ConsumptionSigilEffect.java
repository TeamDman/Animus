package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.blocks.BlockAntiLife;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.api.sigil.ISigilEffect;

/**
 * Sigil of Consumption - converts blocks to antilife.
 * Right-click to convert a block into spreading antilife that destroys matching blocks.
 */
public record ConsumptionSigilEffect() implements ISigilEffect {
    public static final MapCodec<ConsumptionSigilEffect> CODEC = MapCodec.unit(ConsumptionSigilEffect::new);

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return false;
        }

        // Raycast to find target block
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endVec = eyePos.add(lookVec.scale(5.0));

        BlockHitResult result = level.clip(new ClipContext(
                eyePos,
                endVec,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player
        ));

        if (result.getType() == HitResult.Type.MISS || result.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        // Convert block to antilife
        var antiLifeResult = BlockAntiLife.setBlockToAntiLife(level, result.getBlockPos(), player);
        return antiLifeResult.consumesAction();
    }

    @Override
    public boolean useOnBlock(Level level, Player player, ItemStack stack, BlockPos blockPos, Direction side, Vec3 hitVec) {
        if (level.isClientSide) {
            return false;
        }

        // Convert block to antilife
        var antiLifeResult = BlockAntiLife.setBlockToAntiLife(level, blockPos, player);
        return antiLifeResult.consumesAction();
    }
}
