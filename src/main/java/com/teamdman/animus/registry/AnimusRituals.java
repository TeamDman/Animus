package com.teamdman.animus.registry;

import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.registry.RitualRegistry;
import WayofTime.bloodmagic.api.ritual.Ritual;
import WayofTime.bloodmagic.ritual.RitualWater;
import com.teamdman.animus.AnimusConfig;

/**
 * Created by TeamDman on 10/1/2016.
 */
public class AnimusRituals {
	public static Ritual ritualSol;
	public static void initRituals() {
		ritualSol = new RitualWater();
		RitualRegistry.registerRitual(ritualSol, AnimusConfig.ritualSol);
	}
}
