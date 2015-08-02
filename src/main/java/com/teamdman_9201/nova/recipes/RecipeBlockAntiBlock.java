package com.teamdman_9201.nova.recipes;

import com.teamdman_9201.nova.NOVA;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Created by TeamDman on 2015-07-30.
 */
public class RecipeBlockAntiBlock implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting crafting, World world) {
        for (int i=0;i<crafting.getSizeInventory();i++) {
            if (crafting.getStackInSlot(i) != null && i >1)
                return false;
        }
        if (crafting.getStackInSlot(0) == null || crafting.getStackInSlot(1) == null)
            return false;
        if (crafting.getStackInSlot(0).getItem() == ItemBlock.getItemFromBlock(NOVA.blockAntiBlock))
            if (crafting.getStackInSlot(1).getItem() != ItemBlock.getItemFromBlock(NOVA.blockAntiBlock) && crafting.getStackInSlot(1).getItem() instanceof ItemBlock)
                return true;
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting crafting) {
    	
    	
    	
    	ItemStack returnStack   = new ItemStack(NOVA.blockAntiBlock);
        NBTTagCompound nbtData = new NBTTagCompound();
        
        
        nbtData.setInteger("ID", Block.getIdFromBlock(Block.getBlockFromItem(crafting.getStackInSlot(1).getItem()))  );
        returnStack.setTagCompound(nbtData);
        return returnStack;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(NOVA.blockAntiBlock);
    }
}
