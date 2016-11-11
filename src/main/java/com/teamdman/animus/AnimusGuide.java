package com.teamdman.animus;

import WayofTime.bloodmagic.registry.ModBlocks;
import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.IPage;
import amerifrance.guideapi.api.impl.Book;
import amerifrance.guideapi.api.impl.abstraction.CategoryAbstract;
import amerifrance.guideapi.api.impl.abstraction.EntryAbstract;
import amerifrance.guideapi.category.CategoryItemStack;
import amerifrance.guideapi.entry.EntryItemStack;
import amerifrance.guideapi.page.PageText;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnimusGuide {
	public static Book book;

	public static void buildEntry(Map<ResourceLocation, EntryAbstract> entries, String identifier, int numpages, ItemStack icon) {
		ArrayList<IPage> pages = new ArrayList<IPage>();
		for (int i=0;i<numpages;i++)
			pages.add(new PageText("guide.animus."+identifier+".page"+i));

		entries.put(new ResourceLocation(Animus.MODID,identifier), new EntryItemStack(pages,"guide.animus."+identifier+".entry",icon));
	}

	public static void buildEntry(Map<ResourceLocation, EntryAbstract> entries,String identifier, int pages, Block icon) {
		buildEntry(entries, identifier,pages, new ItemStack(icon));
	}
	public static void buildEntry(Map<ResourceLocation, EntryAbstract> entries,String identifier, int pages, Item icon) {
		buildEntry(entries, identifier,pages, new ItemStack(icon));
	}

	public static CategoryAbstract buildRituals() {
		Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<ResourceLocation, EntryAbstract>();
		buildEntry(entries,"rituals.sol",1,Blocks.GLOWSTONE);
		buildEntry(entries,"rituals.luna",1, Items.COAL);
		buildEntry(entries,"rituals.entropy",1, Blocks.COBBLESTONE);
		buildEntry(entries,"rituals.unmaking",1, Items.ENCHANTED_BOOK);
		buildEntry(entries,"rituals.peace",1, Items.SPAWN_EGG);
		buildEntry(entries,"rituals.culling",1, Items.DIAMOND_SWORD);
		buildEntry(entries,"rituals.leech",1, Blocks.LEAVES);

		return new CategoryItemStack(entries,"guide.animus.category.rituals",new ItemStack(ModBlocks.ritualController));
	}

	public static void buildGuide() {
		book = new Book();
		List<CategoryAbstract> categories = new ArrayList<CategoryAbstract>();
		categories.add(buildRituals());

		book.setCategoryList(categories);
		book.setTitle("guide.animus.title");
		book.setWelcomeMessage("guide.animus.welcome");
		book.setRegistryName(Animus.MODID);
		book.setDisplayName("guide.animus.displayname");
		book.setColor(Color.RED); //cube wants 1DEAF6

		GameRegistry.register(book);
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
			GuideAPI.setModel(book);
	}
}
