package com.teamdman_9201.nova;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import WayofTime.alchemicalWizardry.ModItems;
import amerifrance.guideapi.api.GuideRegistry;
import amerifrance.guideapi.api.abstraction.CategoryAbstract;
import amerifrance.guideapi.api.abstraction.EntryAbstract;
import amerifrance.guideapi.api.abstraction.IPage;
import amerifrance.guideapi.api.base.Book;
import amerifrance.guideapi.api.util.BookBuilder;
import amerifrance.guideapi.categories.CategoryItemStack;
import amerifrance.guideapi.entries.EntryText;
import amerifrance.guideapi.pages.PageIRecipe;
import amerifrance.guideapi.pages.PageUnlocText;

/**
 * Created by TeamDman on 2015-07-30.
 */
public class NOVAGuide {
    public static Book myBook;

    public static void buildGuide() {
        //        pages2.add(new PageIRecipe(new ShapedOreRecipe(Items.apple, "AAA", "BBB", "CCC", 'A', "ingotIron", 'B', Blocks.anvil, 'C', Items.potato))); // Create a recipe page and add it to your pages2 list.
        //        pages2.add(new PageFurnaceRecipe("oreGold")); // Create a furnace recipe page and add it to your pages2 list.
        List<EntryAbstract> rituals = new ArrayList<EntryAbstract>();

        ArrayList<IPage> pages1 = new ArrayList<IPage>();
        pages1.add(new PageUnlocText("book.NOVA.rituals.Sol.body"));
        rituals.add(new EntryText(pages1, "Ritual of Sol"));

        ArrayList<IPage> pages2 = new ArrayList<IPage>();
        pages2.add(new PageUnlocText("book.NOVA.rituals.Luna.body"));
        rituals.add(new EntryText(pages2, "Ritual of Luna"));

        ArrayList<IPage> pages3 = new ArrayList<IPage>();
        pages3.add(new PageUnlocText("book.NOVA.rituals.Uncreation.body1"));
        pages3.add(new PageUnlocText("book.NOVA.rituals.Uncreation.body2"));
        rituals.add(new EntryText(pages3, "Ritual of Uncreation"));

        ArrayList<IPage> pages4 = new ArrayList<IPage>();
        pages4.add(new PageUnlocText("book.NOVA.rituals.Entropy.body1"));
        pages4.add(new PageUnlocText("book.NOVA.rituals.Entropy.body2"));
        rituals.add(new EntryText(pages4, "Ritual of Entropy"));

        List<EntryAbstract> items = new ArrayList<EntryAbstract>();
        ArrayList<IPage> pages5 = new ArrayList<IPage>();
        pages5.add(new PageUnlocText("book.NOVA.items.Sickles.body1"));
        pages5.add(new PageIRecipe(new ShapedOreRecipe(NOVA.itemWoodSickle, "AAA", "A B", " B ", 'A', Blocks.planks, 'B', Items.stick)));
        pages5.add(new PageIRecipe(new ShapedOreRecipe(NOVA.itemStoneSickle, "AAA", "A B", " B ", 'A', Blocks.cobblestone, 'B', Items.stick)));
        pages5.add(new PageIRecipe(new ShapedOreRecipe(NOVA.itemIronSickle, "AAA", "A B", " B ", 'A', Items.iron_ingot, 'B', Items.stick)));
        pages5.add(new PageIRecipe(new ShapedOreRecipe(NOVA.itemGoldSickle, "AAA", "A B", " B ", 'A', Items.gold_ingot, 'B', Items.stick)));
        pages5.add(new PageIRecipe(new ShapedOreRecipe(NOVA.itemDiamondSickle, "AAA", "A B", " B ", 'A', Items.diamond, 'B', Items.stick)));
        items.add(new EntryText(pages5, "Sickles"));

        ArrayList<IPage> pages6 = new ArrayList<IPage>();
        pages6.add(new PageUnlocText("book.NOVA.sigils.SigilOfChains.body"));
        items.add(new EntryText(pages6, "Ritual of Entropy"));


        ArrayList<CategoryAbstract> categories = new ArrayList<CategoryAbstract>();
        categories.add(new CategoryItemStack(rituals, "Rituals", new ItemStack(ModItems.activationCrystal)));
        categories.add(new CategoryItemStack(items, "Items", new ItemStack(NOVA.itemBoundSickle)));

        BookBuilder builder = new BookBuilder();
        builder.setCategories(categories);
        builder.setUnlocBookTitle("book.NOVA.title");
        builder.setUnlocWelcomeMessage("book.NOVA.welcome");
        builder.setUnlocDisplayName("book.NOVA.displayname");
        builder.setBookColor(Math.random()>0.5?Color.CYAN:Color.MAGENTA); //cube wants 1DEAF6
        myBook = builder.build();

        GuideRegistry.registerBook(myBook);
    }
}
