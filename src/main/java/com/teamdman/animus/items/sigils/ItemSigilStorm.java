package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.item.sigil.ItemSigilBase;
import WayofTime.bloodmagic.ritual.AreaDescriptor;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.teamdman.animus.Constants;
import com.teamdman.animus.common.util.AnimusUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemSigilStorm extends ItemSigilBase implements IVariantProvider {
	public static final String                      EFFECT_RANGE    = "effect";
	protected final     Map<String, AreaDescriptor> modableRangeMap = new HashMap<>();

	public ItemSigilStorm() {
		super(Constants.Sigils.STORM, 500);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		Random    rand  = new Random();
		BlockPos  pos   = null;
		int       damage;


		RayTraceResult result = AnimusUtil.raytraceFromEntity(world, player, true, 64);

		if (result != null) {

			if (result.typeOfHit == RayTraceResult.Type.BLOCK)
				pos = result.getBlockPos();
			if (result.typeOfHit == RayTraceResult.Type.ENTITY)
				pos = result.entityHit.getPosition();

			if (pos == null) {
				return new ActionResult<>(EnumActionResult.FAIL, stack);
			}

			world.spawnEntity(new EntityLightningBolt(world, pos.getX(), pos.getY() + .5, pos.getZ(), false));

			IBlockState state = world.getBlockState(pos);
			if ((state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) && !world.isRemote) {
				EntityItem fish = new EntityItem(world, pos.getX(), pos.getY() - rand.nextInt(2), pos.getZ(), new ItemStack(Items.FISH, 1 + rand.nextInt(2)));
				fish.setVelocity(rand.nextDouble() * .25, -.25, rand.nextDouble() * .25);
				fish.setEntityInvulnerable(true);
				world.spawnEntity(fish);
			}


			if (world.isRaining()) {
				addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, -1, -1), 4));
				AreaDescriptor         damageRange = getBlockRange(EFFECT_RANGE);
				AxisAlignedBB          range       = damageRange.getAABB(pos);
				List<EntityLivingBase> list        = world.getEntitiesWithinAABB(EntityLivingBase.class, range);
				DamageSource           storm       = new DamageSource("animus.storm").setDamageBypassesArmor().setDamageIsAbsolute();
				for (EntityLivingBase livingEntity : list) {
					if (livingEntity == player)
						continue;
					damage = Math.max(6, rand.nextInt(15));
					livingEntity.attackEntityFrom(storm, damage);
				}
			}

			NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_STORM), getLpUsed()));

		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
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

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.SIGIL_STORM_FLAVOUR));
		super.addInformation(stack, world, tooltip, flag);
	}

	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}

}
