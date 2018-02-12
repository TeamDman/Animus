package com.teamdman.animus.items;


import WayofTime.bloodmagic.api.BlockStack;
import WayofTime.bloodmagic.api.altar.AltarComponent;
import WayofTime.bloodmagic.api.altar.EnumAltarComponent;
import WayofTime.bloodmagic.api.altar.EnumAltarTier;
import WayofTime.bloodmagic.api.altar.IBloodAltar;
import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.core.RegistrarBloodMagicBlocks;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.util.Utils;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by TeamDman on 2015-08-30.
 */
public class ItemAltarDiviner extends Item implements IVariantProvider {
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add("Shift-rightclick on an altar to show altar outline");
		tooltip.add("Keep using to automatically place altar blocks");
		tooltip.add("Use in off hand to display max tier altar outline");
	}

	private boolean isItemComponent(AltarComponent component, ItemStack stack2) {
		return Utils.getBlockForComponent(component.getComponent()) == Block.getBlockFromItem(stack2.getItem());
	}

	private int getSlotFor(AltarComponent component, EntityPlayer player) {
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			if (player.inventory.mainInventory.get(i) != null && isItemComponent(component, player.inventory.mainInventory.get(i))) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (world.getTileEntity(blockPos) == null || !(world.getTileEntity(blockPos) instanceof IBloodAltar))
			return EnumActionResult.PASS;

		TileAltar altar = (TileAltar) world.getTileEntity(blockPos);
		altar.checkTier();

		if (!player.isSneaking() || altar == null || altar.getTier().toInt() >= EnumAltarTier.MAXTIERS)
			return EnumActionResult.PASS;


		for (AltarComponent altarComponent : EnumAltarTier.values()[hand.compareTo(EnumHand.OFF_HAND) == 0 ? EnumAltarTier.MAXTIERS - 1 : altar.getTier().toInt()].getAltarComponents()) {
			BlockPos componentPos = blockPos.add(altarComponent.getOffset());
			if (world.isAirBlock(componentPos)) {
				world.setBlockState(componentPos, AnimusBlocks.phantomBuilder.getDefaultState());
				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			}
		}

		String playerinfomsg = "";
		for (AltarComponent altarComponent : EnumAltarTier.values()[altar.getTier().toInt()].getAltarComponents()) {
			BlockPos componentPos = blockPos.add(altarComponent.getOffset());
			BlockStack worldBlock = new BlockStack(world.getBlockState(componentPos).getBlock(), world.getBlockState(componentPos).getBlock().getMetaFromState(world.getBlockState(componentPos)));

			if (altarComponent.getComponent() != EnumAltarComponent.NOTAIR) {
				if (worldBlock.getBlock() == AnimusBlocks.phantomBuilder || world.isAirBlock(componentPos)) {
					int invSlot = getSlotFor(altarComponent, player);
					if (invSlot != -1) {
						ItemStack _stack = player.inventory.getStackInSlot(invSlot);
						ItemBlock _item = (ItemBlock) player.inventory.getStackInSlot(invSlot).getItem();
						@SuppressWarnings("deprecation")
						IBlockState _state = Block.getBlockFromItem(_item).getStateFromMeta(_item.getDamage(_stack));
						world.setBlockState(componentPos, _state);

						world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 0.5F, 1.0F);

						player.inventory.decrStackSize(invSlot, 1);
						return EnumActionResult.PASS;
					} else {
						if (world.isRemote) {
							playerinfomsg = I18n.format("text.component.diviner.missing") + " " + (altarComponent.getComponent() == EnumAltarComponent.GLOWSTONE ? "Glowstone Block" : (I18n.format(new ItemStack(Utils.getBlockForComponent(altarComponent.getComponent())).getItem().getUnlocalizedName(new ItemStack(Utils.getBlockForComponent(altarComponent.getComponent()))) + ".name")));
						}
					}
				} else if (worldBlock.getBlock() != RegistrarBloodMagicBlocks.BLOOD_RUNE) {
					playerinfomsg = "text.component.diviner.obstructed";
				}
			}
		}
		if (playerinfomsg.length() > 0 && world.isRemote) {
			player.sendMessage(new TextComponentTranslation(playerinfomsg));
		}
		return EnumActionResult.PASS;
	}


	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}
