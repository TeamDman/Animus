package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.item.sigil.ItemSigilBase;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilChains extends ItemSigilBase implements IVariantProvider {
	public ItemSigilChains() {
		super("chains", AnimusConfig.chainsConsumption);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		boolean unusable = isUnusable(stack);
		if (!playerIn.world.isRemote && !unusable) {
			NetworkHelper.getSoulNetwork(playerIn).syphonAndDamage(playerIn, getLpUsed());
			ItemStack      soul       = new ItemStack(AnimusItems.MOBSOUL);
			NBTTagCompound tag        = new NBTTagCompound();
			NBTTagCompound targetData = new NBTTagCompound();
			target.writeToNBT(targetData);
			tag.setString("entity", EntityList.getKey(target).toString());
			targetData.setInteger("id", EntityList.getID(target.getClass()));
			if (target instanceof EntityLiving && target.hasCustomName())
				tag.setString("name", target.getCustomNameTag());
			tag.setTag("MobData", targetData);
			soul.setTagCompound(tag);
			soul.setStackDisplayName((tag.hasKey("name") ? tag.getString("name") : EntityList.getTranslationName(new ResourceLocation(tag.getString("entity"))) + " Soul"));
			if (!playerIn.inventory.addItemStackToInventory(soul))
				playerIn.world.spawnEntity(new EntityItem(playerIn.world, target.posX, target.posY, target.posZ, soul));
			target.setDead();
		}
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}
}