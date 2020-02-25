package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.ritual.AreaDescriptor;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.teamdman.animus.Constants;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;
import com.teamdman.animus.rituals.RitualNaturesLeech;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;
import java.util.Optional;

import static com.teamdman.animus.rituals.RitualNaturesLeech.isConsumable;


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
		if ((eatFromInventory(player) || eatFromSurroundingWorld(player)) && !worldIn.isRemote)
			player.getFoodStats().addStats(1 + worldIn.rand.nextInt(3), 2F);
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	private boolean eatFromInventory(EntityPlayer player) {
		Optional<ItemStack> food = player.inventory.mainInventory.stream()
				.filter(stack -> isConsumable(Block.getBlockFromItem(stack.getItem())))
				.findFirst();
		if (food.isPresent()) {
			food.get().shrink(Math.min(player.world.rand.nextInt(4), food.get().getCount()));
			return true;
		}
		return false;
	}

	private boolean eatFromSurroundingWorld(EntityPlayer player) {
		if (!eatRange.hasNext())
			eatRange.resetIterator();
		for (int i = 0; i < 32 && eatRange.hasNext(); i++) {
			BlockPos eatPos   = eatRange.next().add(player.getPosition());
			Block    eatBlock = player.world.getBlockState(eatPos).getBlock();

			if (!isConsumable(eatBlock))
				continue;

			if (player.world.isRemote) {
				player.world.spawnParticle(
						EnumParticleTypes.SPELL,
						eatPos.getX() + 0.5,
						eatPos.getY() + 0.5,
						eatPos.getZ() + .5,
						(player.world.rand.nextDouble() - 0.5D) * 2.0D,
						-player.world.rand.nextDouble(),
						(player.world.rand.nextDouble() - 0.5D) * 2.0D
				);
			}

			player.world.setBlockToAir(eatPos);
			player.world.playSound(
					null,
					eatPos,
					AnimusSoundEventHandler.naturesleech,
					SoundCategory.BLOCKS,
					.4F,
					1F);
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
