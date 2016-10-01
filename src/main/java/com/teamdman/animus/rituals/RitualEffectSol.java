package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.api.ritual.IMasterRitualStone;
import WayofTime.bloodmagic.api.ritual.Ritual;
import WayofTime.bloodmagic.api.ritual.RitualComponent;

import java.util.ArrayList;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualEffectSol extends Ritual {
	public RitualEffectSol(String name, int crystalLevel, int activationCost, String unlocalizedName) {
		super(name, crystalLevel, activationCost, unlocalizedName);
	}

	@Override
	public int getRefreshCost() {
		return 0;
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