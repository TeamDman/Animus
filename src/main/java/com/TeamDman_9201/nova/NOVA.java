package com.teamdman_9201.nova;

import com.teamdman_9201.nova.blocks.BlockBrickFurnace;
import com.teamdman_9201.nova.blocks.BlockCoalDiamondOre;
import com.teamdman_9201.nova.blocks.BlockCobblizer;
import com.teamdman_9201.nova.blocks.BlockCompressedTorch;
import com.teamdman_9201.nova.blocks.BlockDirtChest;
import com.teamdman_9201.nova.blocks.BlockLeaves;
import com.teamdman_9201.nova.blocks.BlockSapling;
import com.teamdman_9201.nova.generation.WorldGenerator;
import com.teamdman_9201.nova.handlers.NOVAEventHandler;
import com.teamdman_9201.nova.handlers.NOVAFuelHandler;
import com.teamdman_9201.nova.handlers.NOVAGuiHandler;
import com.teamdman_9201.nova.items.ItemBlockCompressedTorch;
import com.teamdman_9201.nova.items.ItemBlockSapling;
import com.teamdman_9201.nova.items.ItemBoundScythe;
import com.teamdman_9201.nova.items.ItemSlotIdentifier;
import com.teamdman_9201.nova.items.ItemSuperCoal;
import com.teamdman_9201.nova.items.ItemTransportalizer;
import com.teamdman_9201.nova.items.ItemUnstableCoal;
import com.teamdman_9201.nova.recipes.RecipeCompressedTorch;
import com.teamdman_9201.nova.rituals.RitualEffectDev;
import com.teamdman_9201.nova.rituals.RitualEffectLuna;
import com.teamdman_9201.nova.rituals.RitualEffectSol;
import com.teamdman_9201.nova.rituals.RitualEffectUncreation;
import com.teamdman_9201.nova.tiles.TileBrickFurnace;
import com.teamdman_9201.nova.tiles.TileCobblizer;
import com.teamdman_9201.nova.tiles.TileCompressedTorch;
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
import WayofTime.alchemicalWizardry.api.bindingRegistry.BindingRegistry;
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

@Mod(modid = NOVA.MODID, name = NOVA.NAME, version = NOVA.VERSION, dependencies = NOVA.DEPENDS,
        guiFactory = "com.teamdman_9201.nova.gui.GuiFactory")
public class NOVA {

    public static final String  MODID           = "NOVA";
    public static final String  NAME            = "NOVA";
    public static final String  VERSION         = "@VERSION@";
    public static final String  DEPENDS         = "required-after:AWWayofTime;";
    public static final int     guiBrickFurnace = 0;
    public static final int     guiCobblizer    = 1;
    public static final int     guiDirtChest    = 2;
    public static       boolean devEnviroment   = (Boolean) Launch.blackboard.get("fml" + "" +
            ".deobfuscatedEnvironment");
    @Instance(value = MODID)
    public static NOVA        instance;
    public static Block       blockBrickFurnace;
    public static Block       blockCoalDiamondOre;
    public static Block       blockCobblizer;
    public static Block       blockCompressedTorch;
    public static Block       blockDirtChest;
    public static Block       blockLeaves;
    public static Block       blockLightManipulator;
    public static Block       blockSapling;
    public static Enchantment enchantPow;
    public static ItemBlock   itemBlockCompressedTorch;
    public static ItemBlock   itemBlockSapling;
    public static Item        itemBoundScythe;
    public static Item        itemSlotIdentifier;
    public static Item        itemSuperCoal;
    public static Item        itemTransportalizer;
    public static Item        itemUnstableCoal;
    public static HashMap<String,Boolean> disabledRituals = new HashMap<String,Boolean>();
    public static HashMap<String,Integer> ritualCosts = new HashMap<String,Integer>();
    //Configurable Variables
    public static boolean doLowerChat;

    public static CreativeTabs mainTab = new CreativeTabs("NOVA") {
        @Override
        public Item getTabIconItem() {
            return itemSuperCoal;
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

        setupBlock(blockBrickFurnace, "blockBrickFurnace", mainTab, 3.5F);
        setupBlock(blockCoalDiamondOre, "blockCoalDiamondOre", mainTab, 6);
        setupBlock(blockCobblizer, "blockCobblizer", mainTab, 3.5F);
        setupBlock(blockDirtChest, "blockDirtChest", mainTab, 3.5F);
        setupBlock(blockLeaves, "blockLeaves", mainTab, 0.1F);
        setupItem(itemBoundScythe, "itemBoundScythe", mainTab);
        setupItem(itemTransportalizer, "itemTransportalizer", mainTab);
        setupItem(itemSlotIdentifier, "itemSlotIdentifier", mainTab);
        setupItem(itemSuperCoal, "itemSuperCoal", mainTab);
        setupItem(itemUnstableCoal, "itemUnstableCoal", mainTab);
        setupItemBlock(blockCompressedTorch, ItemBlockCompressedTorch.class,
                "blockCompressedTorch", mainTab, 0);
        setupItemBlock(blockSapling, ItemBlockSapling.class, "blockSapling", mainTab, 0);

    }

    private void initRecipes() {

        GameRegistry.addRecipe(new ItemStack(blockCobblizer, 1), "A A", "A A", "AAA", 'A', Blocks
                .cobblestone);
        GameRegistry.addRecipe(new RecipeCompressedTorch());
        GameRegistry.addRecipe(new ItemStack(blockLightManipulator, 1), "ACA", "CBC", "ACA", 'A',
                Blocks.torch, 'B', Items.ender_pearl, 'C', Blocks.glowstone);
        GameRegistry.addRecipe(new ItemStack(blockBrickFurnace, 1), "AAA", "A A", "AAA", 'A',
                Blocks.brick_block);
        GameRegistry.addRecipe(new ItemStack(itemSuperCoal, 2), "AAA", "ABA", "AAA", 'A', Items
                .coal, 'B', new ItemStack(itemSuperCoal));
        GameRegistry.addRecipe(new ItemStack(Items.glowstone_dust, 1), "ABA", "BCB", "ABA", 'A',
                Items.redstone, 'B', Blocks.torch, 'C', Items.gold_ingot);
        GameRegistry.addSmelting(new ItemStack(itemSuperCoal), new ItemStack(itemUnstableCoal),
                2048);
        // OreDictionary.registerOre(NAME, blockCoalDiamondOre);
    }

    private void initRituals() {
        if (!disabledRituals.get("ritualSol"))
        Rituals.registerRitual("ritualSol", 1, ritualCosts.get("initSol"), new RitualEffectSol(),
                StatCollector
                .translateToLocal("ritual.NOVA.sol"));
        if (!disabledRituals.get("ritualLuna"))
        Rituals.registerRitual("ritualLuna", 2, ritualCosts.get("initLuna"), new RitualEffectLuna
                        (),
                StatCollector
                .translateToLocal("ritual.NOVA.luna"));
        if (!disabledRituals.get("ritualUncreate"))
        Rituals.registerRitual("ritualUncreate", 2, ritualCosts.get("initUncreate"), new
                        RitualEffectUncreation(),
                StatCollector.translateToLocal("ritual.NOVA.uncreate"));
        if (devEnviroment)
            Rituals.registerRitual("ritualDev", 1, 1, new RitualEffectDev(), StatCollector
                    .translateToLocal("ritual.NOVA.dev"));


        BindingRegistry.registerRecipe(new ItemStack(itemBoundScythe), new ItemStack(ModItems
                .sacrificialDagger));
    }

    private void initTiles() {
        GameRegistry.registerTileEntity(TileBrickFurnace.class, "BrickFurnace");
        GameRegistry.registerTileEntity(TileCobblizer.class, "Cobblizer");
        GameRegistry.registerTileEntity(TileCompressedTorch.class, "CompressedTorch");
        GameRegistry.registerTileEntity(TileDirtChest.class, "DirtChest");
    }

    private void initVars() {
        blockBrickFurnace = new BlockBrickFurnace();
        blockCoalDiamondOre = new BlockCoalDiamondOre();
        blockCobblizer = new BlockCobblizer();
        blockCompressedTorch = new BlockCompressedTorch();
        blockDirtChest = new BlockDirtChest();
        blockLeaves = new BlockLeaves();
        blockSapling = new BlockSapling();
        itemBoundScythe = new ItemBoundScythe();
        itemTransportalizer = new ItemTransportalizer();
        itemSlotIdentifier = new ItemSlotIdentifier();
        itemSuperCoal = new ItemSuperCoal();
        itemUnstableCoal = new ItemUnstableCoal();
        itemBlockCompressedTorch = new ItemBlockCompressedTorch(blockCompressedTorch);
        itemBlockSapling = new ItemBlockSapling(blockSapling);
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        initVars();
        initItemsandBlocks();
        initTiles();
        initRecipes();
        initRituals();
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

    private void setupItemBlock(Block theBlock, Class theClass, String theName, CreativeTabs
            theTab, float theHardness) {
        GameRegistry.registerBlock(theBlock, theClass, theName);
        theBlock.setBlockName(theName);
        theBlock.setCreativeTab(theTab);
        theBlock.setHardness(theHardness);
    }
}

//player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed)
// .applyModifier(...)