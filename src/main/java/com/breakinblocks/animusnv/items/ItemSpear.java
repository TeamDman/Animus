package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.entities.EntityThrownSpear;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Spear - A Roman-style throwable javelin
 * Deals AOE damage where it lands when thrown
 * Normal melee attacks do NOT have AOE (only Bound Spear has AOE melee)
 */
public class ItemSpear extends TridentItem {
    protected final ToolMaterial material;

    public ItemSpear(ToolMaterial material, Properties properties) {
        super(properties.durability(material.durability()));
        this.material = material;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        if (material == ToolMaterial.IRON) {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_IRON_FLAVOUR));
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_IRON_INFO));
        } else if (material == ToolMaterial.DIAMOND) {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_DIAMOND_FLAVOUR));
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_DIAMOND_INFO));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int useDuration = this.getUseDuration(stack, entity) - timeLeft;
            if (useDuration >= 10) {
                int riptide = getRiptideLevel(stack, level);
                if (riptide <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide()) {
                        stack.hurtAndBreak(1, (ServerLevel) level, player, (item) ->
                            player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                        if (riptide == 0) {
                            EntityThrownSpear thrownSpear = new EntityThrownSpear(level, player, stack);
                            thrownSpear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                            if (player.getAbilities().instabuild) {
                                thrownSpear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            level.addFreshEntity(thrownSpear);
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
                        float xSpeed = -Mth.sin(yaw * ((float)Math.PI / 180F)) * Mth.cos(pitch * ((float)Math.PI / 180F));
                        float ySpeed = -Mth.sin(pitch * ((float)Math.PI / 180F));
                        float zSpeed = Mth.cos(yaw * ((float)Math.PI / 180F)) * Mth.cos(pitch * ((float)Math.PI / 180F));
                        float length = Mth.sqrt(xSpeed * xSpeed + ySpeed * ySpeed + zSpeed * zSpeed);
                        float multiplier = 3.0F * ((1.0F + (float)riptide) / 4.0F);
                        xSpeed = xSpeed * (multiplier / length);
                        ySpeed = ySpeed * (multiplier / length);
                        zSpeed = zSpeed * (multiplier / length);
                        player.push((double)xSpeed, (double)ySpeed, (double)zSpeed);
                        player.startAutoSpinAttack(20, 8.0F + (float)riptide * 2.0F, stack);
                        if (player.onGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
                        }

                        // Use .value() for Holder<SoundEvent>
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private int getRiptideLevel(ItemStack stack, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.RIPTIDE));
        }
        return 0;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (otherStack.getItem() instanceof ShieldItem) {
                return InteractionResult.PASS;
            }
        }

        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResult.FAIL;
        } else if (getRiptideLevel(stack, level) > 0 && !player.isInWaterOrRain()) {
            return InteractionResult.FAIL;
        } else {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public ToolMaterial getMaterial() {
        return material;
    }
}
