package com.teamdman.animus.registry;

import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.registry.RitualRegistry;
import WayofTime.bloodmagic.api.ritual.Ritual;
import WayofTime.bloodmagic.ritual.RitualWater;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.rituals.*;

/**
 * Created by TeamDman on 10/1/2016.
 */
public class AnimusRituals {
	public static Ritual ritualSol;
	public static Ritual ritualLuna;
	public static Ritual ritualEntropy;
	public static Ritual ritualUnmaking;
	public static Ritual ritualPeace;

	public static void init() {
		ritualSol = new RitualSol();
		RitualRegistry.registerRitual(ritualSol, AnimusConfig.ritualSol);
		ritualLuna = new RitualLuna();
		RitualRegistry.registerRitual(ritualLuna, AnimusConfig.ritualLuna);
		ritualEntropy = new RitualEntropy();
		RitualRegistry.registerRitual(ritualEntropy, AnimusConfig.ritualEntropy);
		ritualUnmaking = new RitualUnmaking();
		RitualRegistry.registerRitual(ritualUnmaking, AnimusConfig.ritualUnmaking);
		ritualPeace = new RitualPeace();
		RitualRegistry.registerRitual(ritualPeace,AnimusConfig.ritualPeace);

	}
}
