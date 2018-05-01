package com.teamdman.animus.registry;

import com.teamdman.animus.common.potion.PotionVengefulSpirits;
import net.minecraft.potion.Potion;

public class AnimusPotions {
	public static Potion VENGEFULSPIRITS;

	public static void init() {
		VENGEFULSPIRITS = register(new PotionVengefulSpirits(), "vengefulspirits");
	}


	public static Potion register(Potion potion, String name) {
		potion.setRegistryName(name);
		//potion.setPotionName(potion.getClass().getSimpleName());
		potion.setPotionName("Ven Spirits");
		//GameRegistry. register(potion);

		return potion;
	}

}
