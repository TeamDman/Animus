package com.teamdman.animus.rituals.imperfect;

import WayofTime.bloodmagic.ritual.RitualRegister;
import WayofTime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import WayofTime.bloodmagic.ritual.imperfect.ImperfectRitual;
import com.teamdman.animus.Constants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

@RitualRegister.Imperfect(Constants.Rituals.HUNGER)
public class RitualHunger extends ImperfectRitual {
	public RitualHunger() {
		super(Constants.Rituals.HUNGER, e -> e.getBlock() == Blocks.BONE_BLOCK, 500, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.HUNGER);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean onActivate(IImperfectRitualStone imperfectRitualStone, EntityPlayer player) {
		player.getFoodStats().setFoodLevel(1);
		player.getFoodStats().setFoodSaturationLevel(10);
		imperfectRitualStone.getRitualWorld().playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (imperfectRitualStone.getRitualWorld().rand.nextFloat() - imperfectRitualStone.getRitualWorld().rand.nextFloat()) * 0.8F);
		return true;
	}

}
