package com.teamdman.animus.items;


import WayofTime.bloodmagic.altar.AltarComponent;
import WayofTime.bloodmagic.altar.AltarTier;
import WayofTime.bloodmagic.altar.ComponentType;
import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.core.RegistrarBloodMagicBlocks;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.util.Utils;
import amerifrance.guideapi.api.util.TextHelper;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

import javax.annotation.Nonnull;
import java.util.List;


/**
 * Created by TeamDman on 2015-08-30.
 */
public class ItemAltarDiviner extends Item implements IVariantProvider {
	@SuppressWarnings("NullableProblems")
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.getTileEntity(blockPos) == null || !(world.getTileEntity(blockPos) instanceof TileAltar))
			return EnumActionResult.PASS;

		TileAltar altar = (TileAltar) world.getTileEntity(blockPos);
		if (altar == null)
			return EnumActionResult.PASS;
		altar.checkTier();

		if (!player.isSneaking() || altar.getTier().toInt() >= AltarTier.MAXTIERS)
			return EnumActionResult.PASS;

		for (AltarComponent altarComponent : AltarTier.values()[hand.compareTo(EnumHand.OFF_HAND) == 0 ? AltarTier.MAXTIERS - 1 : altar.getTier().toInt()].getAltarComponents()) {
			BlockPos componentPos = blockPos.add(altarComponent.getOffset());
			if (world.isAirBlock(componentPos)) {
				world.setBlockState(componentPos, AnimusBlocks.BLOCKPHANTOMBUILDER.getDefaultState());
				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			}
		}

		String playerinfomsg = "";
		for (AltarComponent altarComponent : AltarTier.values()[altar.getTier().toInt()].getAltarComponents()) {
			BlockPos componentPos = blockPos.add(altarComponent.getOffset());
			Block    worldBlock   = world.getBlockState(componentPos).getBlock();

			if (altarComponent.getComponent() != ComponentType.NOTAIR) {
				if (worldBlock == AnimusBlocks.BLOCKPHANTOMBUILDER || world.isAirBlock(componentPos)) {
					int invSlot = getSlotFor(altarComponent, player);
					if (invSlot != -1) {
						ItemStack   _stack = player.inventory.getStackInSlot(invSlot);
						ItemBlock   _item  = (ItemBlock) player.inventory.getStackInSlot(invSlot).getItem();
						IBlockState _state = Block.getBlockFromItem(_item).getStateFromMeta(_item.getDamage(_stack));
						world.setBlockState(componentPos, _state);

						world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 0.5F, 1.0F);

						player.inventory.decrStackSize(invSlot, 1);
						return EnumActionResult.PASS;
					} else {
						if (world.isRemote) {
							playerinfomsg = I18n.format(Constants.Localizations.Text.DIVINER_MISSING) + " " + (altarComponent.getComponent() == ComponentType.GLOWSTONE ? Blocks.GLOWSTONE.getLocalizedName() : (I18n.format(new ItemStack(Utils.getBlockForComponent(altarComponent.getComponent())).getItem().getUnlocalizedName(new ItemStack(Utils.getBlockForComponent(altarComponent.getComponent()))) + ".name")));
						}
					}
				} else if (worldBlock != RegistrarBloodMagicBlocks.BLOOD_RUNE) {
					playerinfomsg = Constants.Localizations.Text.DIVINER_OBSTRUCTED;
				}
			}
		}
		if (playerinfomsg.length() > 0 && world.isRemote) {
			player.sendMessage(new TextComponentTranslation(playerinfomsg));
		}
		return EnumActionResult.PASS;
	}

	private int getSlotFor(AltarComponent component, EntityPlayer player) {
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			if (isItemComponent(component, player.inventory.mainInventory.get(i))) {
				return i;
			}
		}
		return -1;
	}

	private boolean isItemComponent(AltarComponent component, ItemStack stack2) {
		return Utils.getBlockForComponent(component.getComponent()) == Block.getBlockFromItem(stack2.getItem());
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.DIVINER_FIRST));
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.DIVINER_SECOND));
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.DIVINER_THIRD));
	}


	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}
}
