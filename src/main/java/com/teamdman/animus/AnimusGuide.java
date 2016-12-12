package com.teamdman.animus;

import WayofTime.bloodmagic.registry.ModBlocks;
import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.IPage;
import amerifrance.guideapi.api.impl.Book;
import amerifrance.guideapi.api.impl.abstraction.CategoryAbstract;
import amerifrance.guideapi.api.impl.abstraction.EntryAbstract;
import amerifrance.guideapi.category.CategoryItemStack;
import amerifrance.guideapi.entry.EntryItemStack;
import amerifrance.guideapi.page.PageIRecipe;
import amerifrance.guideapi.page.PageText;
import com.teamdman.animus.items.ItemAltarDiviner;
import com.teamdman.animus.items.ItemKama;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusRecipes;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
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

	private static void buildEntry(Map<ResourceLocation, EntryAbstract> entries, String identifier, int numpages, ItemStack icon, List<IPage> extras) {
		ArrayList<IPage> pages = new ArrayList<IPage>();
		for (int i = 0; i < numpages; i++)
			pages.add(new PageText("guide.animus." + identifier + ".page" + i));

		if (extras != null)
			extras.forEach((pages::add));
		entries.put(new ResourceLocation(Animus.MODID, identifier), new EntryItemStack(pages, "guide.animus." + identifier + ".entry", icon));
	}



	@SuppressWarnings("unused")
	private static void buildEntry(Map<ResourceLocation, EntryAbstract> entries, String identifier, int pages, Block icon, List<IPage> extras) {
		buildEntry(entries, identifier, pages, new ItemStack(icon), extras);
	}

	private static void buildEntry(Map<ResourceLocation, EntryAbstract> entries, String identifier, int pages, Item icon, List<IPage> extras) {
		buildEntry(entries, identifier, pages, new ItemStack(icon), extras);
	}

	private static void buildEntry(Map<ResourceLocation, EntryAbstract> entries, String identifier, int pages, Block icon) {
		buildEntry(entries, identifier, pages, new ItemStack(icon), null);
	}

	private static void buildEntry(Map<ResourceLocation, EntryAbstract> entries, String identifier, int pages, Item icon) {
		buildEntry(entries, identifier, pages, new ItemStack(icon), null);
	}

	private static List<IPage> getRecipesPagesFor(Class<?> check) {
		List<IPage> matches = new ArrayList<>();
		CraftingManager.getInstance().getRecipeList().forEach((v) -> {
			try {
				if (v.getRecipeOutput()!=null && check.isInstance(v.getRecipeOutput().getItem()))
					matches.add(new PageIRecipe(v));
			} catch (Exception e) {
				System.out.println("Error fetching recipe to animus guidebook");
			}
		});
		return matches;
	}

	public static void buildGuide() {
		book = new Book();
		List<CategoryAbstract> categories = new ArrayList<CategoryAbstract>();

		Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<>();
		buildEntry(entries, "rituals.sol", 1, Blocks.GLOWSTONE);
		buildEntry(entries, "rituals.luna", 1, Items.COAL);
		buildEntry(entries, "rituals.entropy", 1, Blocks.COBBLESTONE);
		buildEntry(entries, "rituals.unmaking", 1, Items.ENCHANTED_BOOK);
		buildEntry(entries, "rituals.peace", 1, Items.SPAWN_EGG);
		buildEntry(entries, "rituals.culling", 1, Items.DIAMOND_SWORD);
		buildEntry(entries, "rituals.leech", 1, Blocks.LEAVES);
		buildEntry(entries, "rituals.regression", 1, Blocks.ANVIL);
		categories.add(new CategoryItemStack(entries, "guide.animus.category.rituals", new ItemStack(ModBlocks.RITUAL_CONTROLLER)));
		entries = new LinkedHashMap<>();

		buildEntry(entries, "sigils.chains", 1, AnimusItems.sigilChains);
		buildEntry(entries, "sigils.transposition", 1, AnimusItems.sigilTransposition);
		buildEntry(entries, "sigils.builder", 1, AnimusItems.sigilBuilder);
		buildEntry(entries, "sigils.consumption", 1, AnimusItems.sigilConsumption);
		categories.add(new CategoryItemStack(entries, "guide.animus.category.sigils", new ItemStack(AnimusItems.sigilBuilder)));
		entries = new LinkedHashMap<>();

		buildEntry(entries, "blocks.phantom", 1, AnimusBlocks.phantomBuilder);
		categories.add(new CategoryItemStack(entries, "guide.animus.category.blocks", new ItemStack(AnimusBlocks.phantomBuilder)));
		entries = new LinkedHashMap<>();

		buildEntry(entries, "items.kama", 1, AnimusItems.kamaBound, getRecipesPagesFor(ItemKama.class));
		buildEntry(entries, "items.altardiviner", 1, AnimusItems.altarDiviner, getRecipesPagesFor(ItemAltarDiviner.class));
		buildEntry(entries, "items.mobsoul", 1, AnimusItems.mobSoul);
		buildEntry(entries, "items.fragmenthealing", 1, AnimusItems.fragmentHealing);
		categories.add(new CategoryItemStack(entries, "guide.animus.category.items", new ItemStack(AnimusItems.kamaBound)));

		book.setCategoryList(categories);
		book.setTitle("guide.animus.title");
		book.setWelcomeMessage("guide.animus.welcome");
		book.setRegistryName(Animus.MODID);
		book.setDisplayName("guide.animus.displayname");
		book.setColor(Color.RED); //cube wants 1DEAF6

		GameRegistry.register(book);
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
			GuideAPI.setModel(book);
		AnimusRecipes.addGuideRecipe();
	}
}
