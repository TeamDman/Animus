package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.ritual.AreaDescriptor;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ItemBloodApple extends ItemFood implements IVariantProvider {
	private static int filling = 3;
	public static final String                             ALTAR_RANGE    = "altar";
    protected final Map<String, AreaDescriptor> modableRangeMap = new HashMap<>();
    protected final Map<String, Integer> volumeRangeMap = new HashMap<>();
    protected final Map<String, Integer> horizontalRangeMap = new HashMap<>();
    protected final Map<String, Integer> verticalRangeMap = new HashMap<>();
	public BlockPos altarOffsetPos = new BlockPos(0, 0, 0);
	int bloodPerApple = 50;
	
	public ItemBloodApple() {
		super(filling, false);
	}
	
	@Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
    {
        if (entityLiving instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)entityLiving;
            entityplayer.getFoodStats().addStats(this, stack);
			worldIn.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
            this.onFoodEaten(stack, worldIn, entityplayer);
            entityplayer.addStat(StatList.getObjectUseStats(this));

            if (entityplayer instanceof EntityPlayerMP)
            {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP)entityplayer, stack);
            }

            //code for adding blood to network/altar
            if (!(entityLiving instanceof FakePlayer)){

            	//check for altar in range, if it is, put blood in altar, otherwise give blood to the player
        		TileAltar tileAltar = null;
        		addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
        		setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
        		BlockPos          altarPos            = entityLiving.getPosition();
        		TileEntity tile = worldIn.getTileEntity(altarPos);
        		AreaDescriptor altarRange = getBlockRange(ALTAR_RANGE);

        		if (!altarRange.isWithinArea(altarOffsetPos) || !(tile instanceof TileAltar)) {
        			for (BlockPos newPos : altarRange.getContainedPositions(altarPos)) {
        				TileEntity nextTile = worldIn.getTileEntity(newPos);
        				if (nextTile instanceof TileAltar) {
        					tile = nextTile;
        					altarOffsetPos = newPos.subtract(altarPos);

        					altarRange.resetCache();
        					break;
        				}
        			}
        		}
        		boolean testFlag = false;
				if (tile instanceof TileAltar) {
        			tileAltar = (TileAltar) tile;
        			testFlag  = true;
        		}
        		if (testFlag) {//Player is near an altar, fill the altar
        			tileAltar.sacrificialDaggerCall(bloodPerApple*2, true);
        			
        		}
        		else { //Player is not near an altar, give their LP network blood directly
        		
        			SoulNetwork network = NetworkHelper.getSoulNetwork(entityLiving.getUniqueID());
        			if (network != null) {
        				network.add(bloodPerApple, 10000);
        			}

        			
        		}
        		
            	    	
            }
            
            
        }

        stack.shrink(1);
        return stack;
    }
	
    public void addBlockRange(String range, AreaDescriptor defaultRange) {
        modableRangeMap.put(range, defaultRange);
    }

    public AreaDescriptor getBlockRange(String range) {
        if (modableRangeMap.containsKey(range)) {
            return modableRangeMap.get(range);
        }

        return null;
    }

    protected void setMaximumVolumeAndDistanceOfRange(String range, int volume, int horizontalRadius, int verticalRadius) {
        volumeRangeMap.put(range, volume);
        horizontalRangeMap.put(range, horizontalRadius);
        verticalRangeMap.put(range, verticalRadius);
    }
	
	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}

}
