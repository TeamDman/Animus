package com.teamdman_9201.nova;

import WayofTime.alchemicalWizardry.ModBlocks;
import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipeRegistry;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import WayofTime.alchemicalWizardry.api.bindingRegistry.BindingRegistry;
import WayofTime.alchemicalWizardry.api.items.ShapedBloodOrbRecipe;
import WayofTime.alchemicalWizardry.api.rituals.RitualEffect;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import amerifrance.guideapi.api.GuideRegistry;
import com.teamdman_9201.nova.blocks.BlockAntiBlock;
import com.teamdman_9201.nova.blocks.BlockBloodLeaves;
import com.teamdman_9201.nova.blocks.BlockBloodSapling;
import com.teamdman_9201.nova.blocks.BlockDirtChest;
import com.teamdman_9201.nova.generation.WorldGenerator;
import com.teamdman_9201.nova.handlers.NOVAEventHandler;
import com.teamdman_9201.nova.handlers.NOVAFuelHandler;
import com.teamdman_9201.nova.handlers.NOVAGuiHandler;
import com.teamdman_9201.nova.items.*;
import com.teamdman_9201.nova.items.sigils.ItemSigilOfChains;
import com.teamdman_9201.nova.items.sigils.ItemSigilOfConsumption;
import com.teamdman_9201.nova.items.sigils.ItemSigilOfTransposition;
import com.teamdman_9201.nova.rituals.RitualEffectEntropy;
import com.teamdman_9201.nova.rituals.RitualEffectLuna;
import com.teamdman_9201.nova.rituals.RitualEffectSol;
import com.teamdman_9201.nova.rituals.RitualEffectUncreation;
import com.teamdman_9201.nova.tiles.TileDirtChest;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This mod is made using the google code style xml provided from the google codestyles github repo.
 * Sometimes it looks ugly af tho...
 */


@Mod(modid = NOVA.MODID, name = NOVA.NAME, version = NOVA.VERSION, dependencies = NOVA.DEPENDS,
        guiFactory = "com.teamdman_9201.nova.gui.GuiFactory")
public class NOVA {

    public static final String MODID = "NOVA";
    public static final String NAME = "NOVA";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDS = "required-after:AWWayofTime;required-after:guideapi;";
    public static final int guiDirtChest = 0;
    public static boolean isDevEnv = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    @Instance(value = MODID)
    public static NOVA instance;
    public static Block blockDirtChest;
    public static Block blockLeaves;
    public static Block blockSapling;
    public static Block blockAntiBlock;
    public static Enchantment enchantPow;
    public static ItemBlock itemBlockSapling;
    public static ItemBlock itemBlockAntiBlock;
    public static Item itemSigilOfTransposition;
    public static Item itemSigilOfConsumption;
    public static Item itemUnstableCoal;
    public static Item itemBoundSickle;
    public static Item itemWoodSickle;
    public static Item itemStoneSickle;
    public static Item itemIronSickle;
    public static Item itemGoldSickle;
    public static Item itemDiamondSickle;
    public static Item itemMobSoul;
    public static Item itemSigilOfChains;
    public static Item itemBloodApple;
    public static Item itemRedundantOrb;
    public static Item itemDev;
    public static Item itemAltarDiviner;
    public static HashMap<String, Integer> ritualData = new HashMap<String, Integer>();
    public static ArrayList<Block> moveBlacklist;
    //Configurable Variables
    public static boolean doLowerChat;

    public static CreativeTabs mainTab = new CreativeTabs("NOVA") {
        @Override
        public Item getTabIconItem() {
            return itemBoundSickle;
        }
    };

    @EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerFuelHandler(new NOVAFuelHandler());
        GameRegistry.registerWorldGenerator(new WorldGenerator(), 1);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new NOVAGuiHandler());
        MinecraftForge.EVENT_BUS.register(new NOVAEventHandler());
        FMLCommonHandler.instance().bus().register(new NOVAEventHandler());

    }

    private void initItemsandBlocks() {
        setupBlock(blockDirtChest, "blockDirtChest", mainTab, 3.5F);
        setupBlock(blockLeaves, "blockLeaves", mainTab, 0.1F);
        setupItemBlock(blockAntiBlock, ItemBlockAntiBlock.class, "blockAntiBlock", mainTab, 2.0F);
        setupItemBlock(blockSapling, ItemBlockBloodSapling.class, "blockSapling", mainTab, 0);
        setupItem(itemSigilOfTransposition, "itemSigilOfTransposition", mainTab);
        setupItem(itemUnstableCoal, "itemUnstableCoal", mainTab);
        setupItem(itemWoodSickle, "itemWoodenSickle", mainTab);
        setupItem(itemStoneSickle, "itemStoneSickle", mainTab);
        setupItem(itemIronSickle, "itemIronSickle", mainTab);
        setupItem(itemGoldSickle, "itemGoldSickle", mainTab);
        setupItem(itemDiamondSickle, "itemDiamondSickle", mainTab);
        setupItem(itemBoundSickle, "itemBoundSickle", mainTab);
        setupItem(itemSigilOfChains, "itemSigilOfChains", mainTab);
        setupItem(itemMobSoul, "itemMobSoul", mainTab);
        setupItem(itemBloodApple, "itemBloodApple", mainTab);
        setupItem(itemRedundantOrb, "itemRedundantOrb", mainTab);
        setupItem(itemDev, "itemDev", mainTab);
        setupItem(itemSigilOfConsumption, "itemSigilOfConsumption", mainTab);
        setupItem(itemAltarDiviner,"itemAltarDiviner",mainTab);

    }

    private void initRecipes() {
        BindingRegistry.registerRecipe(new ItemStack(itemBoundSickle), new ItemStack(itemDiamondSickle));

        AltarRecipeRegistry.registerAltarRecipe(new ItemStack(blockAntiBlock), new ItemStack(Blocks.cobblestone), 3, 10000, 100, 100, false);
        AltarRecipeRegistry.registerAltarRecipe(new ItemStack(blockSapling), new ItemStack(Blocks.sapling), 1, 100, 1, 1, false);
        AltarRecipeRegistry.registerAltarRecipe(GuideRegistry.getItemStackForBook(NOVAGuide.myBook), new ItemStack(Items.book), 1, 100, 1, 1, false);

        AlchemyRecipeRegistry.registerRecipe(new ItemStack(itemUnstableCoal), 100, new ItemStack[]{new ItemStack(Items.nether_star), new ItemStack(Blocks.coal_block), new ItemStack(Items.gunpowder), new ItemStack(Items.flint_and_steel)}, 5);

        GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(itemSigilOfChains), "ABA", "DCD", "AEA", 'A', Blocks.iron_bars, 'B', Items.glass_bottle, 'C', ModItems.imbuedSlate, 'D', Items.ender_pearl, 'E', ModItems.magicianBloodOrb));
        GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(itemSigilOfTransposition), "ABA", "BCB", "ADA", 'A', Blocks.obsidian, 'B', Items.ender_pearl, 'C', ModItems.demonicSlate, 'D', ModItems.masterBloodOrb));
        GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(itemSigilOfConsumption),"ABA","BCB","ADA",'A',Blocks.end_stone,'B',Blocks.redstone_lamp,'C',ModItems.demonicSlate,'D',ModItems.masterBloodOrb));
        GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(itemAltarDiviner),"ABA","BCB","ADA",'A', ModBlocks.bloodRune,'B',Blocks.stone,'C',Items.stick,'D',ModItems.weakBloodOrb));
        GameRegistry.addSmelting(new ItemStack(itemRedundantOrb), new ItemStack(itemRedundantOrb), 1);
        GameRegistry.addRecipe(new ItemStack(itemRedundantOrb), "AAA", "ABA", "AAA", 'A', Items.diamond, 'B', Blocks.dirt);

        GameRegistry.addRecipe(new ItemStack(blockDirtChest), "AAA", "ABA", "AAA", 'A', Blocks.dirt, 'B', Blocks.planks);

        GameRegistry.addRecipe(new ItemStack(itemWoodSickle), "AAA", "A B", " B ", 'A', Blocks.planks, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemStoneSickle), "AAA", "A B", " B ", 'A', Blocks.cobblestone, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemIronSickle), "AAA", "A B", " B ", 'A', Items.iron_ingot, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemGoldSickle), "AAA", "A B", " B ", 'A', Items.gold_ingot, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemDiamondSickle), "AAA", "A B", " B ", 'A', Items.diamond, 'B', Items.stick);
    }

    private void initRituals() {
        setupRitual("Sol", new RitualEffectSol());
        setupRitual("Luna", new RitualEffectLuna());
        setupRitual("Uncreate", new RitualEffectUncreation());
        setupRitual("Entropy", new RitualEffectEntropy());
    }

    private void initTiles() {
        GameRegistry.registerTileEntity(TileDirtChest.class, "DirtChest");
    }

    private void initVars() {
        blockDirtChest = new BlockDirtChest();
        blockLeaves = new BlockBloodLeaves();
        blockSapling = new BlockBloodSapling();
        blockAntiBlock = new BlockAntiBlock();
        itemBoundSickle = new ItemBoundSickle();
        itemWoodSickle = new ItemBasicSickle(Item.ToolMaterial.WOOD);
        itemStoneSickle = new ItemBasicSickle(Item.ToolMaterial.STONE);
        itemIronSickle = new ItemBasicSickle(Item.ToolMaterial.IRON);
        itemGoldSickle = new ItemBasicSickle(Item.ToolMaterial.GOLD);
        itemDiamondSickle = new ItemBasicSickle(Item.ToolMaterial.EMERALD);
        itemSigilOfTransposition = new ItemSigilOfTransposition();
        itemUnstableCoal = new ItemUnstableCoal();
        itemBlockAntiBlock = new ItemBlockAntiBlock(blockAntiBlock);
        itemBlockSapling = new ItemBlockBloodSapling(blockSapling);
        itemSigilOfChains = new ItemSigilOfChains();
        itemMobSoul = new ItemMobSoul();
        itemBloodApple = new ItemBloodApple(2, 0.1F, false);
        itemRedundantOrb = new ItemRedundantOrb();
        itemDev = new ItemDev();
        itemSigilOfConsumption = new ItemSigilOfConsumption();
        itemAltarDiviner=new ItemAltarDiviner();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        initVars();
        initItemsandBlocks();
        initTiles();
        initRecipes();
        initRituals();
        NOVAGuide.buildGuide();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        NOVAConfig.init(new File(event.getModConfigurationDirectory(), NOVA.MODID + ".cfg"));
    }

    private void setupBlock(Block theBlock, String name, CreativeTabs tab, float hardness) {
        GameRegistry.registerBlock(theBlock, name);
        theBlock.setBlockName(name);
        theBlock.setCreativeTab(tab);
        theBlock.setHardness(hardness);
    }

    private void setupItem(Item theItem, String theName, CreativeTabs theTab) {
        GameRegistry.registerItem(theItem, theName);
        theItem.setUnlocalizedName(theName);
        theItem.setCreativeTab(theTab);
        theItem.setTextureName(MODID + ":" + theName);
    }

    private void setupItemBlock(Block theBlock, Class theClass, String theName, CreativeTabs theTab, float theHardness) {
        GameRegistry.registerBlock(theBlock, theClass, theName);
        theBlock.setBlockName(theName);
        theBlock.setCreativeTab(theTab);
        theBlock.setHardness(theHardness);
    }

    private void setupRitual(String name, RitualEffect effect) {
        if (ritualData.get("ritual" + name) == 0)
            Rituals.registerRitual("ritual" + name, ritualData.get("level" + name), ritualData.get("init" + name), effect, StatCollector.translateToLocal("ritual.NOVA." + name.toLowerCase()));
    }
}

