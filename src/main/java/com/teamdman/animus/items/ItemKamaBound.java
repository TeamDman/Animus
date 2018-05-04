package com.teamdman.animus.items;

import WayofTime.bloodmagic.altar.IBloodAltar;
import WayofTime.bloodmagic.util.helper.PlayerSacrificeHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by TeamDman on 9/25/2016.
 */
public class ItemKamaBound extends ItemKama {

	static final DamageSource khopeshDamage = new DamageSource("animus.absolute");

	public ItemKamaBound() {
		super(Item.ToolMaterial.DIAMOND);
		this.maxStackSize = 1;

	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityLivingBase) {
			hitEntity(stack, (EntityLivingBase) entity, player);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase hit, EntityLivingBase attacker) {
		double x = hit.posX;
		double y = hit.posY;
		double z = hit.posZ;
		if (checkAndKill(x, y, z, hit.world, attacker, false))
			return false;
		else
			return !checkAndDamage(x, y, z, hit.world, attacker);

	}

	private boolean checkAndDamage(double x, double y, double z, World world, EntityLivingBase attacker) {
		int     d0     = 5;
		boolean hit    = false;
		boolean result = false;

		AxisAlignedBB          region   = new AxisAlignedBB(x, y, z, x, y, z).expand(d0, d0, d0);
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, region);
		if (entities.isEmpty())
			return false;
		for (EntityLivingBase target : entities) {
			if (target == null || target.isDead || attacker == null || !(attacker instanceof EntityPlayer)
					|| attacker == target)
				continue;

			result = target.attackEntityFrom(khopeshDamage, this.attackDamage);


			if (result)
				hit = true;

		}
		return hit;

	}

	private boolean checkAndKill(double x, double y, double z, World world, EntityLivingBase attacker,
								 boolean efficient) {
		int                    d0       = 5;
		boolean                killed   = false;
		AxisAlignedBB          region   = new AxisAlignedBB(x, y, z, x, y, z).expand(d0, d0, d0);
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, region);
		if (entities.isEmpty())
			return false;
		for (EntityLivingBase target : entities) {

			if (target == null || attacker == null || attacker.world.isRemote || !(attacker instanceof EntityPlayer))
				continue;

			if (target.isDead || target.getHealth() < 0.5F || !target.isNonBoss() || target instanceof EntityPlayer)
				continue;

			//String entityName = target.getClass().getSimpleName();
			int lifeEssence = 500;

			//if (ConfigHandler.entitySacrificeValues.containsKey(entityName))
			//lifeEssence = ConfigHandler.entitySacrificeValues.get(entityName);

			//if (BloodMagicAPI.getEntitySacrificeValues().containsKey(entityName))
			//lifeEssence = BloodMagicAPI.getEntitySacrificeValues().get(entityName);
			//TODO: fix essence values
			if (lifeEssence <= 0)
				continue;

			if (target.isChild())
				lifeEssence /= 2;

			if (efficient ? findAndFillAltar(world, attacker, lifeEssence)
					: PlayerSacrificeHelper.findAndFillAltar(world, attacker, lifeEssence, true)) {
				target.world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH,
						SoundCategory.BLOCKS, 0.5F,
						2.6F + (target.world.rand.nextFloat() - target.world.rand.nextFloat()) * 0.8F);
				target.setHealth(-1);
				target.onDeath(khopeshDamage);
				killed = true;
			}
		}

		return killed;
	}

	@Override
	public int getItemEnchantability() {
		return Item.ToolMaterial.GOLD.getEnchantability();
	}

	private boolean findAndFillAltar(World world, EntityLivingBase sacrificingEntity, int amount) {
		IBloodAltar altarEntity = PlayerSacrificeHelper.getAltar(world, sacrificingEntity.getPosition());
		if (altarEntity == null)
			return false;
		if (altarEntity.getCurrentBlood() + amount > altarEntity.getCapacity())
			return false;

		altarEntity.sacrificialDaggerCall(amount, true);
		altarEntity.startCycle();

		return true;
	}
}
