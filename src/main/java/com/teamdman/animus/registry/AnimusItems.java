package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.items.*;
import com.teamdman.animus.items.sigils.*;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TeamDman on 9/18/2016.
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID)
@GameRegistry.ObjectHolder(Constants.Mod.MODID)
public class AnimusItems {
	public static final Item kamaWood = Items.AIR;
	public static final Item kamaStone = Items.AIR;
	public static final Item kamaIron = Items.AIR;
	public static final Item kamaGold = Items.AIR;
	public static final Item kamaDiamond = Items.AIR;
	public static final Item kamaBound = Items.AIR;
	public static final Item altarDiviner = Items.AIR;
	public static final Item mobSoul = Items.AIR;
	public static final Item fragmentHealing = Items.AIR;
	//	public static final Item keyBinding = Items.AIR;
	public static final Item sigilChains = Items.AIR;
	public static final Item sigilTransposition = Items.AIR;
	public static final Item sigilBuilder = Items.AIR;
	public static final Item sigilConsumption = Items.AIR;
	public static final Item sigilStorm = Items.AIR;
	public static final Item sigilLeech = Items.AIR;

	public static List<Item> items;

	@SubscribeEvent
	@SuppressWarnings("unused")
	public static void registerItems(RegistryEvent.Register<Item> event) {
		items = new ArrayList<>();

		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamawood"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.WOOD), "itemkamawood"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamastone"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.STONE), "itemkamastone"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamairon"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.IRON), "itemkamairon"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamagold"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.GOLD), "itemkamagold"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamadiamond"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.DIAMOND), "itemkamadiamond"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamabound"))
			items.add(setupItem(new ItemKamaBound(), "itemkamabound"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemaltardiviner"))
			items.add(setupItem(new ItemAltarDiviner(), "itemaltardiviner"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilchains"))
			items.add(setupItem(new ItemSigilChains(), "itemsigilchains"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigiltransposition"))
			items.add(setupItem(new ItemSigilTransposition(), "itemsigiltransposition"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilbuilder"))
			items.add(setupItem(new ItemSigilBuilder(), "itemsigilbuilder"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilconsumption"))
			items.add(setupItem(new ItemSigilConsumption(), "itemsigilconsumption"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemfragmenthealing"))
			items.add(setupItem(new ItemFragmentHealing(), "itemfragmenthealing"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilstorm"))
			items.add(setupItem(new ItemSigilStorm(), "itemsigilstorm"));
		if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilleech"))
			items.add(setupItem(new ItemSigilLeech(), "itemsigilleech"));
		items.add(setupItem(new ItemMobSoul(), "itemmobsoul"));

		items.forEach(event.getRegistry()::register);
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
