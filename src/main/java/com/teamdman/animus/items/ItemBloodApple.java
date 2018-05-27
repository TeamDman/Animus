package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.ritual.AreaDescriptor;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.common.util.AnimusUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;

public class ItemBloodApple extends ItemFood implements IVariantProvider {
	private final AreaDescriptor altarRange   = new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11);
	private       BlockPos       offsetCached = new BlockPos(0, 0, 0);

	public ItemBloodApple() {
		super(3, false);
		setPotionEffect(new PotionEffect(MobEffects.NAUSEA, 40), 0.75f);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		if (entityLiving instanceof EntityPlayer && !(entityLiving instanceof FakePlayer)) {
			EntityPlayer entityplayer = (EntityPlayer) entityLiving;
			entityplayer.getFoodStats().addStats(this, stack);
			worldIn.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
			this.onFoodEaten(stack, worldIn, entityplayer);
			//noinspection ConstantConditions
			entityplayer.addStat(StatList.getObjectUseStats(this));
			if (entityplayer instanceof EntityPlayerMP)
				CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) entityplayer, stack);

			//check for altar in range, if it is, put blood in altar, otherwise give blood to the player
			TileEntity tile = AnimusUtil.getNearbyAltar(worldIn, altarRange, entityLiving.getPosition(), offsetCached);
			//code for adding blood to network/altar
			if (tile != null) {
				((TileAltar) tile).sacrificialDaggerCall(AnimusConfig.general.bloodPerApple * 2, true);
				offsetCached = tile.getPos();
			} else { //Player is not near an altar, give their LP network blood directly
				SoulNetwork network = NetworkHelper.getSoulNetwork(entityLiving.getUniqueID());
				if (network != null) {
					network.add(AnimusConfig.general.bloodPerApple, 10000);
				}
			}
		}
		stack.shrink(1);
		return stack;
	}

	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}

}
