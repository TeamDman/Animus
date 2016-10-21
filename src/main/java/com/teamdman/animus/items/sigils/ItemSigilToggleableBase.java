package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.api.impl.ItemSigilToggleable;
import WayofTime.bloodmagic.api.util.helper.NBTHelper;
import WayofTime.bloodmagic.api.util.helper.PlayerHelper;
import WayofTime.bloodmagic.client.IMeshProvider;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.google.common.base.Strings;
import com.teamdman.animus.client.mesh.CustomMeshDefinitionActivatable;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemSigilToggleableBase extends ItemSigilToggleable implements IMeshProvider {
	private final String name;

	public ItemSigilToggleableBase(String name, int lpUsed)
	{
		super(lpUsed);
		this.name = name;
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
	{
		NBTHelper.checkNBT(stack);
		super.addInformation(stack, player, tooltip, advanced);
		tooltip.add(TextHelper.localizeEffect("tooltip.BloodMagic." + (getActivated(stack) ? "activated" : "deactivated")));

		if (!Strings.isNullOrEmpty(getOwnerName(stack)))
			tooltip.add(TextHelper.localizeEffect("tooltip.BloodMagic.currentOwner", PlayerHelper.getUsernameFromStack(stack)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemMeshDefinition getMeshDefinition()
	{
		return new CustomMeshDefinitionActivatable("itemSigil" + WordUtils.capitalize(name));
	}

	@Nullable
	@Override
	public ResourceLocation getCustomLocation()
	{
		return null;
	}

	@Override
	public List<String> getVariants()
	{
		List<String> ret = new ArrayList<String>();
		ret.add("active=false");
		ret.add("active=true");

		return ret;
	}
}
