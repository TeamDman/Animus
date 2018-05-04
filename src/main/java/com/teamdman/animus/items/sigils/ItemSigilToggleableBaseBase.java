package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.iface.IActivatable;
import WayofTime.bloodmagic.item.sigil.ItemSigilToggleableBase;
import com.teamdman.animus.Constants;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

public class ItemSigilToggleableBaseBase extends ItemSigilToggleableBase {
	final String name;
	public ItemSigilToggleableBaseBase(String name, int lpUsed) {
		super(name, lpUsed);
		this.name = name;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public ItemMeshDefinition getMeshDefinition() {
		return new CustomCustomMeshDefinitionActivatable("sigil_" + name.toLowerCase(Locale.ROOT));
	}

	public class CustomCustomMeshDefinitionActivatable implements ItemMeshDefinition {
		private final String name;

		public CustomCustomMeshDefinitionActivatable(String name) {
			this.name = name;
		}

		@Override
		public ModelResourceLocation getModelLocation(ItemStack stack) {
			if (!stack.isEmpty() && stack.getItem() instanceof IActivatable)
				if (((IActivatable) stack.getItem()).getActivated(stack))
					return new ModelResourceLocation(new ResourceLocation(Constants.Mod.MODID, name), "active=true");

			return new ModelResourceLocation(new ResourceLocation(Constants.Mod.MODID, name), "active=false");
		}
	}
}
