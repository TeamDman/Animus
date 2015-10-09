package com.teamdman_9201.nova.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Created by TeamDman on 2015-10-09.
 */
public class ItemHealingFragment extends Item {
    public ItemHealingFragment() {
        setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List data, boolean p_77624_4_) {
        data.add("More the merrier!");
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        return false;
    }

}
