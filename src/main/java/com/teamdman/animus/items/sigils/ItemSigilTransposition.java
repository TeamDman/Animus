package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.core.data.Binding;
import WayofTime.bloodmagic.iface.ISigil;
import WayofTime.bloodmagic.util.ChatUtil;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.helper.PlayerHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.teamdman.animus.Constants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilTransposition extends ItemSigilToggleableBaseBase {
	public ItemSigilTransposition() {
		super(Constants.Sigils.TRANSPOSITION, 5000);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack      stack  = player.getHeldItem(hand);

		if (stack.getItem() instanceof ISigil.Holding)
			stack = ((Holding) stack.getItem()).getHeldItem(stack, player);
		if (PlayerHelper.isFakePlayer(player))
			return ActionResult.newResult(EnumActionResult.FAIL, stack);
		if (world.isRemote || isUnusable(stack))
			return super.onItemRightClick(world, player, hand);

		RayTraceResult result = this.rayTrace(world, player, true);
		//noinspection ConstantConditions
		if (result == null || result.typeOfHit == RayTraceResult.Type.MISS || result.typeOfHit != RayTraceResult.Type.BLOCK) {
			NBTHelper.checkNBT(stack);
			//noinspection ConstantConditions
			stack.getTagCompound().setLong("pos", 0);
			ChatUtil.sendNoSpam(player, Constants.Localizations.Text.TRANSPOSITION_CLEARED);
			setActivatedState(stack,false);
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}


	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() instanceof Holding)
			stack = ((Holding) stack.getItem()).getHeldItem(stack,player);
		Binding  binding = getBinding(stack);
		if (binding == null)
			return EnumActionResult.PASS;

		BlockPos posNew     = blockPos.offset(side);
		if (!isUnusable(stack) && !world.isRemote) {
			NBTHelper.checkNBT(stack);
			if (!getActivated(stack)) {
				//noinspection ConstantConditions
				stack.getTagCompound().setLong("pos", posNew.offset(side.getOpposite()).toLong());
				ChatUtil.sendNoSpamUnloc(player, Constants.Localizations.Text.TRANSPOSITION_SET);
				world.playSound(null, posNew, SoundEvents.ENTITY_SHULKER_TELEPORT, SoundCategory.BLOCKS, 1, 1);
				setActivatedState(stack,true);
			} else {
				//noinspection ConstantConditions
				BlockPos   posOld   = BlockPos.fromLong(stack.getTagCompound().getLong("pos"));
				TileEntity tileOld  = world.getTileEntity(posOld);
				if (world.isAirBlock(posNew)) {
					NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, getLpUsed());
					world.setBlockState(posNew, world.getBlockState(posOld));

					TileEntity tileNew = world.getTileEntity(posNew);
					if (tileNew != null && tileOld != null) {
						NBTTagCompound inv = tileOld.serializeNBT();
						inv.setInteger("x", posNew.getX());
						inv.setInteger("y", posNew.getY());
						inv.setInteger("z", posNew.getZ());
						tileNew.deserializeNBT(inv);
						world.removeTileEntity(posOld);
					}

					world.setBlockToAir(posOld);
					world.playSound(null, posNew, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1, 1);
					stack.getTagCompound().setLong("pos", 0);
					setActivatedState(stack,false);
				} else {
					ChatUtil.sendNoSpamUnloc(player, Constants.Localizations.Text.DIVINER_OBSTRUCTED);
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		NBTHelper.checkNBT(stack);
		//noinspection ConstantConditions
		if (stack.getTagCompound().getLong("pos") != 0)
			tooltip.add(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_STORED);
		Binding binding = getBinding(stack);
		if (binding == null) return;
		tooltip.add(TextHelper.localizeEffect(Constants.Localizations.Tooltips.OWNER, binding.getOwnerName()));
	}
}