package com.teamdman.animus.handlers;

import WayofTime.bloodmagic.core.RegistrarBloodMagicBlocks;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.slots.SlotNoPickup;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandler {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onPlaySoundEvent(PlaySoundEvent e) {
		if (AnimusConfig.general.muteWither && (e.getName().equals("entity.wither.spawn"))) {
			e.setResultSound(null);
		}
		if (AnimusConfig.general.muteDragon && (e.getName().equals("entity.enderdragon.death"))) {
			e.setResultSound(null);
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent eventArgs) {
		Container open = eventArgs.player.inventoryContainer;
		if (open == null)
			return;
		int frags = 0;
		for (int i = 0; i < open.inventorySlots.size(); i++) {
			Slot    slot   = open.inventorySlots.get(i);
			boolean isFrag = slot.getHasStack() && slot.getStack().getItem() == AnimusItems.FRAGMENTHEALING;
			if (isFrag) {
				frags++;
			}
			if (slot instanceof SlotNoPickup) {
				if (eventArgs.player.capabilities.isCreativeMode || !slot.getHasStack() || slot.getStack().getItem() != AnimusItems.FRAGMENTHEALING) {
					Slot repl = new Slot(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos);
					repl.slotNumber = slot.slotNumber;
					open.inventorySlots.set(i, repl);
				}
			} else if (isFrag && !eventArgs.player.capabilities.isCreativeMode && slot.getClass() == Slot.class) {
				Slot repl = new SlotNoPickup(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos);
				repl.slotNumber = slot.slotNumber;
				open.inventorySlots.set(i, repl);
			}
		}
		if (eventArgs.player.world.getWorldTime() % 20 == 0 && frags >= 9 && !eventArgs.player.world.isRemote) {
			eventArgs.player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, frags / 9 - 1));
			if (frags >= 35 && eventArgs.player.world.getWorldTime() % 200 == 0)
				eventArgs.player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 200, 4));
		}
	}


	@SubscribeEvent
	public void onEntityJoined(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityLightningBolt) {
			EntityLightningBolt bolt = ((EntityLightningBolt) event.getEntity());
			if (event.getWorld().getBlockState(bolt.getPosition().down()).getBlock() == RegistrarBloodMagicBlocks.LIFE_ESSENCE)
				event.getWorld().setBlockState(bolt.getPosition().down(), AnimusBlocks.BLOCKFLUIDANTIMATTER.getDefaultState());
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityHurt(LivingHurtEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		DamageSource     source = event.getSource();

		if (
				!source.equals(DamageSource.IN_FIRE) &&
						!source.equals(DamageSource.LAVA) &&
						!source.equals(DamageSource.CACTUS) &&
						!source.equals(DamageSource.LIGHTNING_BOLT) &&
						!source.equals(DamageSource.IN_WALL)
		) {
			entity.hurtResistantTime = 0;
			entity.hurtTime = 1;
		}
	}
}
