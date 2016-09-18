package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.items.ItemKama;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by User on 9/18/2016.
 */
public class AnimusItems {
    public static Item kamaWood;

    public static void init() {
        kamaWood = setupItem(new ItemKama(Item.ToolMaterial.WOOD),"itemKamaWood",Animus.tabMain);
    }

    private static Item setupItem(Item item, String name, CreativeTabs tab) {
        if (item.getRegistryName() == null)
            item.setRegistryName(name);
        GameRegistry.register(item);

        item.setUnlocalizedName(name);
        item.setCreativeTab(tab);
//        theItem.setTextureName(Animus.MODID + ":" + theName);
        //TODO: Animus Config Blacklist
        //AnimusConfig.blacklist.add(theName);
        return item;
    }
}
