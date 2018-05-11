package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.core.data.Binding;
import WayofTime.bloodmagic.iface.ISigil;
import WayofTime.bloodmagic.util.ChatUtil;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.helper.PlayerHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.block.state.IBlockState;
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
			stack.getTagCompound().setLong(Constants.NBT.TRANSPOSITION_POS, 0);
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

		if (!isUnusable(stack) && !world.isRemote) {
			NBTHelper.checkNBT(stack);
			IBlockState state = world.getBlockState(blockPos);
			if (!getActivated(stack)) {
				if (AnimusConfig.sigils.transpositionMovesUnbreakables < 2 && state.getBlock().getBlockHardness(state, world, blockPos) == -1)
					return EnumActionResult.PASS;
				//noinspection ConstantConditions
				stack.getTagCompound().setLong(Constants.NBT.TRANSPOSITION_POS, blockPos.toLong());
				ChatUtil.sendNoSpamUnloc(player, Constants.Localizations.Text.TRANSPOSITION_SET);
				world.playSound(null, blockPos, SoundEvents.ENTITY_SHULKER_TELEPORT, SoundCategory.BLOCKS, 1, 1);
				setActivatedState(stack,true);
			} else {
				//noinspection ConstantConditions
				BlockPos posOld = BlockPos.fromLong(stack.getTagCompound().getLong(Constants.NBT.TRANSPOSITION_POS));
				BlockPos posNew = blockPos.offset(side);
				if (AnimusConfig.sigils.transpositionMovesUnbreakables == 0 && world.getBlockState(posOld).getBlock().getBlockHardness(world.getBlockState(posOld), world, posOld) == -1) {
					stack.getTagCompound().setLong(Constants.NBT.TRANSPOSITION_POS, 0);
					world.playSound(null, posNew, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.BLOCKS, 1, 1);
					setActivatedState(stack, false);
					ChatUtil.sendNoSpamUnloc(player, Constants.Localizations.Text.TRANSPOSITION_UNMOVABLE);
					return EnumActionResult.PASS;
				}
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
					stack.getTagCompound().setLong(Constants.NBT.TRANSPOSITION_POS, 0);
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
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_FLAVOUR));
		NBTHelper.checkNBT(stack);
		//noinspection ConstantConditions
		if (stack.getTagCompound().getLong(Constants.NBT.TRANSPOSITION_POS) != 0)
			tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.SIGIL_TRANSPOSITION_STORED));
		Binding binding = getBinding(stack);
		if (binding == null)
			return;
		tooltip.add(TextHelper.localizeEffect(Constants.Localizations.Tooltips.OWNER, binding.getOwnerName()));
	}
}