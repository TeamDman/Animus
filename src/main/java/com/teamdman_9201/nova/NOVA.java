package com.teamdman_9201.nova;

import com.teamdman_9201.nova.blocks.BlockAntiBlock;
import com.teamdman_9201.nova.blocks.BlockDirtChest;
import com.teamdman_9201.nova.blocks.BlockLeaves;
import com.teamdman_9201.nova.blocks.BlockSapling;
import com.teamdman_9201.nova.generation.WorldGenerator;
import com.teamdman_9201.nova.handlers.NOVAEventHandler;
import com.teamdman_9201.nova.handlers.NOVAFuelHandler;
import com.teamdman_9201.nova.handlers.NOVAGuiHandler;
import com.teamdman_9201.nova.items.ItemBasicSickle;
import com.teamdman_9201.nova.items.ItemBlockAntiBlock;
import com.teamdman_9201.nova.items.ItemBlockSapling;
import com.teamdman_9201.nova.items.ItemBoundSickle;
import com.teamdman_9201.nova.items.ItemMobSoul;
import com.teamdman_9201.nova.items.ItemSlotIdentifier;
import com.teamdman_9201.nova.items.ItemTransportalizer;
import com.teamdman_9201.nova.items.ItemUnstableCoal;
import com.teamdman_9201.nova.items.sigils.ItemSigilOfChains;
import com.teamdman_9201.nova.recipes.RecipeBlockAntiBlock;
import com.teamdman_9201.nova.rituals.RitualEffectDev;
import com.teamdman_9201.nova.rituals.RitualEffectEntropy;
import com.teamdman_9201.nova.rituals.RitualEffectLuna;
import com.teamdman_9201.nova.rituals.RitualEffectSol;
import com.teamdman_9201.nova.rituals.RitualEffectUncreation;
import com.teamdman_9201.nova.tiles.TileDirtChest;

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
import java.util.HashMap;

import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import WayofTime.alchemicalWizardry.api.bindingRegistry.BindingRegistry;
import WayofTime.alchemicalWizardry.api.rituals.RitualEffect;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * This mod is made using the google code style xml provided from the google codestyles github repo.
 * Sometimes it looks ugly af tho...
 */


@Mod(modid = NOVA.MODID, name = NOVA.NAME, version = NOVA.VERSION, dependencies = NOVA.DEPENDS,
        guiFactory = "com.teamdman_9201.nova.gui.GuiFactory")
public class NOVA {

    public static final String  MODID        = "NOVA";
    public static final String  NAME         = "NOVA";
    public static final String  VERSION      = "@VERSION@";
    public static final String  DEPENDS      = "required-after:AWWayofTime;";
    public static final int     guiDirtChest = 0;
    public static       boolean isDevEnv     = (Boolean) Launch.blackboard.get("fml" + ".deobfuscatedEnvironment");
    @Instance(value = MODID)
    public static NOVA        instance;
    public static Block       blockDirtChest;
    public static Block       blockLeaves;
    public static Block       blockLightManipulator;
    public static Block       blockSapling;
    public static Block       blockAntiBlock;
    public static Enchantment enchantPow;
    public static ItemBlock   itemBlockSapling;
    public static Item        itemSlotIdentifier;
    public static Item        itemTransportalizer;
    public static Item        itemUnstableCoal;
    public static Item        itemBoundSickle;
    public static Item        itemWoodSickle;
    public static Item        itemStoneSickle;
    public static Item        itemIronSickle;
    public static Item        itemGoldSickle;
    public static Item        itemDiamondSickle;
    public static Item        itemMobSoul;
    public static Item        itemSigilOfChains;
    public static HashMap<String, Integer> ritualData = new HashMap<String, Integer>();
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
        setupItemBlock(blockAntiBlock, ItemBlockAntiBlock.class,  "blockAntiBlock", mainTab, 2.0F);
        setupItemBlock(blockSapling, ItemBlockSapling.class, "blockSapling", mainTab, 0);
        setupItem(itemTransportalizer, "itemTransportalizer", mainTab);
        setupItem(itemSlotIdentifier, "itemSlotIdentifier", mainTab);
        setupItem(itemUnstableCoal, "itemUnstableCoal", mainTab);
        setupItem(itemWoodSickle, "itemWoodenSickle", mainTab);
        setupItem(itemStoneSickle, "itemStoneSickle", mainTab);
        setupItem(itemIronSickle, "itemIronSickle", mainTab);
        setupItem(itemGoldSickle, "itemGoldSickle", mainTab);
        setupItem(itemDiamondSickle, "itemDiamondSickle", mainTab);
        setupItem(itemBoundSickle, "itemBoundSickle", mainTab);
        setupItem(itemSigilOfChains, "itemSigilOfChains", mainTab);
        setupItem(itemMobSoul, "itemMobSoul", mainTab);

    }

    private void initRecipes() {
        BindingRegistry.registerRecipe(new ItemStack(itemBoundSickle), new ItemStack(itemDiamondSickle));
        AltarRecipeRegistry.registerAltarRecipe(new ItemStack(blockAntiBlock),new ItemStack(Blocks.cobblestone),4,10000,100,100,false);
        GameRegistry.addRecipe(new RecipeBlockAntiBlock());
        GameRegistry.addRecipe(new ItemStack(blockLightManipulator, 1), "ACA", "CBC", "ACA", 'A', Blocks.torch, 'B', Items.ender_pearl, 'C', Blocks.glowstone);
        GameRegistry.addRecipe(new ItemStack(Items.glowstone_dust, 1), "ABA", "BCB", "ABA", 'A', Items.redstone, 'B', Blocks.torch, 'C', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(itemWoodSickle), "AAA", "A B", " B ", 'A', Blocks.planks, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemStoneSickle), "AAA", "A B", " B ", 'A', Blocks.cobblestone, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemIronSickle), "AAA", "A B", " B ", 'A', Items.iron_ingot, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemGoldSickle), "AAA", "A B", " B ", 'A', Items.gold_ingot, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemDiamondSickle), "AAA", "A B", " B ", 'A', Items.diamond, 'B', Items.stick);
        GameRegistry.addRecipe(new ItemStack(itemSigilOfChains), "ABA", "DCD", "ABA", 'A', Blocks.iron_bars, 'B', Items.glass_bottle, 'C', ModItems.magicianBloodOrb, 'D', Items.ender_pearl);
    }

    private void initRituals() {
        setupRitual("Sol", new RitualEffectSol());
        setupRitual("Luna", new RitualEffectLuna());
        setupRitual("Uncreate", new RitualEffectUncreation());
        setupRitual("Entropy", new RitualEffectEntropy());
        if (isDevEnv)
            Rituals.registerRitual("ritualDev", 1, 1, new RitualEffectDev(), StatCollector.translateToLocal("ritual.NOVA.dev"));
    }

    private void initTiles() {
        GameRegistry.registerTileEntity(TileDirtChest.class, "DirtChest");
    }

    private void initVars() {
        blockDirtChest = new BlockDirtChest();
        blockLeaves = new BlockLeaves();
        blockSapling = new BlockSapling();
        blockAntiBlock=new BlockAntiBlock();
        itemBoundSickle = new ItemBoundSickle();
        itemWoodSickle = new ItemBasicSickle(Item.ToolMaterial.WOOD);
        itemStoneSickle = new ItemBasicSickle(Item.ToolMaterial.STONE);
        itemIronSickle = new ItemBasicSickle(Item.ToolMaterial.IRON);
        itemGoldSickle = new ItemBasicSickle(Item.ToolMaterial.GOLD);
        itemDiamondSickle = new ItemBasicSickle(Item.ToolMaterial.EMERALD);
        itemTransportalizer = new ItemTransportalizer();
        itemSlotIdentifier = new ItemSlotIdentifier();
        itemUnstableCoal = new ItemUnstableCoal();
        itemBlockSapling = new ItemBlockSapling(blockSapling);
        itemSigilOfChains = new ItemSigilOfChains();
        itemMobSoul = new ItemMobSoul();
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

