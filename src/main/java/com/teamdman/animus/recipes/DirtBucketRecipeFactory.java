package com.teamdman.animus.recipes;

import com.google.gson.JsonObject;
import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.BlockFluidDirt;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class DirtBucketRecipeFactory implements IRecipeFactory {
	@Override
	public IRecipe parse(JsonContext context, JsonObject json) {
		ShapedOreRecipe             recipe = ShapedOreRecipe.factory(context, json);
		CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
		primer.width = recipe.getRecipeWidth();
		primer.height = recipe.getRecipeHeight();
		primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
		primer.input = recipe.getIngredients();

		return new DirtBucketRecipe(new ResourceLocation(Constants.Mod.MODID, Constants.Misc.CRAFTING_DIRTBUCKET), recipe.getRecipeOutput(), primer);
	}

	public static class DirtBucketRecipe extends ShapedOreRecipe {
		public DirtBucketRecipe(ResourceLocation group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer) {
			super(group, result, primer);
		}

		@Nonnull
		@Override
		public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
			return FluidUtil.getFilledBucket(new FluidStack(BlockFluidDirt.FLUID_INSTANCE, Fluid.BUCKET_VOLUME));
		}
	}
}
