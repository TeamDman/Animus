package com.teamdman.animus;

import WayofTime.bloodmagic.api.BloodMagicPlugin;
import WayofTime.bloodmagic.api.IBloodMagicAPI;
import WayofTime.bloodmagic.api.IBloodMagicPlugin;
import WayofTime.bloodmagic.api.IBloodMagicRecipeRegistrar;
import WayofTime.bloodmagic.api.impl.BloodMagicAPI;
import WayofTime.bloodmagic.api.impl.BloodMagicRecipeRegistrar;
import com.teamdman.animus.registry.AnimusRecipes;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@BloodMagicPlugin
@ZenRegister
@ZenClass("mods.animus.AnimusPlugin")
public class AnimusPlugin implements IBloodMagicPlugin {

	private static List<Consumer<BloodMagicAPI>> actions = new ArrayList<>();

	@ZenMethod
	public static void addComponentMapping(IBlockState state, String component) {
		actions.add((api -> api.registerAltarComponent((net.minecraft.block.state.IBlockState) state.getInternal(), component)));
	}

	@ZenMethod
	public static void removeComponentMapping(IBlockState state, String component) {
		actions.add((api -> api.unregisterAltarComponent((net.minecraft.block.state.IBlockState) state.getInternal(), component)));
	}

	@Override
	public void register(IBloodMagicAPI apiInterface) {
		BloodMagicAPI api = (BloodMagicAPI) apiInterface;
		actions.forEach(a -> a.accept(api));
	}

	@Override
	public void registerRecipes(IBloodMagicRecipeRegistrar recipeRegistrar) {
		AnimusRecipes.registerAltarRecipes((BloodMagicRecipeRegistrar) recipeRegistrar);
		AnimusRecipes.registerAlchemyTableRecipes((BloodMagicRecipeRegistrar) recipeRegistrar);
		AnimusRecipes.registerTartaricForgeRecipes((BloodMagicRecipeRegistrar) recipeRegistrar);
		AnimusRecipes.registerAlchemyArrayRecipes((BloodMagicRecipeRegistrar) recipeRegistrar);

	}
}