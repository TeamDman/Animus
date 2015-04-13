package com.TeamDman_9201.nova;
//TODO ADD TOUCH LAUNCH ARMOR ENCHANT
//TODO ADD TNT WORLD GENERATioN SO LIGTH MANIPULATOR BLOWS SHIT UP

import com.TeamDman_9201.nova.Blocks.BlockBrickFurnace;
import com.TeamDman_9201.nova.Blocks.BlockCoalDiamondOre;
import com.TeamDman_9201.nova.Blocks.BlockCompressedTorch;
import com.TeamDman_9201.nova.Blocks.BlockLightManipulator;
import com.TeamDman_9201.nova.Blocks.BlockRecycleBin;
import com.TeamDman_9201.nova.Items.ItemBlockCompressedTorch;
import com.TeamDman_9201.nova.Items.ItemSlotIdentifier;
import com.TeamDman_9201.nova.Items.ItemSuperCoal;
import com.TeamDman_9201.nova.Items.ItemUnstableCoal;
import com.TeamDman_9201.nova.Recipes.RecipeCompressedTorch;
import com.TeamDman_9201.nova.Tiles.TileBrickFurnace;
import com.TeamDman_9201.nova.Tiles.TileCompressedTorch;
import com.TeamDman_9201.nova.Tiles.TileLightManipulator;
import com.TeamDman_9201.nova.Tiles.TileCobblizer;

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

  public static final String MODID = "nova";
  public static final String NAME = "NOVA";
  public static final String VERSION = "1.0";
  public static final int guiBrickFurnace = 0;
  public static final int guiLightManipulator = 1;
  public static final int guiRecycleBin = 2;
  @Instance(value = MODID)
  public static NOVA instance;
  public static Block blockCoalDiamondOre;
  public static Block blockBrickFurnace;
  public static Block blockLightManipulator;
  public static Block blockCompressedTorch;
  public static Block blockRecycleBin;
  public static Enchantment enchantPow;
  public static Item superCoal;
  public static Item unstableCoal;
  public static Item slotIdentifier;
  public static CreativeTabs mainTab = new CreativeTabs("NOVA") {
    @Override
    public Item getTabIconItem() {
      return superCoal;
    }
  };

  public static ItemBlock itemBlockCompressedTorch;

  @EventHandler
  public void preinit(FMLPreInitializationEvent event) {
    Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    config.load();
    enchantPow = new EnchantmentPow(getEnchantID(config, "Pow", 132), 3004);
    config.save();

    blockRecycleBin = new BlockRecycleBin();
    setupBlock(blockRecycleBin,"blockRecycleBin", mainTab, 3.5F, Material.iron);
    GameRegistry.addRecipe(new ItemStack(blockRecycleBin,1), "A A","A A","AAA",'A',Blocks.cobblestone);

    blockCompressedTorch = new BlockCompressedTorch();
    itemBlockCompressedTorch = new ItemBlockCompressedTorch(blockCompressedTorch);
    setupItemBlock(blockCompressedTorch, ItemBlockCompressedTorch.class, "blockCompressedTorch",
                   mainTab, 0, Material.wood);
    GameRegistry.addRecipe(new RecipeCompressedTorch());

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

    unstableCoal = new ItemUnstableCoal();
    setupItem(unstableCoal, "unstableCoal", mainTab);
    GameRegistry.addSmelting(new ItemStack(superCoal), new ItemStack(unstableCoal), 2048);

    superCoal = new ItemSuperCoal();
    setupItem(superCoal, "superCoal", mainTab);
    GameRegistry.addRecipe(new ItemStack(superCoal, 2), "AAA", "ABA", "AAA", 'A', Items.coal, 'B',
                           new ItemStack(superCoal));

    slotIdentifier = new ItemSlotIdentifier();
    setupItem(slotIdentifier,"slotIdentifier",mainTab);

    GameRegistry
        .addRecipe(new ItemStack(Items.glowstone_dust, 1), "ABA", "BCB", "ABA", 'A', Items.redstone,
                   'B', Blocks.torch, 'C', Items.gold_ingot);
    // OreDictionary.registerOre(NAME, blockCoalDiamondOre);
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    GameRegistry.registerFuelHandler(new NOVAFuelHandler());
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
    GameRegistry.registerTileEntity(TileCobblizer.class, "RecycleBin");
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
