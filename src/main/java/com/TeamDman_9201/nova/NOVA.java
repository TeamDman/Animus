package com.TeamDman_9201.nova;

import com.TeamDman_9201.nova.Blocks.BrickFurnace;
import com.TeamDman_9201.nova.Blocks.CoalDiamondOre;
import com.TeamDman_9201.nova.Blocks.LightManipulator;
import com.TeamDman_9201.nova.Items.CompressedTorch;
import com.TeamDman_9201.nova.Items.SuperCoal;
import com.TeamDman_9201.nova.Items.UnstableCoal;
import com.TeamDman_9201.nova.Tiles.TileEntityBrickFurnace;
import com.TeamDman_9201.nova.Tiles.TileEntityLightManipulator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
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

	@Instance(value = MODID)
	public static NOVA instance;

	public static CreativeTabs mainTab = new CreativeTabs("NOVA") {
		public Item getTabIconItem() {
			return superCoal;
		}
	};

	// BLOCKS
	public static Block coalDiamondOre;

	public static Block brickFurnace;
	public static Block lightManipulator;
	public static Block compressedTorch;

	// GUIs
	public static final int guiBrickFurnace = 0;
	public static final int guiLightManipulator = 1;

	// ITEMS
	public static Item superCoal;
	public static Item unstableCoal;

	private void setupItem(Item theItem, String theName, CreativeTabs theTab) {
		GameRegistry.registerItem(theItem, theName);
		theItem.setUnlocalizedName(theName);
		theItem.setCreativeTab(theTab);
		theItem.setTextureName(MODID + ":" + theName);
	}

	private void setupBlock(Block theBlock, String theName,
			CreativeTabs theTab, float theHardness, Material theMaterial) {
		GameRegistry.registerBlock(theBlock, theName);
		theBlock.setBlockName(theName);
		theBlock.setCreativeTab(theTab);
		theBlock.setHardness(theHardness);
		// theBlock.setMaterial(theMaterial);
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		// player.capabilities.allowFlying = true;
		compressedTorch = new CompressedTorch();
		setupBlock(compressedTorch, "compressedTorch", mainTab, 0,
				Material.wood);

		lightManipulator = new LightManipulator(false);
		setupBlock(lightManipulator, "lightManipulator", mainTab, 3.5F,
				Material.glass);
		GameRegistry.addRecipe(new ItemStack(lightManipulator, 1),
				new Object[] { "ACA", "CBC", "ACA", 'A', Blocks.torch, 'B',
						Items.ender_pearl, 'C', Blocks.glowstone });

		GameRegistry.addRecipe(new ItemStack(Items.glowstone_dust, 1),
				new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
						Blocks.torch, 'C', Items.gold_ingot });

		brickFurnace = new BrickFurnace();
		setupBlock(brickFurnace, "brickFurnace", mainTab, 3.5F, Material.rock);
		GameRegistry.addRecipe(new ItemStack(brickFurnace, 1), new Object[] {
				"AAA", "A A", "AAA", 'A', Blocks.brick_block });

		coalDiamondOre = new CoalDiamondOre();
		setupBlock(coalDiamondOre, "coalDiamondOre", mainTab, 6, Material.rock);
		// coalDiamondOre.setBlockTextureName(NOVA.MODID + ":" +
		// "coalDiamondOre");

		unstableCoal = new UnstableCoal();
		setupItem(unstableCoal, "unstableCoal", mainTab);
		GameRegistry.addSmelting(new ItemStack(superCoal), new ItemStack(
				unstableCoal), 2048);

		superCoal = new SuperCoal();
		setupItem(superCoal, "superCoal", mainTab);
		GameRegistry.addRecipe(new ItemStack(superCoal, 2), new Object[] {
				"AAA", "ABA", "AAA", 'A', Items.coal, 'B',
				new ItemStack(superCoal) });

		// OreDictionary.registerOre(NAME, coalDiamondOre);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerFuelHandler(new NOVAFuelHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new NOVAGuiHandler());
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		GameRegistry.registerTileEntity(TileEntityBrickFurnace.class,
				"BrickFurnace");
		GameRegistry.registerTileEntity(TileEntityLightManipulator.class,
				"LightManipulator");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}
}
