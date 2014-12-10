package com.TeamDman_9201.NOVA;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class CoalDiamondOre extends Block {
	public CoalDiamondOre() {
		super(Material.rock);
		// setUnlocalizedName("coalDiamondOre");
		setBlockName("coalDiamondOre");
		setBlockTextureName(First.MODID + ":" + "coalDiamondOre");
		setCreativeTab(First.firstTab);
		setHardness(6);

	}
}
