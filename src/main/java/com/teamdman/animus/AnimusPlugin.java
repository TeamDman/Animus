package com.teamdman.animus;

import WayofTime.bloodmagic.api.BloodMagicPlugin;
import WayofTime.bloodmagic.api.IBloodMagicAPI;
import WayofTime.bloodmagic.api.IBloodMagicPlugin;
import WayofTime.bloodmagic.api.IBloodMagicRecipeRegistrar;
import WayofTime.bloodmagic.api.impl.BloodMagicAPI;
import WayofTime.bloodmagic.api.impl.BloodMagicRecipeRegistrar;
import com.teamdman.animus.registry.AnimusRecipes;

@BloodMagicPlugin
public class AnimusPlugin implements IBloodMagicPlugin {
	@Override
	public void register(IBloodMagicAPI apiInterface) {
		BloodMagicAPI api = (BloodMagicAPI) apiInterface;
	}

	@Override
	public void registerRecipes(IBloodMagicRecipeRegistrar recipeRegistrar) {
		AnimusRecipes.registerAltarRecipes((BloodMagicRecipeRegistrar) recipeRegistrar);
		AnimusRecipes.registerAlchemyTableRecipes((BloodMagicRecipeRegistrar) recipeRegistrar);
		AnimusRecipes.registerTartaricForgeRecipes((BloodMagicRecipeRegistrar) recipeRegistrar);
		AnimusRecipes.registerAlchemyArrayRecipes((BloodMagicRecipeRegistrar) recipeRegistrar);
	}
}