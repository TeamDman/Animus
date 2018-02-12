package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.api.impl.ItemSigil;
import WayofTime.bloodmagic.api.util.helper.NBTHelper;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.api.util.helper.PlayerHelper;
import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.google.common.base.Strings;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
		super(AnimusConfig.chainsConsumption);
	}

	@Override
	public int getLpUsed() {
		return AnimusConfig.chainsConsumption;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		boolean unusable = isUnusable(stack);
		if (!playerIn.world.isRemote && !unusable) {
			NetworkHelper.getSoulNetwork(playerIn).syphonAndDamage(playerIn, getLpUsed());
			ItemStack soul = new ItemStack(AnimusItems.mobSoul);
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound targetData = new NBTTagCompound();
			target.setUniqueId(new UUID(playerIn.world.rand.nextInt(100000), playerIn.world.rand.nextInt(100000000)));
			target.writeToNBT(targetData);
			tag.setString("id", EntityList.getEntityString(target));
			if (target instanceof EntityLiving && target.hasCustomName())
				tag.setString("name", target.getCustomNameTag());
			tag.setTag("MobData", targetData);
			soul.setTagCompound(tag);
			soul.setStackDisplayName((tag.hasKey("name") ? tag.getString("name") + "" : tag.getString("id")) + " Soul");
			if (!playerIn.inventory.addItemStackToInventory(soul))
				playerIn.world.spawnEntity(new EntityItem(playerIn.world, target.posX, target.posY, target.posZ, soul));
			target.setDead();
		}
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {

		NBTHelper.checkNBT(stack);

		if (!Strings.isNullOrEmpty(getOwnerName(stack)))
			tooltip.add(TextHelper.localizeEffect("tooltip.BloodMagic.currentOwner", PlayerHelper.getUsernameFromStack(stack)));

		super.addInformation(stack, world, tooltip, flag);
	}

	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}