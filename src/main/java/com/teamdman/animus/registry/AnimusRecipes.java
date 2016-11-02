package com.teamdman.animus.registry;

import WayofTime.bloodmagic.api.recipe.ShapedBloodOrbRecipe;
import WayofTime.bloodmagic.registry.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class AnimusRecipes {
	public static void init() {
		initCrafting();
	}

	public static void initCrafting() {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaWood), " a ", "a a", "  b", 'a', Blocks.PLANKS, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaStone), " a ", "a a", "  b", 'a', Blocks.COBBLESTONE, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaIron), " a ", "a a", "  b", 'a', Items.IRON_INGOT, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaGold), " a ", "a a", "  b", 'a', Items.GOLD_INGOT, 'b', Items.STICK));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AnimusItems.kamaDiamond), " a ", "a a", "  b", 'a', Items.DIAMOND, 'b', Items.STICK));

		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilChains), "aba", "bcb", "ada", 'a', Blocks.END_STONE, 'b', Blocks.REDSTONE_LAMP, 'c', new ItemStack(ModItems.slate, 1, 3), 'd', ModItems.orbMaster));
		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilTransposition), "aba", "bcb", "ada", 'a', Blocks.OBSIDIAN, 'b', Items.ENDER_PEARL, 'c', new ItemStack(ModItems.slate, 1, 3), 'd', ModItems.orbMaster));
		GameRegistry.addRecipe(new ShapedBloodOrbRecipe(new ItemStack(AnimusItems.sigilBuilder), "aba", "bcb", "ada", 'a', Items.SUGAR, 'b', Items.POTIONITEM, 'c',new ItemStack(ModItems.slate, 1, 3), 'd', ModItems.orbApprentice));
	}
}
