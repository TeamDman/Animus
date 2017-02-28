package com.teamdman.animus.items.sigils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.teamdman.animus.client.resources.EffectHandler;
import com.teamdman.animus.client.resources.fx.EntityFXBurst;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;

import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.api.ritual.AreaDescriptor;
import WayofTime.bloodmagic.api.util.helper.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.IPlantable;


public class ItemSigilLeech extends ItemSigilToggleableBase {
	public static final String EFFECT_RANGE = "effect";
	Random random = new Random();
	protected final Map<String, AreaDescriptor> modableRangeMap = new HashMap<String, AreaDescriptor>();
	public ItemSigilLeech() {
		super("leech", 5);
	}
	
	public ItemStack getFood(EntityPlayer player) {
		int i;
		for (i = 0; i < player.inventory.mainInventory.length; i++){
			if (player.inventory.mainInventory[i] == null)
				continue;
			Item food = player.inventory.mainInventory[i].getItem();
			if (food == null)
				continue;

			if (food instanceof IPlantable){
				return player.inventory.mainInventory[i];
			}
			
		}
			
		return null;
	}
	
	public boolean eatGrowables(EntityPlayer player) {
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 0, 0), 10));
		AreaDescriptor eatRange;
		eatRange = getBlockRange(EFFECT_RANGE);
		eatRange.resetIterator();
		int count = random.nextInt(4);
		
		while (eatRange.hasNext()) {
			int i = 0;
			BlockPos nextPos = eatRange.next().add(player.getPosition());
			Block thisBlock = player.world.getBlockState(nextPos).getBlock();
			if (thisBlock == Blocks.AIR)
				continue;
			boolean edible = false;

				String blockName = thisBlock.getUnlocalizedName().toLowerCase();

				if (thisBlock instanceof BlockCrops || thisBlock instanceof BlockLog
						|| thisBlock instanceof BlockLeaves || thisBlock instanceof BlockFlower
						|| thisBlock instanceof BlockTallGrass || thisBlock instanceof BlockDoublePlant
						|| blockName.contains("extrabiomesxl.flower"))
					edible = true;

				if (blockName.contains("specialflower") || blockName.contains("shinyflower"))
					edible = false;

				if (!edible)
					continue;

				EffectHandler.getInstance().registerFX(
						new EntityFXBurst(1, nextPos.getX() + 0.5, nextPos.getY() + 0.5, nextPos.getZ() + .5, 1F));

				player.world.playSound(null, nextPos, AnimusSoundEventHandler.naturesleech, SoundCategory.BLOCKS, .4F, 1F);
				player.world.setBlockToAir(nextPos);
				i++;
				if (i>=count)
				    return true;

		}
		return false;
	}
	

	public ItemStack getEdible(EntityPlayer player){
		ItemStack food;
		if ((food = getFood(player)) != null)
			return food;
		else
		return null;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		boolean eaten = false;
		if (getActivated(stack)){
			if (entityIn instanceof EntityPlayer && !(entityIn instanceof FakePlayer)){
				EntityPlayer player = (EntityPlayer) entityIn;
				if (player.canEat(false)){
					ItemStack haseditable = null;
					haseditable = getEdible(player);
					if (haseditable != null)
					{
						haseditable.stackSize -= Math.min(random.nextInt(4), haseditable.stackSize);
						eaten = true;
					}
					else if (eatGrowables(player) == true){
						eaten = true;
					}
					if (eaten){
						int fill = 1+random.nextInt(3);
						player.getFoodStats().addStats(fill, 2F);
					}
				}
			}
		}
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote && !isUnusable(stack)) {
				NBTTagCompound comp = NBTHelper.checkNBT(stack).getTagCompound();
				boolean activated = getActivated(stack);
				comp.setBoolean(Constants.NBT.ACTIVATED, !activated);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}
    public void addBlockRange(String range, AreaDescriptor defaultRange)
    {
        modableRangeMap.put(range, defaultRange);
    }

    /**
     * Used to grab the range of a ritual for a given effect.
     * 
     * @param range
     *        - Range that needs to be pulled.
     * @return -
     */
    public AreaDescriptor getBlockRange(String range)
    {
        if (modableRangeMap.containsKey(range))
        {
            return modableRangeMap.get(range);
        }

        return null;
    }
    
    
}
