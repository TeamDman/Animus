package com.teamdman.animus.registry;

import WayofTime.bloodmagic.block.IBMBlock;
import WayofTime.bloodmagic.item.ItemEnum;
import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.items.*;
import com.teamdman.animus.items.sigils.*;
import com.teamdman.animus.types.ComponentTypes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
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
	public static final Item BLOOD_APPLE		 = Items.AIR;
	public static final Item ACTIVATION_SIGIL_FRAGILE	= Items.AIR;

	public static List<Item> items;

	@SubscribeEvent
	@SuppressWarnings("unused")
	public static void registerItems(RegistryEvent.Register<Item> event) {
		items = new ArrayList<>();

		//noinspection ConstantConditions
		AnimusBlocks.blocks.stream()
				.filter(b -> b instanceof IBMBlock)
				.forEach(b -> setupItem(((IBMBlock) b).getItem(), b.getRegistryName().getPath()));

		setupItem(new ItemKama(Item.ToolMaterial.WOOD), "kama_wood");
		setupItem(new ItemKama(Item.ToolMaterial.STONE), "kama_stone");
		setupItem(new ItemKama(Item.ToolMaterial.IRON), "kama_iron");
		setupItem(new ItemKama(Item.ToolMaterial.GOLD), "kama_gold");
		setupItem(new ItemKama(Item.ToolMaterial.DIAMOND), "kama_diamond");
		setupItem(new ItemKamaBound(), "kama_bound");
		setupItem(new ItemAltarDiviner(), "altardiviner");
		setupItem(new ItemSigilChains(), "sigil_chains");
		setupItem(new ItemSigilTransposition(), "sigil_transposition");
		setupItem(new ItemSigilBuilder(), "sigil_builder");
		setupItem(new ItemSigilConsumption(), "sigil_consumption");
		setupItem(new ItemFragmentHealing(), "fragmenthealing");
		setupItem(new ItemSigilStorm(), "sigil_storm");
		setupItem(new ItemSigilLeech(), "sigil_leech");
		setupItem(new ItemKeyBinding(), "keybinding");
		setupItem(new ItemMobSoul(), "mobsoul");
		setupItem(new ItemBloodApple(), "bloodapple");
		setupItem(new ItemActivationCrystalFragile(), "activationcrystalfragile");
		setupItem(new ItemEnum.Variant<>(ComponentTypes.class, "baseComponent"), "component");
		items.forEach(event.getRegistry()::register);
	}


	private static void setupItem(Item item, String name) {
		//		if (AnimusConfig.itemBlacklist.contains(name))
		//			return item;
		if (item.getRegistryName() == null)
			item.setRegistryName(name);
		item.setTranslationKey(name);
		item.setCreativeTab(Animus.tabMain);
		items.add(item);
		//TODO: Animus Config Blacklist
	}
}
