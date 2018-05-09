package com.teamdman.animus.recipes;

import WayofTime.bloodmagic.core.data.Binding;
import WayofTime.bloodmagic.iface.IBindable;
import com.google.gson.JsonObject;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class KeyBindingRecipeFactory implements IRecipeFactory {
	@Override
	public IRecipe parse(JsonContext context, JsonObject json) {
		ShapedOreRecipe             recipe = ShapedOreRecipe.factory(context, json);
		CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
		primer.width = recipe.getRecipeWidth();
		primer.height = recipe.getRecipeHeight();
		primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
		primer.input = recipe.getIngredients();

		return new KeyBindingRecipe(new ResourceLocation(Constants.Mod.MODID, "keybindingcrafting"), recipe.getRecipeOutput(), primer);
	}

	public static class KeyBindingRecipe extends ShapedOreRecipe {
		public KeyBindingRecipe(ResourceLocation group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer) {
			super(group, result, primer);
		}

		@Nonnull
		@Override
		public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.getItem() == AnimusItems.KEYBINDING) {
					if (((IBindable) stack.getItem()).getBinding(stack) == null) {
						for (int v = 0; v < inv.getSizeInventory(); v++) {
							ItemStack _stack = inv.getStackInSlot(v);
							if (_stack.getItem() instanceof IBindable) {
								Binding succ = ((IBindable) _stack.getItem()).getBinding(_stack);
								if (succ != null) {
									if (!stack.hasTagCompound()) {
										stack.setTagCompound(new NBTTagCompound());
									}
									//noinspection ConstantConditions
									stack.getTagCompound().setTag("binding", succ.serializeNBT());
								}
							}
						}
					}
					return stack.copy();
				}
			}
			return ItemStack.EMPTY;
		}

		@Override
		public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
			byte rtn = 0;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.getItem() == AnimusItems.KEYBINDING)
					rtn |= 1;
				else if (stack.getItem() instanceof IBindable)
					rtn |= 2;
				else if (!stack.isEmpty())
					rtn = 0;
			}
			return rtn == (1 | 2);
			//Make sure that there is only the key and bindable items.
		}

		@SuppressWarnings("NullableProblems")
		@Override
		public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
			NonNullList<ItemStack> rtn        = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
			Binding                keyBinding = null;
			ItemStack              key        = ItemStack.EMPTY;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.getItem() == AnimusItems.KEYBINDING) {
					keyBinding = ((IBindable) stack.getItem()).getBinding(stack);
					key = stack.copy();
					break;
				}
			}
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.getItem() instanceof IBindable && stack.getItem() != AnimusItems.KEYBINDING) {
					ItemStack n = stack.copy();
					if (keyBinding != null) {
						if (!stack.hasTagCompound())
							stack.setTagCompound(new NBTTagCompound());
						//noinspection ConstantConditions
						n.getTagCompound().setTag("binding", keyBinding.serializeNBT());
					}
					rtn.set(i, n);
				}
			}
			return rtn;
		}
	}
}
