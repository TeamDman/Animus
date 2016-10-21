package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.items.ItemAltarDiviner;
import com.teamdman.animus.items.ItemKama;
import com.teamdman.animus.items.ItemKamaBound;
import com.teamdman.animus.items.ItemMobSoul;
import com.teamdman.animus.items.sigils.ItemSigilBuilder;
import com.teamdman.animus.items.sigils.ItemSigilChains;
import com.teamdman.animus.items.sigils.ItemSigilTransposition;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 9/18/2016.
 */
public class AnimusItems {
    public static Item kamaWood;
    public static Item kamaStone;
    public static Item kamaIron;
    public static Item kamaGold;
    public static Item kamaDiamond;
    public static Item kamaBound;
	public static Item altarDiviner;
    public static Item mobSoul;
    public static Item sigilChains;
    public static Item sigilTransposition;
    public static Item sigilBuilder;

    public static void init() {
        kamaWood = setupItem(new ItemKama(Item.ToolMaterial.WOOD), "itemKamaWood");
        kamaStone = setupItem(new ItemKama(Item.ToolMaterial.STONE), "itemKamaStone");
        kamaIron = setupItem(new ItemKama(Item.ToolMaterial.IRON), "itemKamaIron");
        kamaGold = setupItem(new ItemKama(Item.ToolMaterial.GOLD), "itemKamaGold");
        kamaDiamond = setupItem(new ItemKama(Item.ToolMaterial.DIAMOND), "itemKamaDiamond");
        kamaBound = setupItem(new ItemKamaBound(), "itemKamaBound");
		altarDiviner = setupItem(new ItemAltarDiviner(), "itemAltarDiviner");
        mobSoul=setupItem(new ItemMobSoul(), "itemMobSoul");
        sigilChains=setupItem(new ItemSigilChains(), "itemSigilChains");
        sigilTransposition=setupItem(new ItemSigilTransposition(), "itemSigilTransposition");
        sigilBuilder=setupItem(new ItemSigilBuilder(), "itemSigilBuilder");

    }

    private static Item setupItem(Item item, String name) {
        if (AnimusConfig.itemBlacklist.contains(name))
            return item;
        if (item.getRegistryName() == null)
            item.setRegistryName(name);
        item.setUnlocalizedName(name);
        item.setCreativeTab(Animus.tabMain);
        GameRegistry.register(item);
        Animus.proxy.tryHandleItemModel(item, name);

        return item;
        //TODO: Animus Config Blacklist
    }

    @SideOnly(Side.CLIENT)
    public static void initRenders() {

    }

}
