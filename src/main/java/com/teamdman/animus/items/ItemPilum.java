package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntityThrownPilum;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Pilum - A Roman-style throwable javelin
 * Deals AOE damage where it lands when thrown
 * Normal melee attacks do NOT have AOE (only Bound Pilum has AOE melee)
 */
public class ItemPilum extends TridentItem {
    protected final Tier tier;

    public ItemPilum(Tier tier) {
        super(new Properties().durability(tier.getUses()));
        this.tier = tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (tier == Tiers.IRON) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.PILUM_IRON_FLAVOUR));
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.PILUM_IRON_INFO));
        } else if (tier == Tiers.DIAMOND) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.PILUM_DIAMOND_FLAVOUR));
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.PILUM_DIAMOND_INFO));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int useDuration = this.getUseDuration(stack) - timeLeft;
            if (useDuration >= 10) {
                int riptide = EnchantmentHelper.getRiptide(stack);
                if (riptide <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide) {
                        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(entity.getUsedItemHand()));
                        if (riptide == 0) {
                            // Spawn our custom pilum entity instead of vanilla trident
                            EntityThrownPilum thrownPilum = new EntityThrownPilum(level, player, stack);
                            thrownPilum.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                            if (player.getAbilities().instabuild) {
                                thrownPilum.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            level.addFreshEntity(thrownPilum);
                            level.playSound(null, thrownPilum, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                            if (!player.getAbilities().instabuild) {
                                player.getInventory().removeItem(stack);
                            }
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    if (riptide > 0) {
                        float yaw = player.getYRot();
                        float pitch = player.getXRot();
                        float xSpeed = -net.minecraft.util.Mth.sin(yaw * ((float)Math.PI / 180F)) * net.minecraft.util.Mth.cos(pitch * ((float)Math.PI / 180F));
                        float ySpeed = -net.minecraft.util.Mth.sin(pitch * ((float)Math.PI / 180F));
                        float zSpeed = net.minecraft.util.Mth.cos(yaw * ((float)Math.PI / 180F)) * net.minecraft.util.Mth.cos(pitch * ((float)Math.PI / 180F));
                        float length = net.minecraft.util.Mth.sqrt(xSpeed * xSpeed + ySpeed * ySpeed + zSpeed * zSpeed);
                        float multiplier = 3.0F * ((1.0F + (float)riptide) / 4.0F);
                        xSpeed = xSpeed * (multiplier / length);
                        ySpeed = ySpeed * (multiplier / length);
                        zSpeed = zSpeed * (multiplier / length);
                        player.push((double)xSpeed, (double)ySpeed, (double)zSpeed);
                        player.startAutoSpinAttack(20);
                        if (player.onGround()) {
                            player.move(net.minecraft.world.entity.MoverType.SELF, new net.minecraft.world.phys.Vec3(0.0, 1.2, 0.0));
                        }

                        level.playSound(null, player, SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        } else if (EnchantmentHelper.getRiptide(stack) > 0 && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(stack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    /**
     * Get the tier of this pilum
     */
    public Tier getTier() {
        return tier;
    }
}
