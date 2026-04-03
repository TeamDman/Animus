package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.entities.EntitySentientArrow;
import com.breakinblocks.animusnv.util.SpiritusTypeHelper;
import com.breakinblocks.animusnv.util.WillWeaponStats;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Sentient Bow - A demon-will powered bow
 * Consumes 1 will per shot instead of arrows
 * Attunes to the Spiritus type in the player's gems
 * Fires spectral-like arrows that vanish after impact
 * Drops Spiritus matching the bow's attuned type on kill
 */
public class ItemSentientBow extends BowItem {
    // Damage scaling by will type (base arrow damage is 2, these are bonus)
    public static final double[] defaultDamageAdded = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
    public static final double[] corrosiveDamageAdded = new double[]{1.5, 2.5, 3.5, 5.0, 6.0};
    public static final double[] destructiveDamageAdded = new double[]{2.0, 3.5, 5.0, 7.0, 9.0};
    public static final double[] vengefulDamageAdded = new double[]{1.0, 1.5, 2.5, 3.5, 4.5};
    public static final double[] steadfastDamageAdded = new double[]{0.5, 1.0, 2.0, 3.0, 4.0};

    // Effect durations (in ticks) - poisonTime differs from spear, so kept here
    public static final int[] poisonTime = new int[]{40, 60, 100, 140, 200};
    public static final int[] slowTime = new int[]{60, 100, 140, 180, 240};
    public static final int[] slowLevel = new int[]{0, 1, 1, 2, 2};

    // Default will cost per shot (used before config loads)
    public static final double DEFAULT_WILL_COST = 1.0;

    public static double getWillCostPerShot() {
        try {
            return AnimusConfig.weapons.sentientBowWillCost.get();
        } catch (IllegalStateException e) {
            return DEFAULT_WILL_COST;
        }
    }

    public ItemSentientBow() {
        super(new Item.Properties()
            .stacksTo(1)
            .durability(500)
            .rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        SpiritusType type = getCurrentType(stack);
        String displayType = type == SpiritusType.DEFAULT ? "raw" : type.name().toLowerCase();
        tooltip.add(Component.translatable("tooltip.animusnv.sentient_bow.will_type", displayType)
            .withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_WILL_DROPS)
            .withStyle(ChatFormatting.YELLOW));
    }

    public static int getLevel(ItemStack stack, double soulsRemaining) {
        return WillWeaponStats.getLevel(soulsRemaining);
    }

    public static double getDamageAdded(SpiritusType type, int level) {
        level = Math.min(level, 4);
        return switch (type) {
            case CORROSIVE -> corrosiveDamageAdded[level];
            case DESTRUCTIVE -> destructiveDamageAdded[level];
            case VENGEFUL -> vengefulDamageAdded[level];
            case STEADFAST -> steadfastDamageAdded[level];
            default -> defaultDamageAdded[level];
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        SpiritusType type = getCurrentType(stack);
        double soulsRemaining = getTotalSpiritusOfType(player, type);
        if (!player.getAbilities().instabuild && soulsRemaining < getWillCostPerShot()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.sentient_bow.out_of_will")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        SpiritusType type = getCurrentType(stack);
        double soulsRemaining = getTotalSpiritusOfType(player, type);

        if (soulsRemaining < getWillCostPerShot()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.sentient_bow.out_of_will")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return;
        }

        int useDuration = this.getUseDuration(stack, entity) - timeLeft;
        float power = getPowerForTime(useDuration);

        if (power < 0.1F) {
            return;
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            drainSpiritusFromPlayer(player, type, getWillCostPerShot());

            int willLevel = getLevel(stack, soulsRemaining);
            double bonusDamage = getDamageAdded(type, willLevel);

            EntitySentientArrow arrow = new EntitySentientArrow(level, player);
            arrow.setWillType(type);
            arrow.setWillLevel(willLevel);
            arrow.setBonusDamage(bonusDamage);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

            int powerEnchant = stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.POWER));
            if (powerEnchant > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchant * 0.5D + 0.5D);
            }

            int flameEnchant = stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FLAME));
            if (flameEnchant > 0) {
                arrow.setRemainingFireTicks(100);
            }

            if (power >= 1.0F) {
                arrow.setCritArrow(true);
            }

            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

            level.addFreshEntity(arrow);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

            stack.hurtAndBreak(1, serverLevel, player, (item) -> {});
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (stack) -> false;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    public SpiritusType getCurrentType(ItemStack stack) {
        return SpiritusTypeHelper.getCurrentType(stack);
    }

    public void setCurrentType(ItemStack stack, SpiritusType type) {
        SpiritusTypeHelper.setCurrentType(stack, type);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (entity instanceof Player player) {
            SpiritusType newType = SpiritusTypeHelper.findSpiritusType(player);
            if (newType != getCurrentType(stack)) {
                setCurrentType(stack, newType);
            }
        }
    }

    private static double getTotalSpiritusOfType(Player player, SpiritusType type) {
        return SpiritusTypeHelper.getTotalSpiritusOfType(player, type);
    }

    private static void drainSpiritusFromPlayer(Player player, SpiritusType type, double amount) {
        SpiritusTypeHelper.drainSpiritusFromPlayer(player, type, amount);
    }

    public List<ItemStack> getRandomSpiritusDrop(LivingEntity killedEntity, LivingEntity attackingEntity,
                                                   ItemStack stack, int looting) {
        return new ArrayList<>();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || getCurrentType(stack) != SpiritusType.DEFAULT;
    }
}
