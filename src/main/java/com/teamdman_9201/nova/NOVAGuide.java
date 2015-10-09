package com.teamdman_9201.nova;

import WayofTime.alchemicalWizardry.ModBlocks;
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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TeamDman on 2015-07-30.
 */
public class NOVAGuide {
    public static Book myBook;

    public static void addPage(String title, List<EntryAbstract> category, Object... extra) {
        ArrayList<IPage> pages = new ArrayList<IPage>();
        for (Object obj : extra) {
            if (obj instanceof IPage) {
                pages.add((IPage) obj);
            } else if (obj instanceof String) {
                pages.add(new PageUnlocText("book.NOVA." + (String) obj));
            }
        }
        category.add(new EntryText(pages, title));
    }

    public static void buildGuide() {
        List<EntryAbstract> rituals = new ArrayList<EntryAbstract>();
        List<EntryAbstract> items   = new ArrayList<EntryAbstract>();
        List<EntryAbstract> blocks  = new ArrayList<EntryAbstract>();
        List<EntryAbstract> sigils  = new ArrayList<EntryAbstract>();

        addPage("Ritual of Sol", rituals, "rituals.Sol.body");
        addPage("Ritual of Luna", rituals, "rituals.Luna.body");
        addPage("Ritual of Uncreation", rituals, "rituals.Uncreation.body");
        addPage("Ritual of Entropy", rituals, "rituals.Entropy.body", "rituals.Entropy.body2");

        addPage("Sickles", items, "items.Sickles.body", new PageIRecipe(new ShapedOreRecipe(NOVA.itemWoodSickle, "AAA", "A B", " B ", 'A', Blocks.planks, 'B', Items.stick)), new PageIRecipe(new ShapedOreRecipe(NOVA.itemStoneSickle, "AAA", "A B", " B ", 'A', Blocks.cobblestone, 'B', Items.stick)), new PageIRecipe(new ShapedOreRecipe(NOVA.itemIronSickle, "AAA", "A B", " B ", 'A', Items.iron_ingot, 'B', Items.stick)), new PageIRecipe(new ShapedOreRecipe(NOVA.itemGoldSickle, "AAA", "A B", " B ", 'A', Items.gold_ingot, 'B', Items.stick)), new PageIRecipe(new ShapedOreRecipe(NOVA.itemDiamondSickle, "AAA", "A B", " B ", 'A', Items.diamond, 'B', Items.stick)));
        addPage("Unstable Coal", items, "items.UnstableCoal.body");
        addPage("Blood Trees", items, "items.Sapling.body");
        addPage("Orb of Redundancy", items, "items.RedundantOrb.body", new PageIRecipe(new ShapedOreRecipe(NOVA.itemRedundantOrb, "AAA", "ABA", "AAA", 'A', Items.diamond, 'B', Blocks.dirt)));
        addPage("Dev Item", items, "items.Dev.body");
        addPage("Healing Fragments", items, "items.HealingFragment.body");

        addPage("Sigil of Chains", sigils, "sigils.SigilOfChains.body", new PageIRecipe(new ShapedOreRecipe(NOVA.itemSigilOfChains, "ABA", "DCD", "AEA", 'A', Blocks.iron_bars, 'B', Items.glass_bottle, 'C', ModItems.imbuedSlate, 'D', Items.ender_pearl, 'E', ModItems.magicianBloodOrb)));
        addPage("Sigil of Transposition", sigils, "sigils.SigilOfTransposition.body", new PageIRecipe( new ShapedOreRecipe(NOVA.itemSigilOfTransposition,"ABA", "BCB", "ADA", 'A', Blocks.obsidian, 'B', Items.ender_pearl, 'C', ModItems.demonicSlate, 'D', ModItems.masterBloodOrb)));
        addPage("Sigil of Consumption", sigils, "sigils.SigilOfConsumption.body", new PageIRecipe(new ShapedOreRecipe(NOVA.itemSigilOfConsumption,"ABA","BCB","ADA",'A',Blocks.end_stone,'B',Blocks.redstone_lamp,'C',ModItems.demonicSlate,'D',ModItems.masterBloodOrb)));
        addPage("Sigil of the Fast Builder", sigils, "sigils.SigilOfFastBuilder.body");

        addPage("Dirt Chest", blocks, "blocks.DirtChest.body", new PageIRecipe(new ShapedOreRecipe(NOVA.blockDirtChest,"AAA","ABA","AAA",'A', Blocks.dirt,'B',Blocks.planks)));

        ArrayList<CategoryAbstract> categories = new ArrayList<CategoryAbstract>();
        categories.add(new CategoryItemStack(rituals, "Rituals", new ItemStack(ModItems.activationCrystal)));
        categories.add(new CategoryItemStack(items, "Items", new ItemStack(NOVA.itemBoundSickle)));
        categories.add(new CategoryItemStack(sigils, "Sigils", new ItemStack(NOVA.itemSigilOfChains)));
        categories.add(new CategoryItemStack(blocks, "Blocks", new ItemStack(ModBlocks.blockMasterStone)));

        BookBuilder builder = new BookBuilder();
        builder.setCategories(categories);
        builder.setUnlocBookTitle("book.NOVA.title");
        builder.setUnlocWelcomeMessage("book.NOVA.welcome");
        builder.setUnlocDisplayName("book.NOVA.displayname");
        builder.setBookColor(Math.random() > 0.5 ? Color.CYAN : Color.MAGENTA); //cube wants 1DEAF6
        myBook = builder.build();

        GuideRegistry.registerBook(myBook);
    }
}
