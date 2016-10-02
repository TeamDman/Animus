package com.teamdman.animus.registry;

import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.registry.RitualRegistry;
import WayofTime.bloodmagic.api.ritual.Ritual;
import WayofTime.bloodmagic.ritual.RitualWater;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.rituals.RitualLuna;
import com.teamdman.animus.rituals.RitualSol;

/**
 * Created by TeamDman on 10/1/2016.
 */
public class AnimusRituals {
	public static Ritual ritualSol;
	public static Ritual ritualLuna;

	public static void initRituals() {
		ritualSol = new RitualSol();
		RitualRegistry.registerRitual(ritualSol, AnimusConfig.ritualSol);
		ritualLuna = new RitualLuna();
		RitualRegistry.registerRitual(ritualLuna, AnimusConfig.ritualLuna);
	}
}
