package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.items.*;
import com.teamdman.animus.items.sigils.*;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
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
	public static final Item KAMAWOOD = Items.AIR;
	public static final Item KAMASTONE = Items.AIR;
	public static final Item KAMAIRON = Items.AIR;
	public static final Item KAMAGOLD = Items.AIR;
	public static final Item KAMADIAMOND = Items.AIR;
	public static final Item KAMABOUND = Items.AIR;
	public static final Item ALTARDIVINER = Items.AIR;
	public static final Item MOBSOUL = Items.AIR;
	public static final Item FRAGMENTHEALING = Items.AIR;
	//	public static final Item KEYBINDING = Items.AIR;
	public static final Item SIGILCHAINS = Items.AIR;
	public static final Item SIGILTRANSPOSITION = Items.AIR;
	public static final Item SIGILBUILDER = Items.AIR;
	public static final Item SIGILCONSUMPTION = Items.AIR;
	public static final Item SIGILSTORM = Items.AIR;
	public static final Item SIGILLEECH = Items.AIR;

	public static List<Item> items;

	@SubscribeEvent
	@SuppressWarnings("unused")
	public static void registerItems(RegistryEvent.Register<Item> event) {
		items = new ArrayList<>();

		AnimusBlocks.blocks.forEach(b -> {
			if (Item.getItemFromBlock(b) instanceof ItemBlock){
				items.add(Item.getItemFromBlock(b).setRegistryName(b.getRegistryName()));
			}
		});

		if (!AnimusConfig.itemBlacklist.contains("animus:kama_wood"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.WOOD), "kama_wood"));
		if (!AnimusConfig.itemBlacklist.contains("animus:kama_stone"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.STONE), "kama_stone"));
		if (!AnimusConfig.itemBlacklist.contains("animus:kama_iron"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.IRON), "kama_iron"));
		if (!AnimusConfig.itemBlacklist.contains("animus:kama_gold"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.GOLD), "kama_gold"));
		if (!AnimusConfig.itemBlacklist.contains("animus:kama_diamond"))
			items.add(setupItem(new ItemKama(Item.ToolMaterial.DIAMOND), "kama_diamond"));
		if (!AnimusConfig.itemBlacklist.contains("animus:kama_bound"))
			items.add(setupItem(new ItemKamaBound(), "kama_bound"));
		if (!AnimusConfig.itemBlacklist.contains("animus:altardiviner"))
			items.add(setupItem(new ItemAltarDiviner(), "altardiviner"));
		if (!AnimusConfig.itemBlacklist.contains("animus:sigil)chains"))
			items.add(setupItem(new ItemSigilChains(), "sigil_chains"));
		if (!AnimusConfig.itemBlacklist.contains("animus:sigil_transposition"))
			items.add(setupItem(new ItemSigilTransposition(), "sigil_transposition"));
		if (!AnimusConfig.itemBlacklist.contains("animus:sigil_builder"))
			items.add(setupItem(new ItemSigilBuilder(), "sigil_builder"));
		if (!AnimusConfig.itemBlacklist.contains("animus:sigil_consumption"))
			items.add(setupItem(new ItemSigilConsumption(), "sigil_consumption"));
		if (!AnimusConfig.itemBlacklist.contains("animus:fragmenthealing"))
			items.add(setupItem(new ItemFragmentHealing(), "fragmenthealing"));
		if (!AnimusConfig.itemBlacklist.contains("animus:sigil_storm"))
			items.add(setupItem(new ItemSigilStorm(), "sigil_storm"));
		if (!AnimusConfig.itemBlacklist.contains("animus:sigil_leech"))
			items.add(setupItem(new ItemSigilLeech(), "sigil_leech"));
		items.add(setupItem(new ItemMobSoul(), "mobsoul"));

		items.forEach(event.getRegistry()::register);
	}


	private static Item setupItem(Item item, String name) {
		if (AnimusConfig.itemBlacklist.contains(name))
			return item;
		if (item.getRegistryName() == null)
			item.setRegistryName(name);
		System.out.println(item.getUnlocalizedName());
		item.setUnlocalizedName(name);
		System.out.println(item.getUnlocalizedName());
		item.setCreativeTab(Animus.tabMain);
		//GameRegistry.register(item);
//		Animus.proxy.tryHandleItemModel(item, name);

		return item;
		//TODO: Animus Config Blacklist
	}


	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		AnimusItems.items.forEach(Animus.proxy::tryHandleItemModel);
		AnimusBlocks.blocks.forEach(Animus.proxy::tryHandleBlockModel);
	}

}
