package com.teamdman.animus.rituals.imperfect;

import WayofTime.bloodmagic.ritual.RitualRegister;
import WayofTime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import WayofTime.bloodmagic.ritual.imperfect.ImperfectRitual;
import WayofTime.bloodmagic.util.ChatUtil;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import com.teamdman.animus.Constants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

@RitualRegister.Imperfect(Constants.Rituals.REGRESSION)
public class RitualRegression extends ImperfectRitual {

	public RitualRegression() {
		super(Constants.Rituals.REGRESSION, e -> e.getBlock() == Blocks.BOOKSHELF, 3000, true, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.REGRESSION);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean onActivate(IImperfectRitualStone imperfectRitualStone, EntityPlayer player) {
		if (player.getHeldItemMainhand().isEmpty()) {
			ChatUtil.sendNoSpamUnloc(player, "text.component.holdingitem");
			return false;
		}
		NBTTagCompound comp = NBTHelper.checkNBT(player.getHeldItemMainhand()).getTagCompound();
		//noinspection ConstantConditions
		comp.removeTag("RepairCost");
		return true;
	}

}
