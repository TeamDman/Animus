package com.teamdman.animus.rituals.imperfect;

import WayofTime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import WayofTime.bloodmagic.ritual.imperfect.ImperfectRitual;
import WayofTime.bloodmagic.util.ChatUtil;
import com.teamdman.animus.Animus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import com.teamdman.animus.Constants;
public class RitualRegression extends ImperfectRitual {

	public RitualRegression() {
		super("ritualRegression", e -> e.getBlock() == Blocks.BOOKSHELF, 3000, true, "ritual." + Constants.Mod.MODID + ".regression");
	}

	@Override
	public boolean onActivate(IImperfectRitualStone imperfectRitualStone, EntityPlayer player) {
		if (player.getHeldItemMainhand() == null) {
			ChatUtil.sendNoSpamUnloc(player, "text.component.holdingitem");
			return false;
		}
		NBTTagCompound comp = player.getHeldItemMainhand().getTagCompound();
		comp.removeTag("RepairCost");
		return true;
	}

}
