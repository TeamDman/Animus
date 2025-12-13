package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntityThrownSpear;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Spear - A Roman-style throwable javelin
 * Deals AOE damage where it lands when thrown
 * Normal melee attacks do NOT have AOE (only Bound Spear has AOE melee)
 */
public class ItemSpear extends TridentItem {
    protected final Tier tier;

    public ItemSpear(Tier tier) {
        super(new Properties().durability(tier.getUses()));
        this.tier = tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (tier == Tiers.IRON) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_IRON_FLAVOUR));
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_IRON_INFO));
        } else if (tier == Tiers.DIAMOND) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_DIAMOND_FLAVOUR));
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_DIAMOND_INFO));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int useDuration = this.getUseDuration(stack, entity) - timeLeft;
            if (useDuration >= 10) {
                int riptide = getRiptideLevel(stack, level);
                if (riptide <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide) {
                        // Use the new hurtAndBreak signature for 1.21
                        stack.hurtAndBreak(1, (ServerLevel) level, player, (item) ->
                            player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                        if (riptide == 0) {
                            // Spawn our custom spear entity instead of vanilla trident
                            EntityThrownSpear thrownSpear = new EntityThrownSpear(level, player, stack);
                            thrownSpear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                            if (player.getAbilities().instabuild) {
                                thrownSpear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            level.addFreshEntity(thrownSpear);
                            // Use .value() for Holder<SoundEvent>
                            level.playSound(null, thrownSpear.getX(), thrownSpear.getY(), thrownSpear.getZ(),
                                SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
                        // Updated signature for 1.21: startAutoSpinAttack(int ticks, float damage, ItemStack stack)
                        player.startAutoSpinAttack(20, 8.0F + (float)riptide * 2.0F, stack);
                        if (player.onGround()) {
                            player.move(net.minecraft.world.entity.MoverType.SELF, new net.minecraft.world.phys.Vec3(0.0, 1.2, 0.0));
                        }

                        // Use .value() for Holder<SoundEvent>
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    /**
     * Get riptide enchantment level from the stack
     * In 1.21, enchantment access changed
     */
    private int getRiptideLevel(ItemStack stack, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.RIPTIDE));
        }
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Allow shield blocking when sneaking with a shield in the other hand
        if (player.isShiftKeyDown()) {
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (otherStack.getItem() instanceof net.minecraft.world.item.ShieldItem) {
                return InteractionResultHolder.pass(stack);
            }
        }

        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        } else if (getRiptideLevel(stack, level) > 0 && !player.isInWaterOrRain()) {
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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    /**
     * Get the tier of this spear
     */
    public Tier getTier() {
        return tier;
    }

    // Note: canApplyAtEnchantingTable and isBookEnchantable were removed in 1.21
    // Enchantment compatibility is now handled through enchantment tags
}
