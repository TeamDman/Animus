package com.teamdman.animus.handlers;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.slots.SlotNoPickup;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandler {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onPlaySoundEvent(PlaySoundEvent e) {
		if (AnimusConfig.muteWither && (e.getName().equals("entity.wither.spawn"))) {
			e.setResultSound(null);
		}
		if (AnimusConfig.muteDragon && (e.getName().equals("entity.enderdragon.death"))) {
			e.setResultSound(null);
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
		if (e.getModID().equals(Constants.Mod.MODID)) {
			AnimusConfig.syncConfig();
		}
	}



	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent eventArgs) {
		Container open = eventArgs.player.openContainer;
		if (open == null)
			return;
		int frags = 0;
		for (int i = 0; i < open.inventorySlots.size(); i++) {
			Slot slot = (Slot) open.inventorySlots.get(i);
			if (slot.getHasStack() && slot.getStack().getItem() == AnimusItems.FRAGMENTHEALING) {
				frags++;
				if (!eventArgs.player.capabilities.isCreativeMode && slot.getClass() == Slot.class) {
					open.inventorySlots.set(i, new SlotNoPickup(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos));
				}
			}
		}
		if (eventArgs.player.world.getWorldTime() % 20 == 0 && frags >= 9 && !eventArgs.player.world.isRemote) {
			eventArgs.player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20, frags / 9 - 1));
			if (frags >= 35 && eventArgs.player.world.getWorldTime() % 200 == 0)
				eventArgs.player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 200, 4));
		}
	}
}
