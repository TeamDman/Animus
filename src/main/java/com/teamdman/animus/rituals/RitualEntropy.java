package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.Utils;
import com.teamdman.animus.Animus;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.*;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualEntropy extends Ritual {
	public static final String EFFECT_RANGE = "effect";
	public static final String CHEST_RANGE = "chest";
	HashMap<Item, Integer> indexed = new HashMap<Item, Integer>();

	public RitualEntropy() {
		super("ritualEntropy", 0, 1000, "ritual." + Animus.MODID + ".entropyRitual");

		addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));
		setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World world = masterRitualStone.getWorldObj();
		SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		int currentEssence = network.getCurrentEssence();
		BlockPos masterPos = masterRitualStone.getBlockPos();
		AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
		TileEntity tileInventory = world.getTileEntity(chestRange.getContainedPositions(masterPos).get(0));


		if (!masterRitualStone.getWorldObj().isRemote && tileInventory != null && tileInventory instanceof IInventory) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}
			for (int slot=0;slot<((IInventory) tileInventory).getSizeInventory();slot++) {
				ItemStack stack = ((IInventory) tileInventory).getStackInSlot(slot);
				if (stack==null)
					continue;
				if (!stack.isItemEqual(new ItemStack(Blocks.COBBLESTONE))) {
					int cobble = getCobbleValue(new ArrayList<Item>(),stack,0);
					if (cobble>0) {
						((IInventory) tileInventory).decrStackSize(slot,1);
						while (cobble>0) {
							Utils.insertStackIntoInventory(new ItemStack(Blocks.COBBLESTONE,cobble>64?64:cobble),(IInventory) tileInventory, EnumFacing.UP);
							cobble-=cobble>64?64:cobble;
						}
					}
				}
			}
		}
	}

	public int getCobbleValue(List<Item> fetchList, ItemStack input, int layer) {
		System.out.printf("%s requested on layer %d\n",input.getDisplayName(),layer);
		if (indexed.get(input.getItem()) != null)
			return indexed.get(input.getItem());
		if (fetchList.contains(input.getItem()))
			return 1;
		if (layer > 8)
			return 0;
		layer++;
		fetchList.add(input.getItem());
		int rtn = 1;
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		for (IRecipe recipe : recipes) {
			if (recipe.getRecipeOutput() != null && recipe.getRecipeOutput().isItemEqual(input)) {
				rtn += recipe.getRecipeSize();
				List components;
				if (recipe instanceof ShapelessRecipes) {
					components = ((ShapelessRecipes) recipe).recipeItems;
				} else if (recipe instanceof ShapedRecipes) {
					components = Arrays.asList(((ShapedRecipes) recipe).recipeItems);
				} else if (recipe instanceof ShapedOreRecipe) {
					components = Arrays.asList(((ShapedOreRecipe) recipe).getInput());
				} else {
					continue;
				}
				if (components == null) {
					continue;
				}
				Iterator iter = components.iterator();
				while (iter.hasNext()) {
					Object recipeItem = (iter.next());
					if (recipeItem instanceof ItemStack) {
						rtn += getCobbleValue(fetchList, (ItemStack) recipeItem, layer);
					} else if (recipeItem instanceof Collection) {
						if (((Collection) recipeItem).contains(new ItemStack(Blocks.COBBLESTONE))) {
							rtn += 1;
							continue;
						} else {
							Collection recipeItemCollection = ((Collection) recipeItem);
							int value = -1;
							for (Object option : recipeItemCollection) {
								if (option instanceof ItemStack) {
									int v = getCobbleValue(fetchList, (ItemStack) option, layer);
									value = (value == -1 || v < value && v > 1) ? v : value;//value < v ? v : value;
								}
							}
							rtn += (value == -1 ? 1 : value);
						}
					}
				}
				break;
			}
		}
		System.out.printf("Returning %d for item %s on layer %d\n",rtn,input.getDisplayName(),layer);
		indexed.put(input.getItem(), new Integer(rtn));
		return rtn;
	}


	@Override
	public int getRefreshCost() {
		return 1;
	}

	@Override
	public int getRefreshTime() {
		return 1;
	}

	@Override
	public ArrayList<RitualComponent> getComponents() {
		ArrayList<RitualComponent> components = new ArrayList();
		this.addCornerRunes(components, 1, 1, EnumRuneType.EARTH);
		this.addCornerRunes(components, 2, 0, EnumRuneType.FIRE);
		this.addCornerRunes(components, 3, -1, EnumRuneType.WATER);

		return components;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualEntropy();
	}

}