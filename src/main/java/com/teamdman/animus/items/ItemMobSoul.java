package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import com.teamdman.animus.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Created by TeamDman on 2015-06-10.
 */
public class ItemMobSoul extends Item implements IVariantProvider {
	@SuppressWarnings("NullableProblems")
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote) return EnumActionResult.FAIL;
		//noinspection ConstantConditions
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(Constants.NBT.SOUL_ENTITY_NAME))
			return EnumActionResult.FAIL;
		Entity mob = EntityList.createEntityByIDFromName(new ResourceLocation(stack.getTagCompound().getString("entity")), world);
		if (mob == null) return EnumActionResult.FAIL;
		if (stack.getTagCompound().hasKey(Constants.NBT.SOUL_DATA))
			mob.readFromNBT(stack.getTagCompound().getCompoundTag(Constants.NBT.SOUL_DATA));
		mob.setLocationAndAngles(blockPos.getX(), blockPos.getY() + 2, blockPos.getZ(), new Random().nextFloat() * 360.0F, 0);
		world.spawnEntity(mob);
		if (mob instanceof EntityLiving)
			((EntityLiving) mob).playLivingSound();
		player.setHeldItem(hand, ItemStack.EMPTY);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}
}
