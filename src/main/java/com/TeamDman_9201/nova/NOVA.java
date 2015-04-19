package com.TeamDman_9201.nova;
//TODO ADD TOUCH LAUNCH ARMOR ENCHANT
//TODO ADD TNT WORLD GENERATioN SO LIGTH MANIPULATOR BLOWS SHIT UP

import com.TeamDman_9201.nova.Blocks.BlockBrickFurnace;
import com.TeamDman_9201.nova.Blocks.BlockCoalDiamondOre;
import com.TeamDman_9201.nova.Blocks.BlockCobblizer;
import com.TeamDman_9201.nova.Blocks.BlockCompressedTorch;
import com.TeamDman_9201.nova.Blocks.BlockLightManipulator;
import com.TeamDman_9201.nova.Blocks.BlockSapling;
import com.TeamDman_9201.nova.Items.ItemBlockCompressedTorch;
import com.TeamDman_9201.nova.Items.ItemBlockSapling;
import com.TeamDman_9201.nova.Items.ItemSlotIdentifier;
import com.TeamDman_9201.nova.Items.ItemSuperCoal;
import com.TeamDman_9201.nova.Items.ItemUnstableCoal;
import com.TeamDman_9201.nova.Recipes.RecipeCompressedTorch;
import com.TeamDman_9201.nova.Tiles.TileBrickFurnace;
import com.TeamDman_9201.nova.Tiles.TileCobblizer;
import com.TeamDman_9201.nova.Tiles.TileCompressedTorch;
import com.TeamDman_9201.nova.Tiles.TileLightManipulator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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

@Mod(modid = NOVA.MODID, name = NOVA.NAME, version = NOVA.VERSION)
public class NOVA {

  public static final String MODID = "NOVA";
  public static final String NAME = "NOVA";
  public static final String VERSION = "1.0";
  public static final int guiBrickFurnace = 0;
  public static final int guiLightManipulator = 1;
  public static final int guiCobblizer = 2;
  @Instance(value = MODID)
  public static NOVA instance;
  public static Block blockCoalDiamondOre;
  public static Block blockBrickFurnace;
  public static Block blockLightManipulator;
  public static Block blockCompressedTorch;
  public static Block blockCobblizer;
  public static Block blockSapling;
  public static Block blockLeaves;
  public static ItemBlock itemBlockCompressedTorch;
  public static ItemBlock itemBlockSapling;
  public static Enchantment enchantPow;
  public static Item itemSuperCoal;
  public static Item itemUnstableCoal;
  public static Item itemSlotIdentifier;

  public static CreativeTabs mainTab = new CreativeTabs("NOVA") {
    @Override
    public Item getTabIconItem() {
      return itemSuperCoal;
    }
  };

  @EventHandler
  public void preinit(FMLPreInitializationEvent event) {
    Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    config.load();
    enchantPow = new EnchantmentPow(getEnchantID(config, "Pow", 132), 3004);
    config.save();

    blockCobblizer = new BlockCobblizer();
    setupBlock(blockCobblizer, "blockCobblizer", mainTab, 3.5F, Material.iron);
    GameRegistry
        .addRecipe(new ItemStack(blockCobblizer, 1), "A A", "A A", "AAA", 'A', Blocks.cobblestone);

    blockCompressedTorch = new BlockCompressedTorch();
    itemBlockCompressedTorch = new ItemBlockCompressedTorch(blockCompressedTorch);
    setupItemBlock(blockCompressedTorch, ItemBlockCompressedTorch.class, "blockCompressedTorch",
                   mainTab, 0, Material.wood);
    GameRegistry.addRecipe(new RecipeCompressedTorch());

    blockSapling = new BlockSapling();
    itemBlockSapling = new ItemBlockSapling(blockSapling);
    setupItemBlock(blockSapling,ItemBlockSapling.class,"blockSapling",mainTab,0,Material.leaves);

    blockLightManipulator = new BlockLightManipulator(false);
    setupBlock(blockLightManipulator, "blockLightManipulator", mainTab, 3.5F, Material.glass);
    GameRegistry
        .addRecipe(new ItemStack(blockLightManipulator, 1), "ACA", "CBC", "ACA", 'A', Blocks.torch,
                   'B', Items.ender_pearl, 'C', Blocks.glowstone);

    blockBrickFurnace = new BlockBrickFurnace();
    setupBlock(blockBrickFurnace, "blockBrickFurnace", mainTab, 3.5F, Material.rock);
    GameRegistry.addRecipe(new ItemStack(blockBrickFurnace, 1), "AAA", "A A", "AAA", 'A',
                           Blocks.brick_block);

    blockCoalDiamondOre = new BlockCoalDiamondOre();
    setupBlock(blockCoalDiamondOre, "blockCoalDiamondOre", mainTab, 6, Material.rock);

    blockLeaves = new com.TeamDman_9201.nova.Blocks.BlockLeaves();
    setupBlock(blockLeaves,"blockLeaves",mainTab,0.1F,Material.leaves);
    itemSuperCoal = new ItemSuperCoal();
    setupItem(itemSuperCoal, "itemSuperCoal", mainTab);
    GameRegistry
        .addRecipe(new ItemStack(itemSuperCoal, 2), "AAA", "ABA", "AAA", 'A', Items.coal, 'B',
                   new ItemStack(itemSuperCoal));

    itemUnstableCoal = new ItemUnstableCoal();
    setupItem(itemUnstableCoal, "itemUnstableCoal", mainTab);
    GameRegistry.addSmelting(new ItemStack(itemSuperCoal), new ItemStack(itemUnstableCoal), 2048);

    itemSlotIdentifier = new ItemSlotIdentifier();
    setupItem(itemSlotIdentifier, "itemSlotIdentifier", mainTab);

    GameRegistry
        .addRecipe(new ItemStack(Items.glowstone_dust, 1), "ABA", "BCB", "ABA", 'A', Items.redstone,
                   'B', Blocks.torch, 'C', Items.gold_ingot);
    // OreDictionary.registerOre(NAME, blockCoalDiamondOre);
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    GameRegistry.registerFuelHandler(new NOVAFuelHandler());
    GameRegistry.registerWorldGenerator(new NOVAWorldGenerator(), 1);
    NetworkRegistry.INSTANCE.registerGuiHandler(this, new NOVAGuiHandler());
    // MinecraftForge.EVENT_BUS.register(new NOVAEventListener());
    FMLCommonHandler.instance().bus().register(new NOVAEventListener());

  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {

  }

  @EventHandler
  public void load(FMLInitializationEvent event) {
    GameRegistry.registerTileEntity(TileBrickFurnace.class, "BrickFurnace");
    GameRegistry.registerTileEntity(TileLightManipulator.class, "LightManipulator");
    GameRegistry.registerTileEntity(TileCompressedTorch.class, "CompressedTorch");
    GameRegistry.registerTileEntity(TileCobblizer.class, "Cobblizer");
  }

  public int getEnchantID(Configuration config, String name, int id) {
    return config.getInt(name, "Enchantments", id, 0, 255, "");
  }

  private void setupBlock(Block theBlock, String name, CreativeTabs tab, float hardness,
                          Material mat) {
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
                              float theHardness, Material theMaterial) {
    GameRegistry.registerBlock(theBlock, theClass, theName);
    theBlock.setBlockName(theName);
    theBlock.setCreativeTab(theTab);
    theBlock.setHardness(theHardness);
  }
}

//player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed).applyModifier(...)