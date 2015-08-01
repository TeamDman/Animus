package com.teamdman_9201.nova.items;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-07-31.
 */
public class ItemBloodApple extends ItemFood {
    @SideOnly(Side.CLIENT)
    IIcon icon;

    public ItemBloodApple(int heal, float sat, boolean wolfy) {
        super(heal, sat, wolfy);
        setAlwaysEdible();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        return icon;
    }

    @Override
    public ItemStack onEaten(ItemStack food, World world, EntityPlayer player) {
        SoulNetworkHandler.addCurrentEssenceToMaximum(SoulNetworkHandler.getUsername(player), 100, 2500);
        return super.onEaten(food, world, player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        icon = reg.registerIcon(NOVA.MODID + ":itemBoundApple");
    }
}
