package com.teamdman_9201.nova.rituals;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import WayofTime.alchemicalWizardry.api.alchemy.energy.ReagentRegistry;
import WayofTime.alchemicalWizardry.api.rituals.IMasterRitualStone;
import WayofTime.alchemicalWizardry.api.rituals.RitualComponent;
import WayofTime.alchemicalWizardry.api.rituals.RitualEffect;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import WayofTime.alchemicalWizardry.common.spell.complex.effect.SpellHelper;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualEffectEntropy extends RitualEffect {
    HashMap<Item, Integer> indexed = new HashMap<Item, Integer>();
    private int upkeep = NOVA.ritualData.get("upkeepEntropy");

    public RitualEffectEntropy() {
        indexed.put(ItemBlock.getItemFromBlock(Blocks.cobblestone), 1);
    }

    public int getComponents(ItemStack origin, ItemStack input, int layer) {
        if (indexed.get(input.getItem()) != null)
            return indexed.get(input.getItem());
        if (input.getItem() == origin.getItem() && layer != 0)
            return 1;
        if (layer > 8)
            return 0;
        layer++;
        int           rtn     = 1;
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
                        rtn += getComponents(origin, (ItemStack) recipeItem, layer);
                    } else if (recipeItem instanceof Collection) {
                        if (((Collection) recipeItem).contains(new ItemStack(Blocks.cobblestone))) {
                            rtn+=1;
                            continue;
                        } else {
                            Collection recipeItemCollection = ((Collection) recipeItem);
                            int value = -1;
                            for (Object option : recipeItemCollection) {
                                if (option instanceof ItemStack) {
                                    int v = getComponents(origin, (ItemStack) option, layer);
                                    value = (value == -1  || v < value && v > 1) ? v : value;//value < v ? v : value;
                                }
                            }
                            rtn += (value==-1?1:value);
                        }
                    }
                }
                break;
            }
        }
        //TODO: Fix container desync [sendinventorytoplayer function?]
        //TODO: Add config support for ritual costs, etc
        indexed.put(input.getItem(), new Integer(rtn));
        return rtn;
    }

    public int getCostPerRefresh() {
        return upkeep;
    }

    @Override
    public List<RitualComponent> getRitualComponentList() {
        ArrayList<RitualComponent> ritualBlocks = new ArrayList();
        ritualBlocks.add(new RitualComponent(-3,0,-3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-3,0,-2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-3,0,2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-3,0,3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-2,0,-3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-2,0,-2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-2,0,2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-2,0,3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-1,0,0, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(0,0,-1, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(0,0,1, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(1,0,0, RitualComponent.WATER));
        ritualBlocks.add(new RitualComponent(2,0,-3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(2,0,-2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(2,0,2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(2,0,3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(3,0,-3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(3,0,-2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(3,0,2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(3,0,3, RitualComponent.EARTH));
        return ritualBlocks;
    }

    @Override
    public void performEffect(IMasterRitualStone ritualStone) {
        String owner          = ritualStone.getOwner();
        World  world          = ritualStone.getWorld();
        int    x              = ritualStone.getXCoord();
        int    y              = ritualStone.getYCoord();
        int    z              = ritualStone.getZCoord();
        int    currentEssence = SoulNetworkHandler.getCurrentEssence(owner);
        if (currentEssence < this.getCostPerRefresh()) {
            EntityPlayer entityOwner = SpellHelper.getPlayerForUsername(owner);
            if (entityOwner == null) {
                return;
            }
            SoulNetworkHandler.causeNauseaToPlayer(owner);
        } else {
            TileEntity tile = world.getTileEntity(x, y + 1, z);
            if (tile instanceof IInventory) {
                IInventory chest = (IInventory) tile;
                if (chest.getSizeInventory() > 0) {
                    for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
                        if (chest.getStackInSlot(slot) != null && chest.getStackInSlot(slot).getItem() == ItemBlock.getItemFromBlock(Blocks.cobblestone)) {
                            return; //Force cobble output to be empty before operations, easier on server
                        }
                    }
                    main:
                    for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
                        ItemStack stack = chest.getStackInSlot(slot);
                        if (stack == null || stack.getItem() == ItemBlock.getItemFromBlock(Blocks.cobblestone))
                            continue;
                        int cobble = getComponents(stack, stack, 0);
                        System.out.printf("Object %s is worth %d cobble * %d\n", stack.getDisplayName(), cobble, stack.stackSize);
                        for (int i = 0; i <= stack.stackSize; i++) {
                            int buffer = cobble;
                            while (buffer > 0) {
                                SpellHelper.insertStackIntoInventory(new ItemStack(Blocks.cobblestone, buffer > 64 ? 64 : buffer), chest, ForgeDirection.UP);
                                buffer -= 64;
                            }
                            SoulNetworkHandler.syphonFromNetwork(owner, cobble * 5);
                            chest.decrStackSize(slot, 1);
                            Boolean hasPotentia = this.canDrainReagent(ritualStone, ReagentRegistry.potentiaReagent, 1, false);
                            if (hasPotentia) {
                                this.canDrainReagent(ritualStone, ReagentRegistry.potentiaReagent, 1, true);
                            } else {
                                break main;
                            }
                        }
                    }
                }
            }
        }
        if (world.rand.nextInt(10) == 0) {
            SpellHelper.sendIndexedParticleToAllAround(world, x, y, z, 20, world.provider.dimensionId, 1, x, y, z);
        }
    }
}



