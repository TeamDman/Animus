package com.teamdman.animus.registry;

import WayofTime.bloodmagic.ritual.Ritual;
import WayofTime.bloodmagic.ritual.RitualRegistry;
import WayofTime.bloodmagic.ritual.imperfect.ImperfectRitual;
import WayofTime.bloodmagic.ritual.imperfect.ImperfectRitualRegistry;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.rituals.*;
import com.teamdman.animus.rituals.imperfect.RitualRegression;

/**
 * Created by TeamDman on 10/1/2016.
 */
public class AnimusRituals {
	public static Ritual ritualSol;
	public static Ritual ritualLuna;
	public static Ritual ritualEntropy;
	public static Ritual ritualUnmaking;
	public static Ritual ritualPeace;
	public static Ritual ritualNaturesLeech;
	public static Ritual ritualCulling;
	public static Ritual ritualSteadfastHeart;
	public static Ritual ritualVengefulSpirit;

	public static ImperfectRitual ritualRegression;

	public static void init() {
		ritualSol = new RitualSol();
		RitualRegistry.registerRitual(ritualSol, AnimusConfig.ritualList.ritualSol);
		ritualLuna = new RitualLuna();
		RitualRegistry.registerRitual(ritualLuna, AnimusConfig.ritualList.ritualLuna);
		ritualEntropy = new RitualEntropy();
		RitualRegistry.registerRitual(ritualEntropy, AnimusConfig.ritualList.ritualEntropy);
		ritualUnmaking = new RitualUnmaking();
		RitualRegistry.registerRitual(ritualUnmaking, AnimusConfig.ritualList.ritualUnmaking);
		ritualPeace = new RitualPeace();
		RitualRegistry.registerRitual(ritualPeace, AnimusConfig.ritualList.ritualPeace);
		ritualNaturesLeech = new RitualNaturesLeech();
		RitualRegistry.registerRitual(ritualNaturesLeech, AnimusConfig.ritualList.ritualNaturesLeech);
		ritualCulling = new RitualCulling();
		RitualRegistry.registerRitual(ritualCulling, AnimusConfig.ritualList.ritualCulling);
		ritualSteadfastHeart = new RitualSteadfastHeart();
		RitualRegistry.registerRitual(ritualSteadfastHeart, AnimusConfig.ritualList.ritualSteadfastHeart);
		ritualRegression = new RitualRegression();
		ImperfectRitualRegistry.registerRitual(ritualRegression, AnimusConfig.ritualList.ritualRegression);
	}
}
