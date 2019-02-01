package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.ritual.AreaDescriptor;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.teamdman.animus.Constants;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;


public class ItemSigilLeech extends ItemSigilToggleableBaseBase {
	private final AreaDescriptor eatRange = new AreaDescriptor.Rectangle(new BlockPos(-5, 0, -5), 10);

	//todo: cleanup all of this
	public ItemSigilLeech() {
		super(Constants.Sigils.LEECH, 5);
	}

	@Override

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote && !isUnusable(stack)) {
			NBTTagCompound comp      = NBTHelper.checkNBT(stack).getTagCompound();
			boolean        activated = getActivated(stack);
			//noinspection ConstantConditions
			comp.setBoolean(WayofTime.bloodmagic.util.Constants.NBT.ACTIVATED, !activated);
		}

		return new ActionResult<>(EnumActionResult.PASS, stack);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (!getActivated(stack) || !(entityIn instanceof EntityPlayer) || entityIn instanceof FakePlayer)
			return;

		EntityPlayer player = (EntityPlayer) entityIn;
		if (!player.canEat(false) && !player.isSneaking())
			return;

		ItemStack stackFood = getFood(player);
		if ((!stackFood.isEmpty() || eatGrowables(player)) && !worldIn.isRemote) {
			if (!stackFood.isEmpty())
				stackFood.shrink(Math.min(worldIn.rand.nextInt(4), stackFood.getCount()));
			player.getFoodStats().addStats(1 + worldIn.rand.nextInt(3), 2F);
		}

		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	private ItemStack getFood(EntityPlayer player) {
		for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
			ItemStack stack = player.inventory.mainInventory.get(i);
			if (stack.isEmpty() || !(stack.getItem() instanceof IPlantable || Block.getBlockFromItem(stack.getItem()) instanceof IPlantable))
				continue;
			return stack;
		}
		return ItemStack.EMPTY;
	}

	private boolean eatGrowables(EntityPlayer player) {
		if (!eatRange.hasNext())
			eatRange.resetIterator();
		for (int i = 0; i < 32 && eatRange.hasNext(); i++) {
			BlockPos nextPos   = eatRange.next().add(player.getPosition());
			Block    thisBlock = player.world.getBlockState(nextPos).getBlock();
			if (thisBlock == Blocks.AIR)
				continue;
			String blockName = thisBlock.getTranslationKey().toLowerCase();

			if (!(thisBlock instanceof BlockLog
					|| thisBlock instanceof BlockLeaves
					|| thisBlock instanceof IPlantable
					|| blockName.contains("extrabiomesxl.flower"))
					|| blockName.contains("specialflower") || blockName.contains("shinyflower"))
				continue;

			if (player.world.isRemote)
				player.world.spawnParticle(EnumParticleTypes.SPELL, nextPos.getX() + 0.5, nextPos.getY() + 0.5, nextPos.getZ() + .5,
						(player.world.rand.nextDouble() - 0.5D) * 2.0D, -player.world.rand.nextDouble(), (player.world.rand.nextDouble() - 0.5D) * 2.0D);

			player.world.playSound(null, nextPos, AnimusSoundEventHandler.naturesleech, SoundCategory.BLOCKS, .4F, 1F);
			player.world.setBlockToAir(nextPos);

			if (player.canEat(false) || !player.isSneaking())
				return true;
		}
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.SIGIL_LEECH_FLAVOUR));
		super.addInformation(stack, world, tooltip, flag);
	}
}
