package com.teamdman.animus.handlers;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {
	@SubscribeEvent
	public void onPlaySoundEvent(PlaySoundEvent e) {
//		System.out.println(e.getName());
		if (AnimusConfig.muteWither && e.getName().contains("entity.wither.spawn")) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
		if (e.getModID().equals(Animus.MODID)) {
			AnimusConfig.syncConfig();
		}
	}
	
	
}
