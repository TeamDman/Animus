package com.teamdman_9201.nova.handlers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import WayofTime.alchemicalWizardry.api.tile.IBloodAltar;


public class NOVABloodappleHandler {

	public static float scalingOfSacrifice = 0.001f;
	


	
	public static boolean addBloodToAltar(EntityPlayer player)
	{
		if (!(player instanceof EntityLivingBase))
			return false;
		
		
			if(findAndFillAltar(player.getEntityWorld(), player, (int)(100)))
				{									
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, 400));
				player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id,400));
				player.addPotionEffect(new PotionEffect(Potion.hunger.id,400));
				return true;
				}

				
		return false;
	}
	
	public static float getModifier(float amount)
	{
		return 1 + amount*scalingOfSacrifice;
	}
	
	public static boolean findAndFillAltar(World world, EntityPlayer player, int amount)
    {
        int posX = (int) Math.round(player.posX - 0.5f);
        int posY = (int) player.posY;
        int posZ = (int) Math.round(player.posZ - 0.5f);
        IBloodAltar altarEntity = getAltar(world, posX, posY, posZ);

        if (altarEntity == null)
        {
            return false;
        }

        altarEntity.sacrificialDaggerCall(amount, false);
        altarEntity.startCycle();
        
        return true;
    }

    public static IBloodAltar getAltar(World world, int x, int y, int z)
    {
        TileEntity tileEntity;

        for (int i = -2; i <= 2; i++)
        {
            for (int j = -2; j <= 2; j++)
            {
                for (int k = -2; k <= 1; k++)
                {
                    tileEntity = world.getTileEntity(i + x, k + y, j + z);
                    
                    if(tileEntity instanceof IBloodAltar)
                    {
                    	return (IBloodAltar)tileEntity;
                    }
                }
            }
        }

        return null;
    }
}