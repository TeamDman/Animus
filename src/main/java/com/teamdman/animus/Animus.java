package com.teamdman.animus;
/**
 * Created by TeamDman on 9/9/2016.
 */

import com.teamdman.animus.client.gui.config.GuiHandler;
import com.teamdman.animus.proxy.CommonProxy;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusRituals;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.io.File;
import java.util.Locale;

// TODO: Sigil that eats for you

@Mod(modid = Animus.MODID, name = Animus.NAME, version = Animus.VERSION, dependencies = Animus.DEPENDENCIES, guiFactory = "com.teamdman.client.gui.config.ConfigGuiFactory")
public class Animus {
    public static final String MODID = "animus";
    public static final String DOMAIN = MODID.toLowerCase(Locale.ENGLISH) + ":";
    public static final String NAME = "Animus";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDENCIES = "required-after:BloodMagic;required-after:guideapi;after:Waila";

    @SidedProxy(clientSide = "com.teamdman.animus.proxy.ClientProxy", serverSide = "com.teamdman.animus.proxy.ServerProxy")
    public static CommonProxy proxy;

    public static CreativeTabs tabMain = new CreativeTabs("Animus") {
        @Override
        public Item getTabIconItem() {
            return Items.BAKED_POTATO;
        }
    };

    // init blocks and items
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        AnimusConfig.init(new File(event.getModConfigurationDirectory(), Animus.MODID + ".cfg"));
        AnimusItems.init();
        AnimusBlocks.init();
        proxy.preInit(event);
    }

    // mod setup, register recipes
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        AnimusRituals.initRituals();
        proxy.init(event);
    }

    // mod interaction
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }


}
