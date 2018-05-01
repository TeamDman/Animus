package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.item.sigil.ItemSigilBase;
import WayofTime.bloodmagic.util.ChatUtil;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.google.common.base.Strings;
import com.teamdman.animus.AnimusConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilTransposition extends ItemSigilBase implements IVariantProvider {
	public ItemSigilTransposition() {
		super("transposition", AnimusConfig.transpositionConsumption);

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack      stack  = player.getHeldItem(hand);
		RayTraceResult result = this.rayTrace(world, player, true);
		if (result == null || result.typeOfHit == RayTraceResult.Type.MISS) {
			NBTHelper.checkNBT(stack);
			stack.getTagCompound().setLong("pos", 0);
			ChatUtil.sendNoSpam(player, "Position cleared!");
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}


	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		BlockPos  pos   = blockPos.offset(side);
		if (!isUnusable(stack)) {
			NBTHelper.checkNBT(stack);
			if (stack.getTagCompound().getLong("pos") == 0) {
				stack.getTagCompound().setLong("pos", pos.toLong());
				ChatUtil.sendNoSpamUnloc(player, "text.component.transposition.set");
				world.playSound(null, pos, SoundEvents.ENTITY_SHULKER_TELEPORT, SoundCategory.BLOCKS, 1, 1);
			} else if (stack.getTagCompound().getLong("pos") != 0) {
				BlockPos   _pos   = BlockPos.fromLong(stack.getTagCompound().getLong("pos"));
				TileEntity _tile  = world.getTileEntity(_pos);
				BlockPos   _place = pos.offset(side);
				if (world.isAirBlock(_place)) {
					NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, getLpUsed());
					world.setBlockState(_place, world.getBlockState(_pos));
					TileEntity _newtile = world.getTileEntity(_place);
					if (_newtile != null && _tile != null) {
						NBTTagCompound _inv = _tile.serializeNBT();
						_inv.setInteger("x", _place.getX());
						_inv.setInteger("y", _place.getY());
						_inv.setInteger("z", _place.getZ());
						_newtile.deserializeNBT(_inv);
						world.removeTileEntity(_pos);
					}
					world.setBlockToAir(_pos);
					world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1, 1);
					stack.getTagCompound().setLong("pos", 0);

				} else {
					ChatUtil.sendNoSpamUnloc(player, "text.component.transposition.obstructed");
				}
			}
		}
		return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {

		NBTHelper.checkNBT(stack);
		if (getBinding(stack) == null) return;
		//TODO: fix custom binding info
		if (!Strings.isNullOrEmpty(getBinding(stack).getOwnerName()))
			tooltip.add(TextHelper.localizeEffect("tooltip.BloodMagic.currentOwner", getBinding(stack).getOwnerName()));
		if (stack.getTagCompound().getLong("pos") != 0)
			tooltip.add("Position stored");
		super.addInformation(stack, world, tooltip, flag);
	}

	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}
}