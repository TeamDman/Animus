package com.teamdman.animus.registry;

import WayofTime.bloodmagic.ritual.Ritual;
import WayofTime.bloodmagic.ritual.RitualRegistry;
import WayofTime.bloodmagic.ritual.imperfect.ImperfectRitual;
import WayofTime.bloodmagic.ritual.imperfect.ImperfectRitualRegistry;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.rituals.*;
import com.teamdman.animus.rituals.imperfect.RitualHunger;
import com.teamdman.animus.rituals.imperfect.RitualRegression;

/**
 * Created by TeamDman on 10/1/2016.
 */
public class AnimusRituals {
	public static Ritual          ritualCulling;
	public static Ritual          ritualEntropy;
	public static ImperfectRitual ritualHunger;
	public static Ritual          ritualLuna;
	public static Ritual          ritualNaturesLeech;
	public static Ritual          ritualPeace;
	public static ImperfectRitual ritualRegression;
	public static Ritual          ritualSol;
	public static Ritual          ritualSteadfastHeart;
	public static Ritual          ritualUnmaking;
	public static Ritual          ritualVengefulSpirit;

	public static void init() {
		RitualRegistry.registerRitual(ritualSol = new RitualSol(), AnimusConfig.ritualList.ritualSol);
		RitualRegistry.registerRitual(ritualLuna = new RitualLuna(), AnimusConfig.ritualList.ritualLuna);
		RitualRegistry.registerRitual(ritualEntropy = new RitualEntropy(), AnimusConfig.ritualList.ritualEntropy);
		RitualRegistry.registerRitual(ritualUnmaking = new RitualUnmaking(), AnimusConfig.ritualList.ritualUnmaking);
		RitualRegistry.registerRitual(ritualPeace = new RitualPeace(), AnimusConfig.ritualList.ritualPeace);
		RitualRegistry.registerRitual(ritualNaturesLeech = new RitualNaturesLeech(), AnimusConfig.ritualList.ritualNaturesLeech);
		RitualRegistry.registerRitual(ritualCulling = new RitualCulling(), AnimusConfig.ritualList.ritualCulling);
		RitualRegistry.registerRitual(ritualSteadfastHeart = new RitualSteadfastHeart(), AnimusConfig.ritualList.ritualSteadfastHeart);
		ImperfectRitualRegistry.registerRitual(ritualRegression = new RitualRegression(), AnimusConfig.ritualList.ritualRegression);
		ImperfectRitualRegistry.registerRitual(ritualHunger = new RitualHunger(), AnimusConfig.ritualList.ritualHunger);
	}
}
