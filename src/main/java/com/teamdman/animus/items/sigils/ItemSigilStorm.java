package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.api.impl.ItemSigil;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.client.IVariantProvider;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemSigilStorm extends ItemSigil implements IVariantProvider {
	public ItemSigilStorm() {
		super(AnimusConfig.transpositionConsumption);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		Random rand = new Random();
		RayTraceResult result = this.rayTrace(world, player, true);
		if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = result.getBlockPos();
			IBlockState state = world.getBlockState(pos);

			world.spawnEntity(new EntityLightningBolt(world, pos.getX(), pos.getY() + .5, pos.getZ(), false));

			NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, getLpUsed());

			if (state.getBlock() == Blocks.WATER && !world.isRemote) {
				EntityItem fish = new EntityItem(world, pos.getX(), pos.getY() - rand.nextInt(2), pos.getZ(), new ItemStack(Items.FISH, 1 + rand.nextInt(2)));
				fish.setVelocity(rand.nextDouble() * .25, -.25, rand.nextDouble() * .25);
				fish.setEntityInvulnerable(true); //Stop killing our fish, stupid lightning.
				world.spawnEntity(fish);

			}
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	protected RayTraceResult rayTrace(World worldIn, EntityPlayer playerIn, boolean useLiquids) {
		float f = playerIn.rotationPitch;
		float f1 = playerIn.rotationYaw;
		double d0 = playerIn.posX;
		double d1 = playerIn.posY + (double) playerIn.getEyeHeight();
		double d2 = playerIn.posZ;
		Vec3d vec3d = new Vec3d(d0, d1, d2);
		float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * 0.017453292F);
		float f5 = MathHelper.sin(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = 25.0D;
		if (playerIn instanceof net.minecraft.entity.player.EntityPlayerMP) {
			d3 = ((net.minecraft.entity.player.EntityPlayerMP) playerIn).interactionManager.getBlockReachDistance();
		}
		Vec3d vec3d1 = vec3d.addVector((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
		return worldIn.rayTraceBlocks(vec3d, vec3d1, useLiquids, !useLiquids, false);
	}

	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}
