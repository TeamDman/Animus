package com.TeamDman_9201.nova;

import com.TeamDman_9201.nova.Blocks.BlockBrickFurnace;
import com.TeamDman_9201.nova.Blocks.BlockCoalDiamondOre;
import com.TeamDman_9201.nova.Blocks.BlockCompressedTorch;
import com.TeamDman_9201.nova.Blocks.BlockLightManipulator;
import com.TeamDman_9201.nova.Items.ItemBlockCompressedTorch;
import com.TeamDman_9201.nova.Items.ItemSuperCoal;
import com.TeamDman_9201.nova.Items.ItemUnstableCoal;
import com.TeamDman_9201.nova.Recipes.RecipeCompressedTorch;
import com.TeamDman_9201.nova.Tiles.TileBrickFurnace;
import com.TeamDman_9201.nova.Tiles.TileCompressedTorch;
import com.TeamDman_9201.nova.Tiles.TileLightManipulator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
//import fml.common.NetworkMod
//@NetworkMod(clientSideRequired = true, serverSideRequired = false)

@Mod(modid = NOVA.MODID, name = NOVA.NAME, version = NOVA.VERSION)
public class NOVA {
  public static final String MODID = "nova";
  public static final String NAME = "NOVA";
  public static final String VERSION = "1.0";
  // GUIs
  public static final int guiBrickFurnace = 0;
  public static final int guiLightManipulator = 1;
  @Instance(value = MODID)
  public static NOVA instance;
  // BLOCKS
  public static Block coalDiamondOre;
  public static Block brickFurnace;
  public static Block lightManipulator;
  public static Block compressedTorch;
  // ITEMS
  public static Item superCoal;
  public static CreativeTabs mainTab = new CreativeTabs("NOVA") {
    @Override
    public Item getTabIconItem() {
      return superCoal;
    }
  };
  public static Item unstableCoal;

  // ITEMBLOCKS
  public static ItemBlock itemBlockCompressedTorch;

  @EventHandler
  public void init(FMLInitializationEvent event) {
    GameRegistry.registerFuelHandler(new NOVAFuelHandler());
    NetworkRegistry.INSTANCE.registerGuiHandler(this, new NOVAGuiHandler());
    // MinecraftForge.EVENT_BUS.register(new NOVAEventListener());
    FMLCommonHandler.instance().bus().register(new NOVAEventListener());
    // FML.EVENT();

  }

  @EventHandler
  public void load(FMLInitializationEvent event) {
    GameRegistry.registerTileEntity(TileBrickFurnace.class, "BrickFurnace");
    GameRegistry.registerTileEntity(TileLightManipulator.class, "LightManipulator");
    GameRegistry.registerTileEntity(TileCompressedTorch.class, "CompressedTorch");
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {

  }

  @EventHandler
  public void preinit(FMLPreInitializationEvent event) {
    // player.capabilities.allowFlying = true;
    compressedTorch = new BlockCompressedTorch();
    itemBlockCompressedTorch = new ItemBlockCompressedTorch(compressedTorch);
    setupItemBlock(compressedTorch, ItemBlockCompressedTorch.class, "compressedTorch", mainTab, 0,
                   Material.wood);
    GameRegistry.addRecipe(new RecipeCompressedTorch());

    lightManipulator = new BlockLightManipulator(false);
    setupBlock(lightManipulator, "lightManipulator", mainTab, 3.5F, Material.glass);
    GameRegistry
        .addRecipe(new ItemStack(lightManipulator, 1), "ACA", "CBC", "ACA", 'A', Blocks.torch, 'B',
                   Items.ender_pearl, 'C', Blocks.glowstone);

    brickFurnace = new BlockBrickFurnace();
    setupBlock(brickFurnace, "brickFurnace", mainTab, 3.5F, Material.rock);
    GameRegistry
        .addRecipe(new ItemStack(brickFurnace, 1), "AAA", "A A", "AAA", 'A', Blocks.brick_block);

    coalDiamondOre = new BlockCoalDiamondOre();
    setupBlock(coalDiamondOre, "coalDiamondOre", mainTab, 6, Material.rock);

    unstableCoal = new ItemUnstableCoal();
    setupItem(unstableCoal, "unstableCoal", mainTab);
    GameRegistry.addSmelting(new ItemStack(superCoal), new ItemStack(unstableCoal), 2048);

    superCoal = new ItemSuperCoal();
    setupItem(superCoal, "superCoal", mainTab);
    GameRegistry.addRecipe(new ItemStack(superCoal, 2), "AAA", "ABA", "AAA", 'A', Items.coal, 'B',
                           new ItemStack(superCoal));

    GameRegistry
        .addRecipe(new ItemStack(Items.glowstone_dust, 1), "ABA", "BCB", "ABA", 'A', Items.redstone,
                   'B',
                   Blocks.torch, 'C', Items.gold_ingot);
    // OreDictionary.registerOre(NAME, coalDiamondOre);
  }

  private void setupBlock(Block theBlock, String theName, CreativeTabs theTab, float theHardness,
                          Material theMaterial) {
    GameRegistry.registerBlock(theBlock, theName);
    theBlock.setBlockName(theName);
    theBlock.setCreativeTab(theTab);
    theBlock.setHardness(theHardness);
  }

  private void setupItem(Item theItem, String theName, CreativeTabs theTab) {
    GameRegistry.registerItem(theItem, theName);
    theItem.setUnlocalizedName(theName);
    theItem.setCreativeTab(theTab);
    theItem.setTextureName(MODID + ":" + theName);
  }

  private void setupItemBlock(Block theBlock, Class theClass, String theName, CreativeTabs theTab,
                              float theHardness,
                              Material theMaterial) {
    GameRegistry.registerBlock(theBlock, theClass, theName);
    theBlock.setBlockName(theName);
    theBlock.setCreativeTab(theTab);
    theBlock.setHardness(theHardness);
  }
}
