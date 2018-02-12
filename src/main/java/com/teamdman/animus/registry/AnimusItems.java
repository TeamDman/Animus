package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.items.*;
import com.teamdman.animus.items.sigils.*;
import net.minecraft.item.Item;
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
	public static Item fragmentHealing;
	public static Item keyBinding;

	public static Item sigilChains;
	public static Item sigilTransposition;
	public static Item sigilBuilder;
	public static Item sigilConsumption;
	public static Item sigilStorm;
	public static Item sigilLeech;

	public static void init() {
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamawood"))
		kamaWood = setupItem(new ItemKama(Item.ToolMaterial.WOOD), "itemkamawood");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamastone"))		
		kamaStone = setupItem(new ItemKama(Item.ToolMaterial.STONE), "itemkamastone");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamairon"))
		kamaIron = setupItem(new ItemKama(Item.ToolMaterial.IRON), "itemkamairon");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamagold"))
		kamaGold = setupItem(new ItemKama(Item.ToolMaterial.GOLD), "itemkamagold");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamadiamond"))
		kamaDiamond = setupItem(new ItemKama(Item.ToolMaterial.DIAMOND), "itemkamadiamond");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamabound"))
		kamaBound = setupItem(new ItemKamaBound(), "itemkamabound");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemaltardiviner"))
		altarDiviner = setupItem(new ItemAltarDiviner(), "itemaltardiviner");
		mobSoul = setupItem(new ItemMobSoul(), "itemmobsoul");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilchains"))
		sigilChains = setupItem(new ItemSigilChains(), "itemsigilchains");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigiltransposition"))
		sigilTransposition = setupItem(new ItemSigilTransposition(), "itemsigiltransposition");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilbuilder"))
		sigilBuilder = setupItem(new ItemSigilBuilder(), "itemsigilbuilder");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilconsumption"))
		sigilConsumption = setupItem(new ItemSigilConsumption(), "itemsigilconsumption");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemfragmenthealing"))
		fragmentHealing = setupItem(new ItemFragmentHealing(), "itemfragmenthealing");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilstorm"))
		sigilStorm = setupItem(new ItemSigilStorm(), "itemsigilstorm");
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilleech"))
		sigilLeech = setupItem(new ItemSigilLeech(), "itemsigilleech");
	}

	private static Item setupItem(Item item, String name) {
		if (AnimusConfig.itemBlacklist.contains(name))
			return item;
		if (item.getRegistryName() == null)
			item.setRegistryName(name);
		item.setUnlocalizedName(name);
		item.setCreativeTab(Animus.tabMain);
		//GameRegistry.register(item);
		Animus.proxy.tryHandleItemModel(item, name);

		return item;
		//TODO: Animus Config Blacklist
	}

	@SideOnly(Side.CLIENT)
	public static void initRenders() {

	}

}
