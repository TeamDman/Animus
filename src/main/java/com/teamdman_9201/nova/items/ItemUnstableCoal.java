package com.teamdman_9201.nova.items;

import com.teamdman_9201.nova.NOVA;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

public class ItemUnstableCoal extends Item {

    public ItemUnstableCoal() {
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List lore, boolean p_77624_4_) {
        lore.add(StatCollector.translateToLocal("item.itemUnstableCoal.lore"));
    }

    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        return Color.HSBtoRGB(0, Math.abs((((System.currentTimeMillis()/4)%720)-360)/360F), 1F);
    }


    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer me) {
        stack.stackSize--;
        double x = me.posX;
        double y = me.posY;
        double z = me.posZ;
        Explosion boom = world.createExplosion(null, x, y, z, 100, true);
        DamageSource ultimate = new DamageSource("NOVA.absolute").setDamageAllowedInCreativeMode().setDamageBypassesArmor().setDamageIsAbsolute();
        int d0 = 5;
        AxisAlignedBB region = AxisAlignedBB.getBoundingBox(x - 1, y - 2, z - 1, x + 1, y + 2, z + 1).expand(d0, d0, d0);
        List<EntityPlayer> list = world.getEntitiesWithinAABB(EntityPlayer.class, region);
        for (EntityPlayer hit : list) {
            System.out.printf("Killing %s\n", hit.getDisplayName());
            if (!NOVA.isDevEnv)
                hit.attackEntityFrom(ultimate, Integer.MAX_VALUE);
            hit.addPotionEffect(new PotionEffect(Potion.wither.getId(), 6000, 4));
            hit.motionY=10;
            if (hit.getHealth() > 0 && !world.isRemote)
                MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(hit.getDisplayName() + " cheated death."));
        }
        return stack;
    }

}
