package com.teamdman.animus.items;

import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.BloodMagicAPI;
import WayofTime.bloodmagic.api.util.helper.PlayerSacrificeHelper;
import WayofTime.bloodmagic.item.ItemDaggerOfSacrifice;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

/**
 * Created by TeamDman on 9/25/2016.
 */
public class ItemKamaBound extends ItemDaggerOfSacrifice {

	public ItemKamaBound() {
		super();
		this.maxStackSize = 1;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase hit, EntityLivingBase attacker) {
		double x = hit.posX;
		double y = hit.posY;
		double z = hit.posZ;
		int d0 = 10;
		AxisAlignedBB region = new AxisAlignedBB(x, y, z, x, y, z).expand(d0, d0, d0);
		List<EntityLivingBase> entities = hit.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, region);
		if (entities == null || entities.isEmpty())
			return false;
		for (EntityLivingBase target : entities) {
			if (target == null || attacker == null || attacker.worldObj.isRemote || (attacker instanceof EntityPlayer && !(attacker instanceof EntityPlayerMP)))
				return false;

			if (target instanceof EntityPlayer)
				return false;

			if (target.isDead || target.getHealth() < 0.5F)
				return false;

			String entityName = target.getClass().getSimpleName();
			int lifeEssence = 500;

			if (ConfigHandler.entitySacrificeValues.containsKey(entityName))
				lifeEssence = ConfigHandler.entitySacrificeValues.get(entityName);

			if (BloodMagicAPI.getEntitySacrificeValues().containsKey(entityName))
				lifeEssence = BloodMagicAPI.getEntitySacrificeValues().get(entityName);

			if (lifeEssence <= 0)
				return false;
			if (target.isChild())
				lifeEssence /= 2;

			if (PlayerSacrificeHelper.findAndFillAltar(attacker.worldObj, attacker, lifeEssence, true)) {
				target.worldObj.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (target.worldObj.rand.nextFloat() - target.worldObj.rand.nextFloat()) * 0.8F);
				target.setHealth(-1);
				target.onDeath(BloodMagicAPI.getDamageSource());
			}
		}
		return false;
	}
}
