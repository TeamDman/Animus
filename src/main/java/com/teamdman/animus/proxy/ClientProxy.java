package com.teamdman.animus.proxy;

import com.teamdman.animus.client.render.entity.RenderVengefulSpirit;
import com.teamdman.animus.entity.EntityVengefulSpirit;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

//import WayofTime.bloodmagic.util.helper.InventoryRenderHelperV2; no longer exists

/**
 * Created by TeamDman on 9/18/2016.
 */
public class ClientProxy extends CommonProxy {
	//InventoryRenderHelperV2 renderHelper;


	/*public InventoryRenderHelperV2 getRenderHelper() {
		return renderHelper;
	}*/

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);


		//renderHelper = new InventoryRenderHelperV2(Animus.DOMAIN);
		AnimusItems.initRenders();
		initRenderers();
	}

	private void initRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityVengefulSpirit.class, RenderVengefulSpirit::new);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);

	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}

	//
	//	@Override
	//	public void tryHandleItemModel(Item item, String name) {
	//
	//		if (item instanceof IMeshProvider) {
	//			System.out.println("IMeshProvider");
	//			IMeshProvider meshProvider = (IMeshProvider) item;
	//			ModelLoader.setCustomMeshDefinition(item, meshProvider.getMeshDefinition());
	//			ResourceLocation resourceLocation = meshProvider.getCustomLocation();
	//			if (resourceLocation == null)
	//				resourceLocation = new ResourceLocation(Constants.Mod.MODID, "item/" + name);
	//
	//			for (String variant : meshProvider.getVariants())
	//				ModelLoader.registerItemVariants(item, new ModelResourceLocation(resourceLocation, variant));
	//		} else if (item instanceof IVariantProvider) {
	//			IVariantProvider variantProvider = (IVariantProvider) item;
	//			for (Pair<Integer, String> variant : variantProvider.getVariants()) {
	//				ModelLoader.setCustomModelResourceLocation(item, variant.getLeft(), new ModelResourceLocation(new ResourceLocation(Constants.Mod.MODID, "item/" + name), variant.getRight()));
	//
	//
	//			}
	//		}
	//	}
	//
	//
	//	@Override
	//	public void tryHandleBlockModel(Block block, String name) {
	//		if (block instanceof IVariantProvider) {
	//			IVariantProvider variantProvider = (IVariantProvider) block;
	//			for (Pair<Integer, String> variant : variantProvider.getVariants())
	//				ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), variant.getLeft(), new ModelResourceLocation(new ResourceLocation(Constants.Mod.MODID, name), variant.getRight()));
	//		}
	//	}


}
