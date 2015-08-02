package com.teamdman_9201.nova.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.List;

/**
 * Created by TeamDman on 2015-07-30.
 */
public class ItemBlockAntiBlock extends ItemBlock {
	
	private Block myBlock;
	private String blockName;
	
    public ItemBlockAntiBlock(Block p_i45328_1_) {
        super(p_i45328_1_);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void addInformation(ItemStack stack, EntityPlayer player, List data, boolean wut) {
    	
        if (stack.hasTagCompound() && stack.getTagCompound().getString("ID") != null)
        	
        	myBlock = Block.getBlockById(stack.getTagCompound().getInteger("ID"));
        	blockName = myBlock.getUnlocalizedName();
        	
        	data.add("Replacing: " + StatCollector.translateToLocal(blockName));            		
    }
}
