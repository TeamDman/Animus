package com.teamdman.animus.registry;

import WayofTime.bloodmagic.alchemyArray.AlchemyArrayEffectBinding;
import WayofTime.bloodmagic.api.altar.EnumAltarTier;
import WayofTime.bloodmagic.api.recipe.ShapedBloodOrbRecipe;
import WayofTime.bloodmagic.api.registry.AlchemyArrayRecipeRegistry;
import WayofTime.bloodmagic.api.registry.AltarRecipeRegistry;
import WayofTime.bloodmagic.api.registry.OrbRegistry;
import WayofTime.bloodmagic.client.render.alchemyArray.BindingAlchemyCircleRenderer;
import WayofTime.bloodmagic.item.ItemComponent;
import WayofTime.bloodmagic.registry.ModBlocks;
import WayofTime.bloodmagic.registry.ModItems;
import WayofTime.bloodmagic.util.Utils;
import amerifrance.guideapi.api.GuideAPI;
import com.teamdman.animus.AnimusGuide;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class AnimusRecipes {
	public static void init() {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaWood), " a ", "a a", "  b", 'a', Blocks.PLANKS, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaStone), " a ", "a a", "  b", 'a', Blocks.COBBLESTONE, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaIron), " a ", "a a", "  b", 'a', Items.IRON_INGOT, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaGold), " a ", "a a", "  b", 'a', Items.GOLD_INGOT, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaDiamond), " a ", "a a", "  b", 'a', Items.DIAMOND, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.altarDiviner), "aaa", "aba", "aaa", 'a', ModBlocks.BLOOD_RUNE, 'b', ModItems.RITUAL_DIVINER));


		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilChains), "aba", "bcb", "ada", 'a', Blocks.END_STONE, 'b', Blocks.OBSIDIAN, 'c', new ItemStack(ModItems.SLATE, 1, 2), 'd', OrbRegistry.getOrbStack(ModItems.ORB_MAGICIAN)));
		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilTransposition), "aba", "bcb", "ada", 'a', Blocks.OBSIDIAN, 'b', Items.ENDER_PEARL, 'c', new ItemStack(ModItems.SLATE, 1, 2), 'd', OrbRegistry.getOrbStack(ModItems.ORB_MAGICIAN)));
		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilBuilder), "aba", "bcb", "ada", 'a', Items.SUGAR, 'b', Items.POTIONITEM, 'c', new ItemStack(ModItems.SLATE, 1, 2), 'd', OrbRegistry.getOrbStack(ModItems.ORB_MAGICIAN)));
		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilConsumption), "aba", "bcb", "ada", 'a', Blocks.OBSIDIAN, 'b', Blocks.END_STONE, 'c', new ItemStack(ModItems.SLATE, 1, 2), 'd', OrbRegistry.getOrbStack(ModItems.ORB_MAGICIAN)));
		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilStorm), "aaa", "aba", "aca", 'a', Items.FISHING_ROD, 'b', new ItemStack(ModItems.SLATE, 1, 1), 'c', OrbRegistry.getOrbStack(ModItems.ORB_APPRENTICE)));

		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilLeech), "dad", "aba", "dcd", 'a', Blocks.LEAVES, 'b', new ItemStack(ModItems.SLATE, 1, 1), 'c', OrbRegistry.getOrbStack(ModItems.ORB_APPRENTICE), 'd', Items.GOLDEN_APPLE));
		
		AltarRecipeRegistry.registerRecipe(new AltarRecipeRegistry.AltarRecipe(new ItemStack(Items.PRISMARINE_SHARD), new ItemStack(AnimusItems.fragmentHealing), EnumAltarTier.TWO, 1000, 20, 25));

		AlchemyArrayRecipeRegistry.registerRecipe(ItemComponent.getStack(ItemComponent.REAGENT_BINDING), new ItemStack(AnimusItems.kamaDiamond), new AlchemyArrayEffectBinding("boundKama", Utils.setUnbreakable(new ItemStack(AnimusItems.kamaBound))), new BindingAlchemyCircleRenderer());

	}

	public static void addGuideRecipe() {
		AltarRecipeRegistry.registerRecipe(new AltarRecipeRegistry.AltarRecipe(new ItemStack(Items.PAPER), GuideAPI.getStackFromBook(AnimusGuide.book), EnumAltarTier.ONE, 200, 5, 5));
	}
}
