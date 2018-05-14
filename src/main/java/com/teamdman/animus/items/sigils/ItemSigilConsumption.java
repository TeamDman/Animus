package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.item.sigil.ItemSigilBase;
import amerifrance.guideapi.api.util.TextHelper;
import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.BlockAntimatter;
import com.teamdman.animus.common.util.AnimusUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilConsumption extends ItemSigilBase implements IVariantProvider {
	public ItemSigilConsumption() {
		super(Constants.Sigils.CONSUMPTION, 200);
	}


	@SuppressWarnings("NullableProblems")
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		RayTraceResult result = AnimusUtil.raytraceFromEntity(worldIn, playerIn, true, 5);
		if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
			return new ActionResult<>(BlockAntimatter.setBlockToAntimatter(worldIn, result.getBlockPos(), playerIn), playerIn.getHeldItem(handIn));
		}
		return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.SIGIL_CONSUMPTION_FLAVOUR));
		super.addInformation(stack, world, tooltip, flag);
	}

}