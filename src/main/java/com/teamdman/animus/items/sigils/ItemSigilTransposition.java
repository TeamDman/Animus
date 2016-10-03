package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.api.impl.ItemSigil;
import WayofTime.bloodmagic.api.util.helper.NBTHelper;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.api.util.helper.PlayerHelper;
import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.util.ChatUtil;
import WayofTime.bloodmagic.util.Utils;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.google.common.base.Strings;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilTransposition extends ItemSigil implements IVariantProvider {
	public ItemSigilTransposition() {
		super(5000);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!isUnusable(stack)) {
			NBTHelper.checkNBT(stack);
			if (stack.getTagCompound().getLong("pos")==0) {
				NetworkHelper.getSoulNetwork(playerIn).syphonAndDamage(playerIn, getLpUsed());
				stack.getTagCompound().setLong("pos", pos.toLong());
				ChatUtil.sendNoSpam(playerIn, "Position set!");
			} else if (stack.getTagCompound().getLong("pos")!=0) {
				BlockPos _pos = BlockPos.fromLong(stack.getTagCompound().getLong("pos"));
				TileEntity _tile = worldIn.getTileEntity(_pos);
				BlockPos _place = pos.offset(facing);
				if (worldIn.isAirBlock(_place)) {
					worldIn.setBlockState(_place, worldIn.getBlockState(_pos));
					TileEntity _newtile = worldIn.getTileEntity(_place);
					if (_newtile != null && _tile != null) {
						NBTTagCompound _inv = _tile.serializeNBT();
						_inv.setInteger("x",_place.getX());
						_inv.setInteger("y",_place.getY());
						_inv.setInteger("z",_place.getZ());
						_newtile.deserializeNBT(_inv);
						worldIn.removeTileEntity(_pos);
					}
				}
				worldIn.setBlockToAir(_pos);
				stack.getTagCompound().setLong("pos",0);
			}
		}
		return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		System.out.println(world.isRemote);
		return super.onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {

		NBTHelper.checkNBT(stack);

		if (!Strings.isNullOrEmpty(getOwnerName(stack)))
			tooltip.add(TextHelper.localizeEffect("tooltip.BloodMagic.currentOwner", PlayerHelper.getUsernameFromStack(stack)));
		if (stack.getTagCompound().getLong("pos") != 0)
			tooltip.add("Position stored");
		super.addInformation(stack, player, tooltip, advanced);
	}

	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}