package com.teamdman_9201.nova.items;

import com.teamdman_9201.nova.NOVA;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 * Created by TeamDman on 2015-07-31.
 */
public class ItemRedundantOrb extends Item {

    @SideOnly(Side.CLIENT)
    IIcon happy;
    @SideOnly(Side.CLIENT)
    IIcon sad;

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack me) {
        return happy;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        happy = reg.registerIcon(NOVA.MODID + ":itemRedundantOrbHappy");
        sad = reg.registerIcon(NOVA.MODID + ":itemRedundantOrbSad");
    }

}
