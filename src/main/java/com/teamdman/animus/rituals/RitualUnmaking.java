package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.Utils;
import com.teamdman.animus.Animus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualUnmaking extends Ritual {
	public static final String EFFECT_RANGE = "effect";

	public RitualUnmaking() {
		super("ritualUnmaking", 1, 3000, "ritual." + Animus.MODID + ".unmakingRitual");

		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-2, -2, -2), 5));
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 8, 8);
	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World world = masterRitualStone.getWorldObj();
		SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		int currentEssence = network.getCurrentEssence();
		BlockPos masterPos = masterRitualStone.getBlockPos();

		if (!masterRitualStone.getWorldObj().isRemote) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}

			AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
			List<EntityItem> itemList = world.getEntitiesWithinAABB(EntityItem.class, effectRange.getAABB(masterRitualStone.getBlockPos()));
			if (itemList != null) {
				EntityItem books = null;
				for (EntityItem entityItem : itemList) {
					if (entityItem.isDead || books != null) {
						continue;
					}
					if (entityItem.getEntityItem().getItem() == Items.BOOK) {
						books = entityItem;
					}
				}
				if (books == null)
					return;
				for (EntityItem entityItem : itemList) {
					if (entityItem.isDead) {
						continue;
					}
					if (entityItem.getEntityItem().getItem() == Items.ENCHANTED_BOOK) {
						NBTTagList enchants = entityItem.getEntityItem().getTagCompound().getTagList("StoredEnchantments", 10);
						if (enchants == null)
							continue;
						for (int i = enchants.tagCount() - 1; i >= 0; i--) {
							if (books == null || books.getEntityItem() == null)
								break;

							NBTTagCompound data = enchants.getCompoundTagAt(i);
							short enchID = data.getShort("id");
							int enchLVL = data.getShort("lvl") - 1;
							enchants.removeTag(i);


							ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
							enchBook.setTagCompound(new NBTTagCompound());
							NBTTagList bookTags = new NBTTagList();
							NBTTagCompound comp = new NBTTagCompound();
							comp.setShort("id", enchID);
							comp.setShort("lvl", (short) (enchLVL < 1 ? 1 : enchLVL));
							bookTags.appendTag(comp);
							enchBook.getTagCompound().setTag("StoredEnchantments", bookTags);
							world.spawnEntityInWorld(new EntityItem(world, masterPos.getX(), masterPos.getY() + 1, masterPos.getZ(), enchBook.copy()));
							world.spawnEntityInWorld(new EntityItem(world, masterPos.getX(), masterPos.getY() + 1, masterPos.getZ(), enchBook));

							books.getEntityItem().stackSize--;
						}
						entityItem.getEntityItem().stackSize--;
						world.playSound(null,masterPos, SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.BLOCKS,0.5F,1.0F);
						masterRitualStone.stopRitual(BreakType.DEACTIVATE);
					} else {
						NBTTagList enchants = entityItem.getEntityItem().getEnchantmentTagList();
						if (enchants != null) {
							for (int i = enchants.tagCount() - 1; i >= 0; --i) {
								if (books == null || books.getEntityItem() == null) {
									break;
								}
								ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
								NBTTagCompound data = enchants.getCompoundTagAt(i);
								short enchID = data.getShort("id");
								short enchLVL = data.getShort("lvl");
								enchants.removeTag(i);
								enchBook.setTagCompound(new NBTTagCompound());
								NBTTagList bookTags = new NBTTagList();
								enchBook.getTagCompound().setTag("StoredEnchantments", bookTags);
								NBTTagCompound nbttagcompound = new NBTTagCompound();
								nbttagcompound.setShort("id", enchID);
								nbttagcompound.setShort("lvl", enchLVL);
								bookTags.appendTag(nbttagcompound);
								world.spawnEntityInWorld(new EntityItem(world, masterPos.getX(), masterPos.getY() + 1, masterPos.getZ(), enchBook));
								books.getEntityItem().stackSize--;
							}
							if (entityItem.getEntityItem().getEnchantmentTagList().tagCount() == 0) {
								entityItem.getEntityItem().getTagCompound().removeTag("ench");
							}

							world.playSound(null,masterPos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS,0.5F,1.0F);
							masterRitualStone.stopRitual(BreakType.DEACTIVATE);
						}
					}
				}
			}
		}
	}

	@Override
	public int getRefreshCost() {
		return 0;
	}

	@Override
	public int getRefreshTime() {
		return 20;
	}

	@Override
	public ArrayList<RitualComponent> getComponents() {
		ArrayList<RitualComponent> components = new ArrayList();
		this.addRune(components, -4, 0, -2, EnumRuneType.FIRE);
		this.addRune(components, -4, 0, 0, EnumRuneType.FIRE);
		this.addRune(components, -4, 0, 2, EnumRuneType.FIRE);
		this.addRune(components, -3, 0, -3, EnumRuneType.DUSK);
		this.addRune(components, -3, 0, -1, EnumRuneType.FIRE);
		this.addRune(components, -3, 0, 1, EnumRuneType.FIRE);
		this.addRune(components, -3, 0, 3, EnumRuneType.DUSK);
		this.addRune(components, -2, 0, -4, EnumRuneType.AIR);
		this.addRune(components, -2, 0, -2, EnumRuneType.DUSK);
		this.addRune(components, -2, 0, 0, EnumRuneType.FIRE);
		this.addRune(components, -2, 0, 2, EnumRuneType.DUSK);
		this.addRune(components, -2, 0, 4, EnumRuneType.EARTH);
		this.addRune(components, -1, 0, -3, EnumRuneType.AIR);
		this.addRune(components, -1, 0, -1, EnumRuneType.DUSK);
		this.addRune(components, -1, 0, 0, EnumRuneType.FIRE);
		this.addRune(components, -1, 0, 1, EnumRuneType.DUSK);
		this.addRune(components, -1, 0, 3, EnumRuneType.EARTH);
		this.addRune(components, 0, 0, -4, EnumRuneType.AIR);
		this.addRune(components, 0, 0, -2, EnumRuneType.AIR);
		this.addRune(components, 0, 0, -1, EnumRuneType.AIR);
		this.addRune(components, 0, 0, 1, EnumRuneType.EARTH);
		this.addRune(components, 0, 0, 2, EnumRuneType.EARTH);
		this.addRune(components, 0, 0, 4, EnumRuneType.EARTH);
		this.addRune(components, 1, 0, -3, EnumRuneType.AIR);
		this.addRune(components, 1, 0, -1, EnumRuneType.DUSK);
		this.addRune(components, 1, 0, 0, EnumRuneType.WATER);
		this.addRune(components, 1, 0, 1, EnumRuneType.DUSK);
		this.addRune(components, 1, 0, 3, EnumRuneType.EARTH);
		this.addRune(components, 2, 0, -4, EnumRuneType.AIR);
		this.addRune(components, 2, 0, -2, EnumRuneType.DUSK);
		this.addRune(components, 2, 0, 0, EnumRuneType.WATER);
		this.addRune(components, 2, 0, 2, EnumRuneType.DUSK);
		this.addRune(components, 2, 0, 4, EnumRuneType.EARTH);
		this.addRune(components, 3, 0, -3, EnumRuneType.DUSK);
		this.addRune(components, 3, 0, -1, EnumRuneType.WATER);
		this.addRune(components, 3, 0, 1, EnumRuneType.WATER);
		this.addRune(components, 3, 0, 3, EnumRuneType.DUSK);
		this.addRune(components, 4, 0, -2, EnumRuneType.WATER);
		this.addRune(components, 4, 0, 0, EnumRuneType.WATER);
		this.addRune(components, 4, 0, 2, EnumRuneType.WATER);
		return components;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualUnmaking();
	}

}