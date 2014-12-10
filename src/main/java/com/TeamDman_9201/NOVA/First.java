package com.TeamDman_9201.NOVA;

import net.minecraft.block.Block;
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
@Mod(modid = First.MODID, name = First.NAME, version = First.VERSION)
public class First {
	public static final String MODID = "first";
	public static final String NAME = "First";
	public static final String VERSION = "1.0";

	@Instance(value=MODID)
	public static First instance;

	public static CreativeTabs firstTab = new CreativeTabs("First Tab") {
		public Item getTabIconItem() {
			return superCoal;
		}
	};


	// BLOCKS
	public static Block coalDiamondOre;

	public static Block brickFurnace;
	public static Block lightManipulator;
	
	// GUIs
	public static final int guiBrickFurnace= 0;
	public static final int guiLightManipulator= 1;

	// ITEMS
	public static Item superCoal;
	public static Item unstableCoal;

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		// player.capabilities.allowFlying = true;
		lightManipulator = new LightManipulator(false);
//		lightManipulator.setBlockName("lightManipulator");
		GameRegistry.registerBlock(lightManipulator, "lightManipulator");
		GameRegistry.addRecipe(new ItemStack(lightManipulator,1), new Object[] {
			"ACA","CBC","ACA", 'A', Blocks.torch, 'B', Items.ender_pearl, 'C', Blocks.glowstone
		});
		// Maybe add a separate block torch placer and add it to the recipie then make light manipulator be able to remove light.

		GameRegistry.addRecipe(new ItemStack(Items.glowstone_dust,1), new Object[] {
			"ABA","BCB","ABA", 'A', Items.redstone, 'B', Blocks.torch, 'C', Items.gold_ingot
		});
		
		brickFurnace = new BrickFurnace();
		brickFurnace.setBlockName("brickFurnace");
		GameRegistry.registerBlock(brickFurnace, "brickFurnace");
		GameRegistry.addRecipe(new ItemStack(brickFurnace,1), new Object[] {
			"AAA", "A A", "AAA", 'A', Blocks.brick_block
		});

		coalDiamondOre = new CoalDiamondOre();
		GameRegistry.registerBlock(coalDiamondOre, coalDiamondOre.getUnlocalizedName().substring(5));

		unstableCoal = new UnstableCoal();
		GameRegistry.registerItem(unstableCoal, unstableCoal.getUnlocalizedName().substring(5));
		GameRegistry.addSmelting(new ItemStack(superCoal), new ItemStack(unstableCoal), 2048);

		superCoal = new SuperCoal();
		GameRegistry.registerItem(superCoal, superCoal.getUnlocalizedName().substring(5));
		GameRegistry.addRecipe(new ItemStack(superCoal, 2), new Object[] {
			"AAA", "ABA", "AAA", 'A', Items.coal, 'B',
			new ItemStack(superCoal) 
		});
		
//		OreDictionary.registerOre(NAME, coalDiamondOre);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) { 
		GameRegistry.registerFuelHandler(new FirstFuelHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new FirstGuiHandler());
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		GameRegistry.registerTileEntity(TileEntityBrickFurnace.class, "BrickFurnace");
		GameRegistry.registerTileEntity(TileEntityLightManipulator.class, "LightManipulator");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}
}
