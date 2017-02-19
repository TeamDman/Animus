package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TeamDman on 2015-06-10.
 */
public class ItemMobSoul extends Item implements IVariantProvider {
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (stack.getTagCompound() == null)
			return EnumActionResult.PASS;
		if (worldIn.isRemote)
			return EnumActionResult.PASS;
		Entity mob;
		NBTTagCompound root = stack.getTagCompound();
		if (root.hasKey("id")) {
			String entityId = root.getString("id");
			mob = EntityList.createEntityByIDFromName(entityId, worldIn);
		} else {
			mob = EntityList.createEntityFromNBT(root.getCompoundTag("MobData"), worldIn);
		}
		if (mob == null)
			return EnumActionResult.PASS;
		mob.readFromNBT(root.getCompoundTag("MobData"));
		mob.setLocationAndAngles(pos.getX(), pos.getY() + 2, pos.getZ(), worldIn.rand.nextFloat() * 360.0F, 0);
		//		if (!worldIn.checkNoEntityCollision(mob.getEntityBoundingBox()) || !worldIn.getCollisionBoxes(mob, mob.getEntityBoundingBox()).isEmpty())
		//			return EnumActionResult.PASS;

		if (root.hasKey("name")) {
			mob.setCustomNameTag(root.getString("name"));
		}

		worldIn.spawnEntity(mob);

		if (mob instanceof EntityLiving) {
			((EntityLiving) mob).playLivingSound();
		}

		//		Entity riddenByEntity = mob.riddenByEntity;
		//		while (riddenByEntity != null) {
		//			riddenByEntity.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() * 360.0F, 0.0F);
		//			world.spawnEntityInWorld(riddenByEntity);
		//			if (riddenByEntity instanceof EntityLiving) {
		//				((EntityLiving) riddenByEntity).playLivingSound();
		//			}
		//			riddenByEntity = riddenByEntity.riddenByEntity;
		//		}
		playerIn.setHeldItem(hand, null);
		return EnumActionResult.PASS;
	}

	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}
