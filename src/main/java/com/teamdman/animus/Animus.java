package com.teamdman.animus;

import com.teamdman.animus.client.gui.GuiHandler;
import com.teamdman.animus.handlers.AnimusSounds;
import com.teamdman.animus.handlers.EventHandler;
import com.teamdman.animus.proxy.CommonProxy;
import com.teamdman.animus.registry.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.io.File;

@Mod(modid = Constants.Mod.MODID, name = Constants.Mod.NAME, version = Constants.Mod.VERSION, dependencies = Constants.Mod.DEPEND, guiFactory = "com.teamdman.animus.client.gui.ConfigGuiFactory")
public class Animus {


	@Mod.Instance(Constants.Mod.MODID)
	public static Animus instance;
	@SidedProxy(clientSide = "com.teamdman.animus.proxy.ClientProxy", serverSide = "com.teamdman.animus.proxy.ServerProxy")
	public static CommonProxy proxy;
	//	public static CreativeTabs tabMain = BloodMagic.TAB_BM;
	public static CreativeTabs tabMain = new CreativeTabs(Constants.Mod.MODID) {
		@Override
		public ItemStack getTabIconItem() {
			return AnimusItems.altarDiviner.getDefaultInstance();
		}
	};

	// init blocks and items
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		AnimusConfig.init(new File(event.getModConfigurationDirectory(), Constants.Mod.MODID + ".cfg"));
		AnimusPotions.init();
		AnimusTiles.init();
		AnimusEntities.init();
		proxy.preInit(event);
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}

	// mod setup, register recipes
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		AnimusRituals.init();
		AnimusSounds.init();
		AnimusRecipes.init();
		proxy.init(event);
	}

	// mod interaction
	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}


}
