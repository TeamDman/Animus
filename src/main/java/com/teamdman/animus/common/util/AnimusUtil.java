package com.teamdman.animus.common.util;

import WayofTime.bloodmagic.ritual.AreaDescriptor;
import WayofTime.bloodmagic.tile.TileAltar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class AnimusUtil {
	/**
	 * @author mDiyo
	 */
	public static RayTraceResult raytraceFromEntity(World world, Entity player, boolean useLiquids, double range) {
		float  f  = 1.0F;
		float  f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float  f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * f;
		double d1 = player.prevPosY + (player.posY - player.prevPosY) * f;
		if (player instanceof EntityPlayer)
			d1 += ((EntityPlayer) player).eyeHeight;
		double d2    = player.prevPosZ + (player.posZ - player.prevPosZ) * f;
		Vec3d  vec3  = new Vec3d(d0, d1, d2);
		float  f3    = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
		float  f4    = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
		float  f5    = -MathHelper.cos(-f1 * 0.017453292F);
		float  f6    = MathHelper.sin(-f1 * 0.017453292F);
		float  f7    = f4 * f5;
		float  f8    = f3 * f5;
		Vec3d  vec31 = vec3.addVector(f7 * range, f6 * range, f8 * range);
		return world.rayTraceBlocks(vec3, vec31, useLiquids);
	}

	public static TileAltar getNearbyAltar(World world, AreaDescriptor area, BlockPos offset, BlockPos previous) {
		TileEntity tile;
		if (area.isWithinArea(previous) && (tile = world.getTileEntity(previous)) instanceof TileAltar)
			return (TileAltar) tile;

		Optional<TileEntity> found = area.getContainedPositions(offset).stream()
				.map(world::getTileEntity)
				.filter(e -> e instanceof TileAltar)
				.findFirst();

		area.resetCache();
		return (TileAltar) found.orElse(null);
	}
}
