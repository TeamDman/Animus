package com.TeamDman.nova;

import com.TeamDman.nova.Blocks.BlockBrickFurnace;
import com.TeamDman.nova.Blocks.BlockCoalDiamondOre;
import com.TeamDman.nova.Blocks.BlockCobblizer;
import com.TeamDman.nova.Blocks.BlockCompressedTorch;
import com.TeamDman.nova.Blocks.BlockDirtChest;
import com.TeamDman.nova.Blocks.BlockLeaves;
import com.TeamDman.nova.Blocks.BlockLightManipulator;
import com.TeamDman.nova.Blocks.BlockSapling;
import com.TeamDman.nova.Items.ItemBlockCompressedTorch;
import com.TeamDman.nova.Items.ItemBlockSapling;
import com.TeamDman.nova.Items.ItemSlotIdentifier;
import com.TeamDman.nova.Items.ItemSuperCoal;
import com.TeamDman.nova.Items.ItemTransportalizer;
import com.TeamDman.nova.Items.ItemUnstableCoal;
import com.TeamDman.nova.Recipes.RecipeCompressedTorch;
import com.TeamDman.nova.Tiles.TileBrickFurnace;
import com.TeamDman.nova.Tiles.TileCobblizer;
import com.TeamDman.nova.Tiles.TileCompressedTorch;
import com.TeamDman.nova.Tiles.TileDirtChest;
import com.TeamDman.nova.Tiles.TileLightManipulator;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = NOVA.MODID, name = NOVA.NAME, version = NOVA.VERSION, dependencies = NOVA.DEPENDS, guiFactory = "com.TeamDman.nova.NOVAGuiFactory")
public class NOVA {

  public static final String MODID               = "NOVA";
  public static final String NAME                = "NOVA";
  public static final String VERSION             = "%VERSION%";
  public static final String DEPENDS = "required-after:AWWayofTime;" + "required-after:guideapi;" + "after:Waila";
  public static final int    guiBrickFurnace     = 0;
  public static final int    guiCobblizer        = 2;
  public static final int    guiLightManipulator = 1;
  public static final int    guiDirtChest        = 3;
  public static Block         blockBrickFurnace;
  public static Block         blockCoalDiamondOre;
  public static Block         blockCobblizer;
  public static Block         blockCompressedTorch;
  public static Block         blockDirtChest;
  public static Block         blockLeaves;
  public static Block         blockLightManipulator;
  public static Block         blockSapling;
  public static Enchantment   enchantPow;
  @Instance(value = MODID)
  public static NOVA          instance;
  public static ItemBlock     itemBlockCompressedTorch;
  public static ItemBlock     itemBlockSapling;
  public static Item          itemSlotIdentifier;
  public static Item          itemSuperCoal;
  public static Item          itemTransportalizer;
  public static Item          itemUnstableCoal;
  public static boolean       doLowerChat;
  public static Configuration config;
  public static CreativeTabs mainTab = new CreativeTabs("NOVA") {
    @Override
    public Item getTabIconItem() {
      return itemSuperCoal;
    }
  };

  @EventHandler
  public void init(FMLInitializationEvent event) {
    GameRegistry.registerFuelHandler(new com.TeamDman.nova.NOVAFuelHandler());
    GameRegistry.registerWorldGenerator(new com.TeamDman.nova.NOVAWorldGenerator(), 1);
    NetworkRegistry.INSTANCE.registerGuiHandler(this, new com.TeamDman.nova.NOVAGuiHandler());
    MinecraftForge.EVENT_BUS.register(new com.TeamDman.nova.NOVAEventListener());
    FMLCommonHandler.instance().bus().register(new com.TeamDman.nova.NOVAEventListener());

  }


  @EventHandler
  public void load(FMLInitializationEvent event) {
    GameRegistry.registerTileEntity(TileBrickFurnace.class, "BrickFurnace");
    GameRegistry.registerTileEntity(TileCobblizer.class, "Cobblizer");
    GameRegistry.registerTileEntity(TileCompressedTorch.class, "CompressedTorch");
    GameRegistry.registerTileEntity(TileDirtChest.class, "DirtChest");
    GameRegistry.registerTileEntity(TileLightManipulator.class, "LightManipulator");
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {

  }

  public static void syncConfig() {
    config.load();
    enchantPow = new com.TeamDman.nova.EnchantmentPow(config.getInt("Pow", "Enchantments", 164, 0, 255, ""), 3004);
    doLowerChat =
        config.getBoolean("doLowerChat", "General", false,
                          "Converts incoming chat messages to lowercase.");
    if (config.hasChanged()) {
      config.save();
    }

  }

  @EventHandler
  public void preinit(FMLPreInitializationEvent event) {
    config = new Configuration(event.getSuggestedConfigurationFile());
    syncConfig();
    blockBrickFurnace = new BlockBrickFurnace();
    blockCoalDiamondOre = new BlockCoalDiamondOre();
    blockCobblizer = new BlockCobblizer();
    blockCompressedTorch = new BlockCompressedTorch();
    blockDirtChest = new BlockDirtChest();
    blockLeaves = new BlockLeaves();
    blockLightManipulator = new BlockLightManipulator();
    blockSapling = new BlockSapling();
    itemTransportalizer = new ItemTransportalizer();
    itemSlotIdentifier = new ItemSlotIdentifier();
    itemSuperCoal = new ItemSuperCoal();
    itemUnstableCoal = new ItemUnstableCoal();
    itemBlockCompressedTorch = new ItemBlockCompressedTorch(blockCompressedTorch);
    itemBlockSapling = new ItemBlockSapling(blockSapling);

    setupBlock(blockBrickFurnace, "blockBrickFurnace", mainTab, 3.5F);
    setupBlock(blockCoalDiamondOre, "blockCoalDiamondOre", mainTab, 6);
    setupBlock(blockCobblizer, "blockCobblizer", mainTab, 3.5F);
    setupBlock(blockDirtChest, "blockDirtChest", mainTab, 3.5F);
    setupBlock(blockLeaves, "blockLeaves", mainTab, 0.1F);
    setupBlock(blockLightManipulator, "blockLightManipulator", mainTab, 3.5F);
    setupItem(itemTransportalizer, "itemTransportalizer", mainTab);
    setupItem(itemSlotIdentifier, "itemSlotIdentifier", mainTab);
    setupItem(itemSuperCoal, "itemSuperCoal", mainTab);
    setupItem(itemUnstableCoal, "itemUnstableCoal", mainTab);
    setupItemBlock(blockCompressedTorch, ItemBlockCompressedTorch.class, "blockCompressedTorch",
                   mainTab, 0);
    setupItemBlock(blockSapling, ItemBlockSapling.class, "blockSapling", mainTab, 0);

    GameRegistry
        .addRecipe(new ItemStack(blockCobblizer, 1), "A A", "A A", "AAA", 'A', Blocks.cobblestone);
    GameRegistry.addRecipe(new RecipeCompressedTorch());
    GameRegistry
        .addRecipe(new ItemStack(blockLightManipulator, 1), "ACA", "CBC", "ACA", 'A', Blocks.torch,
                   'B', Items.ender_pearl, 'C', Blocks.glowstone);
    GameRegistry.addRecipe(new ItemStack(blockBrickFurnace, 1), "AAA", "A A", "AAA", 'A',
                           Blocks.brick_block);
    GameRegistry
        .addRecipe(new ItemStack(itemSuperCoal, 2), "AAA", "ABA", "AAA", 'A', Items.coal, 'B',
                   new ItemStack(itemSuperCoal));
    GameRegistry
        .addRecipe(new ItemStack(Items.glowstone_dust, 1), "ABA", "BCB", "ABA", 'A', Items.redstone,
                   'B', Blocks.torch, 'C', Items.gold_ingot);
    GameRegistry.addSmelting(new ItemStack(itemSuperCoal), new ItemStack(itemUnstableCoal), 2048);
    // OreDictionary.registerOre(NAME, blockCoalDiamondOre);
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

  private void setupItemBlock(Block theBlock, Class theClass, String theName, CreativeTabs theTab,
                              float theHardness) {
    GameRegistry.registerBlock(theBlock, theClass, theName);
    theBlock.setBlockName(theName);
    theBlock.setCreativeTab(theTab);
    theBlock.setHardness(theHardness);
  }
}

//player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed).applyModifier(...)