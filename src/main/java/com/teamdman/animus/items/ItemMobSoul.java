package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by TeamDman on 2015-06-10.
 */
public class ItemMobSoul extends Item implements IVariantProvider {
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		
		Random bRand = new Random();

		if (stack.getTagCompound() == null)
			return EnumActionResult.PASS;
		if (world.isRemote)
			return EnumActionResult.PASS;
		Entity mob;
		NBTTagCompound root = stack.getTagCompound();
		if (root.hasKey("id")) {
			int entityId = Integer.parseInt(root.getString("id"));
			mob = EntityList.createEntityByID(entityId, world);
		} else {
			mob = EntityList.createEntityFromNBT(root.getCompoundTag("MobData"), world);
		}
		if (mob == null)
			return EnumActionResult.PASS;
		mob.readFromNBT(root.getCompoundTag("MobData"));
		mob.setLocationAndAngles(blockPos.getX(), blockPos.getY() + 2, blockPos.getZ(), bRand.nextFloat() * 360.0F, 0);

		if (root.hasKey("name")) {
			mob.setCustomNameTag(root.getString("name"));
		}

		world.spawnEntity(mob);

		if (mob instanceof EntityLiving) {
			((EntityLiving) mob).playLivingSound();
		}

		player.setHeldItem(hand, null);
		return EnumActionResult.PASS;
	}

@Override	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {		variants.put(0,"type=normal");	}
}
