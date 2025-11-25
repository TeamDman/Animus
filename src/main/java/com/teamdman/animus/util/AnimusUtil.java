package com.teamdman.animus.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.altar.AltarTier;
import wayoftime.bloodmagic.common.tile.TileAltar;
import wayoftime.bloodmagic.ritual.AreaDescriptor;

import java.util.Optional;

public class AnimusUtil {
    /**
     * @author mDiyo
     */
    public static BlockHitResult raytraceFromEntity(Level level, Entity entity, boolean useLiquids, double range) {
        float f = 1.0F;
        float pitch = entity.xRotO + (entity.getXRot() - entity.xRotO) * f;
        float yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * f;
        double d0 = entity.xOld + (entity.getX() - entity.xOld) * f;
        double d1 = entity.yOld + (entity.getY() - entity.yOld) * f;

        if (entity instanceof Player) {
            d1 += ((Player) entity).getEyeHeight();
        }

        double d2 = entity.zOld + (entity.getZ() - entity.zOld) * f;
        Vec3 vec3 = new Vec3(d0, d1, d2);
        float f3 = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f4 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f5 = -Mth.cos(-pitch * 0.017453292F);
        float f6 = Mth.sin(-pitch * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        Vec3 vec31 = vec3.add(f7 * range, f6 * range, f8 * range);

        ClipContext.Fluid fluidMode = useLiquids ?
            ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;

        return level.clip(new ClipContext(
            vec3,
            vec31,
            ClipContext.Block.OUTLINE,
            fluidMode,
            entity
        ));
    }

    public static TileAltar getNearbyAltar(Level level, AreaDescriptor area, BlockPos offset, BlockPos previous) {
        BlockEntity tile;
        if (area.isWithinArea(previous) && (tile = level.getBlockEntity(previous)) instanceof TileAltar) {
            return (TileAltar) tile;
        }

        Optional<BlockEntity> found = area.getContainedPositions(offset).stream()
            .map(level::getBlockEntity)
            .filter(e -> e instanceof TileAltar)
            .findFirst();

        area.resetCache();
        return (TileAltar) found.orElse(null);
    }
}
