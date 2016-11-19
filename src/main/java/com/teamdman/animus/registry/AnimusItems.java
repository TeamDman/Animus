package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.items.*;
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
	public static Item fragmentHealing;

	public static void init() {
		kamaWood = setupItem(new ItemKama(Item.ToolMaterial.WOOD), "itemkamawood");
		kamaStone = setupItem(new ItemKama(Item.ToolMaterial.STONE), "itemkamastone");
		kamaIron = setupItem(new ItemKama(Item.ToolMaterial.IRON), "itemkamairon");
		kamaGold = setupItem(new ItemKama(Item.ToolMaterial.GOLD), "itemkamagold");
		kamaDiamond = setupItem(new ItemKama(Item.ToolMaterial.DIAMOND), "itemkamadiamond");
		kamaBound = setupItem(new ItemKamaBound(), "itemkamabound");
		altarDiviner = setupItem(new ItemAltarDiviner(), "itemaltardiviner");
		mobSoul = setupItem(new ItemMobSoul(), "itemmobsoul");
		sigilChains = setupItem(new ItemSigilChains(), "itemsigilchains");
		sigilTransposition = setupItem(new ItemSigilTransposition(), "itemsigiltransposition");
		sigilBuilder = setupItem(new ItemSigilBuilder(), "itemsigilbuilder");
		fragmentHealing = setupItem(new ItemFragmentHealing(), "itemfragmenthealing");
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
