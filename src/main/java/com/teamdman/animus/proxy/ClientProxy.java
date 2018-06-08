package com.teamdman.animus.proxy;

import WayofTime.bloodmagic.client.IMeshProvider;
import WayofTime.bloodmagic.client.IVariantProvider;
import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

//import WayofTime.bloodmagic.util.helper.InventoryRenderHelperV2; no longer exists

/**
 * Created by TeamDman on 9/18/2016.
 */
@Mod.EventBusSubscriber(modid=Constants.Mod.MODID)
public class ClientProxy extends CommonProxy {
	@Override
	public void tryHandleItemModel(Item item) {
		if (item instanceof IVariantProvider) {
			Int2ObjectMap<String> variants = new Int2ObjectOpenHashMap<>();
			((IVariantProvider) item).gatherVariants(variants);
			//noinspection ConstantConditions, items _must_ have a registry name
			variants.forEach((i, v) -> ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), v)));
		} else if (item instanceof IMeshProvider) {
			IMeshProvider mesh = (IMeshProvider) item;
			final ResourceLocation location = mesh.getCustomLocation() != null ? mesh.getCustomLocation() : item.getRegistryName();
			Set<String> variants = new HashSet<>();
			mesh.gatherVariants(variants::add);
			//noinspection ConstantConditions
			variants.forEach(v -> ModelLoader.registerItemVariants(item, new ModelResourceLocation(location, v)));
			ModelLoader.setCustomMeshDefinition(item, mesh.getMeshDefinition());

		}
	}

	@Override
	public void tryHandleBlockModel(Block block) {
		if (block instanceof IVariantProvider) {
			Int2ObjectMap<String> variants = new Int2ObjectOpenHashMap<>();
			((IVariantProvider) block).gatherVariants(variants);
			//noinspection ConstantConditions
			variants.forEach((i, v) -> ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), i, new ModelResourceLocation(block.getRegistryName(), v)));
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		AnimusItems.items.forEach(Animus.proxy::tryHandleItemModel);
		AnimusBlocks.blocks.forEach(Animus.proxy::tryHandleBlockModel);
	}
	
	@Override
	public boolean fancyGraphics() {
		return Minecraft.getMinecraft().gameSettings.fancyGraphics;
	}
	
}
