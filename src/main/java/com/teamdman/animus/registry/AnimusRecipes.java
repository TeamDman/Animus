package com.teamdman.animus.registry;

import WayofTime.bloodmagic.altar.AltarTier;
import WayofTime.bloodmagic.core.registry.AltarRecipeRegistry;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class AnimusRecipes {
	public static void init() {
		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamawood"))
			//			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaWood), " a ", "a a", "  b", 'a', Blocks.PLANKS, 'b', Items.STICK));

			if (!AnimusConfig.itemBlacklist.contains("animus:itemkamastone"))
				//GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaStone), " a ", "a a", "  b", 'a', Blocks.COBBLESTONE, 'b', Items.STICK));

				if (!AnimusConfig.itemBlacklist.contains("animus:itemkamairon"))
					//GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaIron), " a ", "a a", "  b", 'a', Items.IRON_INGOT, 'b', Items.STICK));

					if (!AnimusConfig.itemBlacklist.contains("animus:itemkamagold"))
						//GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaGold), " a ", "a a", "  b", 'a', Items.GOLD_INGOT, 'b', Items.STICK));

						if (!AnimusConfig.itemBlacklist.contains("animus:itemkamadiamond"))
							//GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaDiamond), " a ", "a a", "  b", 'a', Items.DIAMOND, 'b', Items.STICK));

							if (!AnimusConfig.itemBlacklist.contains("animus:itemaltardiviner"))
								//GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.altarDiviner), "aaa", "aba", "aaa", 'a', RegistrarBloodMagicBlocks.BLOOD_RUNE, 'b', RegistrarBloodMagicItems.RITUAL_DIVINER));

								if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilchains"))
									//GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilChains), "aba", "bcb", "ada", 'a', Blocks.END_STONE, 'b', Blocks.OBSIDIAN, 'c', new ItemStack(RegistrarBloodMagicItems.SLATE, 1, 2), 'd', OrbRegistry.getOrbStack(RegistrarBloodMagicItems.ORB_MAGICIAN)));

									if (!AnimusConfig.itemBlacklist.contains("animus:itemsigiltransposition"))
										//GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilTransposition), "aba", "bcb", "ada", 'a', Blocks.OBSIDIAN, 'b', Items.ENDER_PEARL, 'c', new ItemStack(RegistrarBloodMagicItems.SLATE, 1, 2), 'd', OrbRegistry.getOrbStack(RegistrarBloodMagicItems.ORB_MAGICIAN)));

										if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilbuilder"))
											//GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilBuilder), "aba", "bcb", "ada", 'a', Items.SUGAR, 'b', Items.POTIONITEM, 'c', new ItemStack(RegistrarBloodMagicItems.SLATE, 1, 2), 'd', OrbRegistry.getOrbStack(RegistrarBloodMagicItems.ORB_MAGICIAN)));

											if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilconsumption"))
												//GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilConsumption), "aba", "bcb", "ada", 'a', Blocks.OBSIDIAN, 'b', Blocks.END_STONE, 'c', new ItemStack(RegistrarBloodMagicItems.SLATE, 1, 2), 'd', OrbRegistry.getOrbStack(RegistrarBloodMagicItems.ORB_MAGICIAN)));

												if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilstorm"))
													//GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilStorm), "aaa", "aba", "aca", 'a', Items.FISHING_ROD, 'b', new ItemStack(RegistrarBloodMagicItems.SLATE, 1, 1), 'c', OrbRegistry.getOrbStack(RegistrarBloodMagicItems.ORB_APPRENTICE)));

													if (!AnimusConfig.itemBlacklist.contains("animus:itemsigilleech"))
														//GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilLeech), "dad", "aba", "dcd", 'a', Blocks.LEAVES, 'b', new ItemStack(RegistrarBloodMagicItems.SLATE, 1, 1), 'c', OrbRegistry.getOrbStack(RegistrarBloodMagicItems.ORB_APPRENTICE), 'd', Items.GOLDEN_APPLE));

														if (!AnimusConfig.itemBlacklist.contains("animus:itemfragmenthealing"))
															AltarRecipeRegistry.registerRecipe(new AltarRecipeRegistry.AltarRecipe(new ItemStack(Items.PRISMARINE_SHARD), new ItemStack(AnimusItems.FRAGMENTHEALING), AltarTier.TWO, 1000, 20, 25));
		//TODO: fix guidebook crash with this
//		if (!AnimusConfig.itemBlacklist.contains("animus:itemkamabound") && !AnimusConfig.itemBlacklist.contains("animus:itemkamadiamond"))
//			AlchemyArrayRecipeRegistry.registerRecipe(ComponentTypes.REAGENT_BINDING.getStack(), new ItemStack(AnimusItems.kamaDiamond), new AlchemyArrayEffectBinding("boundKama", Utils.setUnbreakable(new ItemStack(AnimusItems.kamaBound))), new BindingAlchemyCircleRenderer());

	}

	public static void addGuideRecipe() {
		//AltarRecipeRegistry.registerRecipe(new AltarRecipeRegistry.AltarRecipe(new ItemStack(Items.PAPER), GuideAPI.getStackFromBook(AnimusGuide.book), EnumAltarTier.ONE, 200, 5, 5));
	}
}
