package com.teamdman.animus.registry;

import WayofTime.bloodmagic.altar.AltarTier;
import WayofTime.bloodmagic.api.impl.BloodMagicRecipeRegistrar;
import WayofTime.bloodmagic.item.ItemSlate;
import WayofTime.bloodmagic.util.Utils;
import com.teamdman.animus.types.ComponentTypes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

public class AnimusRecipes {
	public static void init() {
		OreDictionary.registerOre("foodCooked", Items.COOKED_BEEF);
		OreDictionary.registerOre("foodCooked", Items.COOKED_CHICKEN);
		OreDictionary.registerOre("foodCooked", Items.COOKED_FISH);
		OreDictionary.registerOre("foodCooked", Items.COOKED_MUTTON);
		OreDictionary.registerOre("foodCooked", Items.COOKED_PORKCHOP);
		OreDictionary.registerOre("foodCooked", Items.COOKED_RABBIT);
	}

	public static void registerAltarRecipes(BloodMagicRecipeRegistrar registrar) {
		registrar.addBloodAltar(Ingredient.fromItem(Items.PRISMARINE_SHARD), new ItemStack(AnimusItems.FRAGMENTHEALING), AltarTier.TWO.ordinal(), 1000, 20, 25);
		registrar.addBloodAltar(new OreIngredient("ingotGold"), new ItemStack(AnimusItems.KEYBINDING), AltarTier.THREE.ordinal(), 1000, 20, 25);
	}

	public static void registerAlchemyTableRecipes(BloodMagicRecipeRegistrar registrar) {

	}

	public static void registerTartaricForgeRecipes(BloodMagicRecipeRegistrar registrar) {
		registrar.addTartaricForge(ComponentTypes.REAGENT_BUILDER.getStack(), 128, 32, Items.SUGAR, Blocks.CRAFTING_TABLE, Blocks.DISPENSER, Blocks.BRICK_BLOCK);
		registrar.addTartaricForge(ComponentTypes.REAGENT_CHAINS.getStack(), 128, 32, Blocks.IRON_BARS, Items.ENDER_PEARL, Items.GLASS_BOTTLE, Blocks.END_STONE);
		registrar.addTartaricForge(ComponentTypes.REAGENT_CONSUMPTION.getStack(), 128, 32, Items.IRON_PICKAXE, Items.IRON_PICKAXE, Items.IRON_PICKAXE, Items.IRON_PICKAXE);
		registrar.addTartaricForge(ComponentTypes.REAGENT_LEECH.getStack(), 64, 20, "treeSapling", "treeLeaves", Blocks.TALLGRASS, "foodCooked");
		registrar.addTartaricForge(ComponentTypes.REAGENT_STORM.getStack(), 64, 20, Blocks.SAND, Items.WATER_BUCKET, Items.FISHING_ROD, Items.GHAST_TEAR);
		registrar.addTartaricForge(ComponentTypes.REAGENT_TRANSPOSITION.getStack(), 128, 32, Blocks.END_STONE, Items.ENDER_PEARL, Blocks.OBSIDIAN, Blocks.CHEST);
	}

	public static void registerAlchemyArrayRecipes(BloodMagicRecipeRegistrar registrar) {
		registrar.addAlchemyArray(new ItemStack(AnimusItems.KAMA_DIAMOND), WayofTime.bloodmagic.item.types.ComponentTypes.REAGENT_BINDING.getStack(), Utils.setUnbreakable(new ItemStack(AnimusItems.KAMA_BOUND)), null);
		registrar.addAlchemyArray(ComponentTypes.REAGENT_BUILDER.getStack(), ItemSlate.SlateType.REINFORCED.getStack(), new ItemStack(AnimusItems.SIGIL_BUILDER), null);
		registrar.addAlchemyArray(ComponentTypes.REAGENT_CHAINS.getStack(), ItemSlate.SlateType.IMBUED.getStack(), new ItemStack(AnimusItems.SIGIL_CHAINS), null);
		registrar.addAlchemyArray(ComponentTypes.REAGENT_CONSUMPTION.getStack(), ItemSlate.SlateType.IMBUED.getStack(), new ItemStack(AnimusItems.SIGIL_CONSUMPTION), null);
		registrar.addAlchemyArray(ComponentTypes.REAGENT_LEECH.getStack(), ItemSlate.SlateType.REINFORCED.getStack(), new ItemStack(AnimusItems.SIGIL_LEECH), null);
		registrar.addAlchemyArray(ComponentTypes.REAGENT_STORM.getStack(), ItemSlate.SlateType.REINFORCED.getStack(), new ItemStack(AnimusItems.SIGIL_STORM), null);
		registrar.addAlchemyArray(ComponentTypes.REAGENT_TRANSPOSITION.getStack(), ItemSlate.SlateType.DEMONIC.getStack(), new ItemStack(AnimusItems.SIGIL_TRANSPOSITION), null);
	}

	public static void addGuideRecipe() {
		//AltarRecipeRegistry.registerRecipe(new AltarRecipeRegistry.AltarRecipe(new ItemStack(Items.PAPER), GuideAPI.getStackFromBook(AnimusGuide.book), EnumAltarTier.ONE, 200, 5, 5));
	}
}
