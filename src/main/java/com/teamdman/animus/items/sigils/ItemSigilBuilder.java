package com.teamdman.animus.items.sigils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.lang.reflect.Field;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilBuilder extends com.teamdman.animus.items.sigils.ItemSigilToggleableBase {
	public ItemSigilBuilder() {
		super("Builder",100);
	}

	@Override
	public void onSigilUpdate(ItemStack stack, World world, EntityPlayer player, int itemSlot, boolean isSelected) {
		super.onSigilUpdate(stack, world, player, itemSlot, isSelected);
		ItemSigilBuilder.removeDelay();
	}
	//			NetworkHelper.getSoulNetwork(playerIn).syphonAndDamage(playerIn, getLpUsed());

	public static void removeDelay() {
		try {
			Field delay = Minecraft.class.getDeclaredField("rightClickDelayTimer");
			delay.setAccessible(true);
			try {
				delay.set(Minecraft.getMinecraft(), 0);
			} catch (IllegalAccessException nsfe) {
				throw new RuntimeException(nsfe);
			}
		} catch (NoSuchFieldException nsfe) {
			throw new RuntimeException(nsfe);
		}
	}
}