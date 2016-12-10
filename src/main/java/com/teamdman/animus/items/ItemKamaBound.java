package com.teamdman.animus.items;

import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.BloodMagicAPI;
import WayofTime.bloodmagic.api.altar.IBloodAltar;
import WayofTime.bloodmagic.api.util.helper.PlayerSacrificeHelper;
import WayofTime.bloodmagic.item.ItemDaggerOfSacrifice;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

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
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityLivingBase) {

			hitEntity(stack,(EntityLivingBase) entity,player);
			return true;
		} else {
			return false;
		}
	}


	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		checkAndKill(player.posX,player.posY,player.posZ, world, player,true);
		return new ActionResult<>(EnumActionResult.SUCCESS,stack);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase hit, EntityLivingBase attacker) {
		double x = hit.posX;
		double y = hit.posY;
		double z = hit.posZ;
		checkAndKill(x,y,z,hit.world,attacker,false);
		return false;
	}

	private void checkAndKill(double x, double y, double z, World world, EntityLivingBase attacker, boolean efficient) {
		int d0 = 10;
		AxisAlignedBB region = new AxisAlignedBB(x, y, z, x, y, z).expand(d0, d0, d0);
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, region);
		if (entities == null || entities.isEmpty())
			return;
		for (EntityLivingBase target : entities) {
			//			System.out.println(target.getDisplayName());
			if (target == null || attacker == null || attacker.world.isRemote || (attacker instanceof EntityPlayer && !(attacker instanceof EntityPlayerMP)))
				continue;

			if (target instanceof EntityPlayer)
				continue;

			if (target.isDead || target.getHealth() < 0.5F)
				continue;

			String entityName = target.getClass().getSimpleName();
			int lifeEssence = 500;

			if (ConfigHandler.entitySacrificeValues.containsKey(entityName))
				lifeEssence = ConfigHandler.entitySacrificeValues.get(entityName);

			if (BloodMagicAPI.getEntitySacrificeValues().containsKey(entityName))
				lifeEssence = BloodMagicAPI.getEntitySacrificeValues().get(entityName);

			if (lifeEssence <= 0)
				continue;
			if (!target.isNonBoss())
				continue;

			if (target.isChild())
				lifeEssence /= 2;

			if (efficient?findAndFillAltar(world,attacker,lifeEssence):PlayerSacrificeHelper.findAndFillAltar(world, attacker, lifeEssence, true)) {
				target.world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.8F);
				target.setHealth(-1);
				target.onDeath(BloodMagicAPI.getDamageSource());
			}
		}
	}

	private boolean findAndFillAltar(World world, EntityLivingBase sacrificingEntity, int amount) {
		IBloodAltar altarEntity = PlayerSacrificeHelper.getAltar(world, sacrificingEntity.getPosition());
		if (altarEntity == null)
			return false;
		if (altarEntity.getCurrentBlood()+amount > altarEntity.getCapacity())
			return false;

		altarEntity.sacrificialDaggerCall(amount, true);
		altarEntity.startCycle();

		return true;
	}
}
