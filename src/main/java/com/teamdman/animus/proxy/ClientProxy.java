package com.teamdman.animus.proxy;

import WayofTime.bloodmagic.client.IMeshProvider;
import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.util.helper.InventoryRenderHelperV2;
import com.teamdman.animus.Animus;
import com.teamdman.animus.client.render.entity.RenderVengefulSpirit;
import com.teamdman.animus.client.resources.EffectHandler;
import com.teamdman.animus.entity.EntityVengefulSpirit;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by TeamDman on 9/18/2016.
 */
public class ClientProxy extends CommonProxy {
	InventoryRenderHelperV2 renderHelper;

	@Override
	public InventoryRenderHelperV2 getRenderHelper() {
		return renderHelper;
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		renderHelper = new InventoryRenderHelperV2(Animus.DOMAIN);
		AnimusItems.initRenders();
		initRenderers();
	}

	private void initRenderers() {
		
		RenderingRegistry.registerEntityRenderingHandler(EntityVengefulSpirit.class, RenderVengefulSpirit::new);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		MinecraftForge.EVENT_BUS.register(EffectHandler.getInstance());
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}


	@Override
	public void tryHandleItemModel(Item item, String name) {

		if (item instanceof IMeshProvider) {
			System.out.println("IMeshProvider");
			IMeshProvider meshProvider = (IMeshProvider) item;
			ModelLoader.setCustomMeshDefinition(item, meshProvider.getMeshDefinition());
			ResourceLocation resourceLocation = meshProvider.getCustomLocation();
			if (resourceLocation == null)
				resourceLocation = new ResourceLocation(Animus.MODID, "item/" + name);

			for (String variant : meshProvider.getVariants())
				ModelLoader.registerItemVariants(item, new ModelResourceLocation(resourceLocation, variant));
		} else if (item instanceof IVariantProvider) {
			//        	System.out.println("IVariantProvider");
			IVariantProvider variantProvider = (IVariantProvider) item;
			for (Pair<Integer, String> variant : variantProvider.getVariants()) {
				ModelLoader.setCustomModelResourceLocation(item, variant.getLeft(), new ModelResourceLocation(new ResourceLocation(Animus.MODID, "item/" + name), variant.getRight()));
				//            	  System.out.println("Resource: " + Animus.MODID + "item/" + name);
				//            	  System.out.println("Variant left: " + variant.getLeft());
				//            	  System.out.println("Variant right: " + variant.getRight());

			}
		}
	}


	@Override
	public void tryHandleBlockModel(Block block, String name) {
		if (block instanceof IVariantProvider) {
			IVariantProvider variantProvider = (IVariantProvider) block;
			for (Pair<Integer, String> variant : variantProvider.getVariants())
				ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), variant.getLeft(), new ModelResourceLocation(new ResourceLocation(Animus.MODID, name), variant.getRight()));
		}
	}


}
