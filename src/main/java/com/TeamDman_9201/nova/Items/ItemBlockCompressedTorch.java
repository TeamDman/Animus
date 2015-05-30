package com.teamdman_9201.nova.items;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemBlockCompressedTorch extends ItemBlock {
    Block cTorch;

    public ItemBlockCompressedTorch(Block arg) {
        super(NOVA.blockCompressedTorch);
        cTorch = NOVA.blockCompressedTorch;
    }

    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("Torches")) {
            list.add("Torches: " + itemStack.getTagCompound().getLong("Torches"));
            // list.add(EnumChatFormatting.GREEN + "code: " + code);
        }
    }
}