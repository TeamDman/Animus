package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.util.Constants;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

import static net.minecraft.client.Minecraft.getMinecraft;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilBuilder extends ItemSigilToggleableBaseBase {
	public ItemSigilBuilder() {
		super("builder", 100);
	}

	public ItemStack getStackToUse(EnumHand hand, EntityPlayer player) {
		return hand == EnumHand.MAIN_HAND ? player.getHeldItemOffhand() : player.getHeldItemMainhand();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (getActivated(stack) && !isSelected)
			ItemSigilBuilder.removeDelay();
	}

	@SuppressWarnings({"deprecation"})
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote && !isUnusable(stack)) {
			if (player.isSneaking()) {

				NBTTagCompound comp      = NBTHelper.checkNBT(stack).getTagCompound();
				boolean        activated = getActivated(stack);
				//noinspection ConstantConditions
				comp.setBoolean(Constants.NBT.ACTIVATED, !activated);
			} else {
				ItemStack _stack = getStackToUse(hand, player);
				if (_stack != null) {
					BlockPos air = player.getPosition().offset(player.getHorizontalFacing(), 2).up();
					if (world.isAirBlock(air) && !_stack.isEmpty()) {
						ItemBlock _item = (ItemBlock) _stack.getItem();
						if (_item != Items.AIR) {
							IBlockState _state = Block.getBlockFromItem(_item).getStateFromMeta(_item.getDamage(_stack));
							world.setBlockState(air, _state);
							NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, getLpUsed());
							_stack.shrink(1);
							if (hand == EnumHand.MAIN_HAND && _stack.getCount() <= 0)
								player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
						}
					}
				}
			}
		}

		return new ActionResult<>(EnumActionResult.PASS, stack);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		BlockPos air  = blockPos;
		int      dist = 0;
		if (player.isSneaking()) {
			for (int radius = 1; radius <= Math.sqrt(AnimusConfig.builderRange); radius++) {
				for (int x = -radius; x <= radius; x++) {
					for (int z = -radius; z <= radius; z++) {
						switch (side.getAxis()) {
							case X:
								air = blockPos.add(0, x, z);
								break;
							case Y:
								air = blockPos.add(x, 0, z);
								break;
							case Z:
								air = blockPos.add(x, z, 0);
								break;
						}
						if (world.isAirBlock(air)) {
							ItemStack _stack = getStackToUse(hand, player);
							if (_stack.isEmpty())
								return EnumActionResult.SUCCESS;
							ItemBlock _item = (ItemBlock) _stack.getItem();
							IBlockState _state = Block.getBlockFromItem(_item).getStateFromMeta(_item.getDamage(_stack));
							world.setBlockState(air, _state);
							_stack.shrink(1);
							if (hand == EnumHand.MAIN_HAND && _stack.getCount() <= 0)
								player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
							return EnumActionResult.SUCCESS;
						}
					}
				}
			}
		} else {
			do {
				air = air.offset(side.getOpposite(), 1);
				dist++;
				if (dist > AnimusConfig.builderRange)
					return EnumActionResult.SUCCESS;
			} while (!world.isAirBlock(air) || air.getY() <= 0);

			ItemStack _stack = getStackToUse(hand, player);
			if (_stack.isEmpty())
				return EnumActionResult.SUCCESS;
			ItemBlock _item = (ItemBlock) _stack.getItem();
			IBlockState _state = Block.getBlockFromItem(_item).getStateFromMeta(_item.getDamage(_stack));
			world.setBlockState(air, _state);
			NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, getLpUsed());
			_stack.shrink(1);
			if (hand == EnumHand.MAIN_HAND && _stack.getCount() <= 0)
				player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
		}
		return EnumActionResult.SUCCESS;
	}

	public static void removeDelay() {
		//ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, getMinecraft(), Integer.valueOf(0), 46);
		try {

			Field delay = Minecraft.class.getDeclaredField("rightClickDelayTimer");
			delay.setAccessible(true);
			delay.set(getMinecraft(), 0);
		} catch (Exception e) {
			System.out.println("ANIMUS BUILDER SIGIL HAS SCREWD UP");
			e.printStackTrace();
		}
	}
}