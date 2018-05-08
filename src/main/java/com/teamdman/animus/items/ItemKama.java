package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by TeamDman on 9/18/2016.
 */
public class ItemKama extends ItemSword implements IVariantProvider {
	final float             attackDamage;
	final Item.ToolMaterial mat;

	public ItemKama(Item.ToolMaterial material) {
		super(material);
		mat = material;
		attackDamage = 2.0F + material.getAttackDamage();
		setFull3D();
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
	public boolean hitEntity(ItemStack stack, EntityLivingBase hit, @Nullable EntityLivingBase attacker) {
		double                 x        = hit.posX;
		double                 y        = hit.posY;
		double                 z        = hit.posZ;
		int                    d0       = (mat.getHarvestLevel() + 1) * 2;
		AxisAlignedBB          region   = new AxisAlignedBB(x, y, z, x, y, z).expand(d0, d0, d0);
		List<EntityLivingBase> entities = hit.world.getEntitiesWithinAABB(EntityLivingBase.class, region);
		if (entities.isEmpty())
			return false;
		for (EntityLivingBase target : entities) {
			if (target instanceof EntityPlayer)
				continue;
			if (target == null || attacker.world.isRemote)
				continue;
			target.attackEntityFrom(DamageSource.causeMobDamage(attacker), attackDamage);
			stack.damageItem(1, attacker);
		}
		return false;
	}

	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}
}
