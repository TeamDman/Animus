package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.api.ritual.IMasterRitualStone;
import WayofTime.bloodmagic.api.ritual.Ritual;
import WayofTime.bloodmagic.api.ritual.RitualComponent;
import com.teamdman.animus.Animus;

import java.util.ArrayList;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualEffectSol extends Ritual {
	public RitualEffectSol(String name, int crystalLevel, int activationCost, String unlocalizedName) {
		super("ritualSol", 0, 1000, "ritual."+ Animus.MODID+".SolRitual");
	}

	@Override
	public int getRefreshCost() {
		return 1;
	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {

	}

	@Override
	public Ritual getNewCopy() {
		return null;
	}

	@Override
	public ArrayList<RitualComponent> getComponents() {

		return null;
	}
}