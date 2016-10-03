package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.api.impl.ItemSigil;
import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.item.sigil.ItemSigilBase;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilChains extends ItemSigil implements IVariantProvider {
	public ItemSigilChains() {
		super(200);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		boolean unusable = isUnusable(stack);
		if (!playerIn.worldObj.isRemote && !unusable) {
			ItemStack soul = new ItemStack(AnimusItems.mobSoul);
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound targetData = new NBTTagCompound();
			//			for (int i = 0; i < 10; i++) {
			//				SpellHelper.sendIndexedParticleToAllAround(world, target.posX, target.posY, target.posZ, 20, world.provider.dimensionId, 1, target.posX, target.posY, target.posZ);
			//			}
			target.setUniqueId(new UUID(playerIn.worldObj.rand.nextInt(100000),playerIn.worldObj.rand.nextInt(100000000)));
			target.writeToNBT(targetData);
			tag.setString("id", EntityList.getEntityString(target));
			if (target instanceof EntityLiving && target.hasCustomName())
				tag.setString("name", target.getCustomNameTag());
			tag.setTag("MobData", targetData);
			soul.setTagCompound(tag);
			soul.setStackDisplayName((tag.hasKey("name") ? tag.getString("name") + "" : tag.getString("id")) + " Soul");
			if (!playerIn.inventory.addItemStackToInventory(soul))
				playerIn.worldObj.spawnEntityInWorld(new EntityItem(playerIn.worldObj, target.posX, target.posY, target.posZ, soul));
			target.setDead();
		}
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}

	@Override
	public List<Pair<Integer, String>> getVariants()
	{
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}