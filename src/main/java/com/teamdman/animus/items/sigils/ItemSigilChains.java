package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.item.sigil.ItemSigilBase;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.teamdman.animus.Constants;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilChains extends ItemSigilBase implements IVariantProvider {
	public ItemSigilChains() {
		super(Constants.Sigils.CHAINS, 500);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		boolean unusable = isUnusable(stack);
		if (!playerIn.world.isRemote && !unusable) {
			NetworkHelper.getSoulNetwork(playerIn).syphonAndDamage(playerIn, new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_CHAINS), getLpUsed()));
			ItemStack      soul       = new ItemStack(AnimusItems.MOBSOUL);
			NBTTagCompound tag        = new NBTTagCompound();
			NBTTagCompound targetData = new NBTTagCompound();
			target.writeToNBT(targetData);
			//if they mess with the nbt, then the game will crash /shrug
			//noinspection ConstantConditions
			tag.setString(Constants.NBT.SOUL_ENTITY_NAME, EntityList.getKey(target).toString());
			targetData.setInteger(Constants.NBT.SOUL_ENTITY_ID, EntityList.getID(target.getClass()));
			if (target instanceof EntityLiving && target.hasCustomName())
				tag.setString(Constants.NBT.SOUL_NAME, target.getCustomNameTag());
			tag.setTag(Constants.NBT.SOUL_DATA, targetData);
			soul.setTagCompound(tag);
			soul.setTranslatableName((tag.hasKey(Constants.NBT.SOUL_NAME) ? tag.getString(Constants.NBT.SOUL_NAME) : EntityList.getTranslationName(new ResourceLocation(tag.getString(Constants.NBT.SOUL_ENTITY_NAME))) + " Soul"));
			if (!playerIn.inventory.addItemStackToInventory(soul))
				playerIn.world.spawnEntity(new EntityItem(playerIn.world, target.posX, target.posY, target.posZ, soul));
			target.setDead();
		}
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.SIGIL_CHAINS_FLAVOUR));
		super.addInformation(stack, world, tooltip, flag);
	}

}