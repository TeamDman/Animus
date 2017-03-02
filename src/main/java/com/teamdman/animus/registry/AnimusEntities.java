package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.entity.EntityVengefulSpirit;

import net.minecraftforge.fml.common.registry.EntityRegistry;

public class AnimusEntities {

	public static void init() {
		int id = 0;
		EntityRegistry.registerModEntity(EntityVengefulSpirit.class, "SpiritOfVengeance", id++, Animus.instance, 128, 3, true);
	}

	
}
