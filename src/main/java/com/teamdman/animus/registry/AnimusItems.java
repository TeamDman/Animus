package com.teamdman.animus.registry;

import WayofTime.bloodmagic.item.ItemEnum;
import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.items.*;
import com.teamdman.animus.items.sigils.*;
import com.teamdman.animus.types.ComponentTypes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TeamDman on 9/18/2016.
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID)
@GameRegistry.ObjectHolder(Constants.Mod.MODID)
public class AnimusItems {
	public static final Item ALTARDIVINER        = Items.AIR;
	public static final Item COMPONENT           = Items.AIR;
	public static final Item FRAGMENTHEALING     = Items.AIR;
	public static final Item KAMA_BOUND          = Items.AIR;
	public static final Item KAMA_DIAMOND        = Items.AIR;
	public static final Item KAMA_GOLD           = Items.AIR;
	public static final Item KAMA_IRON           = Items.AIR;
	public static final Item KAMA_STONE          = Items.AIR;
	public static final Item KAMA_WOOD           = Items.AIR;
	public static final Item KEYBINDING          = Items.AIR;
	public static final Item MOBSOUL             = Items.AIR;
	public static final Item SIGIL_BUILDER       = Items.AIR;
	public static final Item SIGIL_CHAINS        = Items.AIR;
	public static final Item SIGIL_CONSUMPTION   = Items.AIR;
	public static final Item SIGIL_LEECH         = Items.AIR;
	public static final Item SIGIL_STORM         = Items.AIR;
	public static final Item SIGIL_TRANSPOSITION = Items.AIR;

	public static List<Item> items;

	@SubscribeEvent
	@SuppressWarnings("unused")
	public static void registerItems(RegistryEvent.Register<Item> event) {
		items = new ArrayList<>();

		AnimusBlocks.blocks.forEach(b -> {
			if (Item.getItemFromBlock(b) instanceof ItemBlock) {
				//noinspection ConstantConditions
				items.add(Item.getItemFromBlock(b).setRegistryName(b.getRegistryName()));
			}
		});

		items.add(setupItem(new ItemKama(Item.ToolMaterial.WOOD), "kama_wood"));
		items.add(setupItem(new ItemKama(Item.ToolMaterial.STONE), "kama_stone"));
		items.add(setupItem(new ItemKama(Item.ToolMaterial.IRON), "kama_iron"));
		items.add(setupItem(new ItemKama(Item.ToolMaterial.GOLD), "kama_gold"));
		items.add(setupItem(new ItemKama(Item.ToolMaterial.DIAMOND), "kama_diamond"));
		items.add(setupItem(new ItemKamaBound(), "kama_bound"));
		items.add(setupItem(new ItemAltarDiviner(), "altardiviner"));
		items.add(setupItem(new ItemSigilChains(), "sigil_chains"));
		items.add(setupItem(new ItemSigilTransposition(), "sigil_transposition"));
		items.add(setupItem(new ItemSigilBuilder(), "sigil_builder"));
		items.add(setupItem(new ItemSigilConsumption(), "sigil_consumption"));
		items.add(setupItem(new ItemFragmentHealing(), "fragmenthealing"));
		items.add(setupItem(new ItemSigilStorm(), "sigil_storm"));
		items.add(setupItem(new ItemSigilLeech(), "sigil_leech"));
		items.add(setupItem(new ItemKeyBinding(), "keybinding"));
		items.add(setupItem(new ItemMobSoul(), "mobsoul"));
		items.add(setupItem(new ItemEnum.Variant<>(ComponentTypes.class, "baseComponent"), "component"));
		items.forEach(event.getRegistry()::register);
	}


	private static Item setupItem(Item item, String name) {
		//		if (AnimusConfig.itemBlacklist.contains(name))
		//			return item;
		if (item.getRegistryName() == null)
			item.setRegistryName(name);
		item.setUnlocalizedName(name);
		item.setCreativeTab(Animus.tabMain);
		return item;
		//TODO: Animus Config Blacklist
	}
}
