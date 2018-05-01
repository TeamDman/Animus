package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.item.sigil.ItemSigilToggleableBase;
import WayofTime.bloodmagic.ritual.AreaDescriptor;
import WayofTime.bloodmagic.util.Constants;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ItemSigilLeech extends ItemSigilToggleableBaseBase {
	public static final String EFFECT_RANGE = "effect";
	Random random = new Random();
	protected final Map<String, AreaDescriptor> modableRangeMap = new HashMap<String, AreaDescriptor>();

	public ItemSigilLeech() {
		super("leech", 5);
	}

	public ItemStack getFood(EntityPlayer player) {
		int i;
		for (i = 0; i < player.inventory.mainInventory.size(); i++) {
			if (player.inventory.mainInventory.get(i) == null)
				continue;
			Item food = player.inventory.mainInventory.get(i).getItem();
			if (food == null)
				continue;

			if (food instanceof IPlantable) {
				return player.inventory.mainInventory.get(i);
			}

		}

		return null;
	}

	public boolean eatGrowables(EntityPlayer player) {
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 0, 0), 10));
		AreaDescriptor eatRange;
		eatRange = getBlockRange(EFFECT_RANGE);
		eatRange.resetIterator();
		int count = random.nextInt(2);

		while (eatRange.hasNext()) {
			int      i         = 0;
			BlockPos nextPos   = eatRange.next().add(player.getPosition());
			Block    thisBlock = player.world.getBlockState(nextPos).getBlock();
			if (thisBlock == Blocks.AIR)
				continue;
			boolean edible = false;

			String blockName = thisBlock.getUnlocalizedName().toLowerCase();

			if (thisBlock instanceof BlockCrops || thisBlock instanceof BlockLog
					|| thisBlock instanceof BlockLeaves || thisBlock instanceof BlockFlower
					|| thisBlock instanceof BlockTallGrass || thisBlock instanceof BlockDoublePlant
					|| blockName.contains("extrabiomesxl.flower"))
				edible = true;

			if (blockName.contains("specialflower") || blockName.contains("shinyflower"))
				edible = false;

			if (!edible)
				continue;

			if (player.world.isRemote) {
				player.world.spawnParticle(EnumParticleTypes.SPELL, nextPos.getX() + 0.5, nextPos.getY() + 0.5, nextPos.getZ() + .5,
						(this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D, new int[0]);
			}

			player.world.playSound(null, nextPos, AnimusSoundEventHandler.naturesleech, SoundCategory.BLOCKS, .4F, 1F);
			player.world.setBlockToAir(nextPos);
			i++;
			if (i >= count)
				return true;

		}
		return false;
	}


	public ItemStack getEdible(EntityPlayer player) {
		ItemStack food;
		if ((food = getFood(player)) != null)
			return food;
		else
			return null;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		boolean eaten = false;
		if (getActivated(stack)) {
			if (entityIn instanceof EntityPlayer && !(entityIn instanceof FakePlayer)) {
				EntityPlayer player = (EntityPlayer) entityIn;
				if (player.canEat(false)) {
					ItemStack haseditable = null;
					haseditable = getEdible(player);
					if (haseditable != null) {
						haseditable.shrink(Math.min(random.nextInt(4), haseditable.getCount()));
						eaten = true;
					} else if (eatGrowables(player) == true) {
						eaten = true;
					}
					if (eaten) {
						int fill = 1 + random.nextInt(3);
						player.getFoodStats().addStats(fill, 2F);
					}
				}
			}
		}
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote && !isUnusable(stack)) {
			NBTTagCompound comp      = NBTHelper.checkNBT(stack).getTagCompound();
			boolean        activated = getActivated(stack);
			comp.setBoolean(Constants.NBT.ACTIVATED, !activated);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}

	public void addBlockRange(String range, AreaDescriptor defaultRange) {
		modableRangeMap.put(range, defaultRange);
	}

	/**
	 * Used to grab the range of a ritual for a given effect.
	 *
	 * @param range - Range that needs to be pulled.
	 * @return -
	 */
	public AreaDescriptor getBlockRange(String range) {
		if (modableRangeMap.containsKey(range)) {
			return modableRangeMap.get(range);
		}

		return null;
	}


}
