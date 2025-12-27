package com.teamdman.animus.items;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntitySentientArrow;
import com.teamdman.animus.registry.AnimusEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.api.compat.IDemonWillWeapon;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Sentient Bow - A demon-will powered bow
 * Consumes 1 will per shot instead of arrows
 * Attunes to the demon will type in the player's gems
 * Fires spectral-like arrows that vanish after impact
 * Applies soul snare to enemies hit (for will drops on kill)
 */
public class ItemSentientBow extends BowItem implements IDemonWillWeapon {
    // Soul brackets for level progression (same as sentient spear)
    public static final double[] soulBracket = new double[]{16, 60, 200, 400, 1000};

    // Damage scaling by will type (base arrow damage is 2, these are bonus)
    public static final double[] defaultDamageAdded = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
    public static final double[] corrosiveDamageAdded = new double[]{1.5, 2.5, 3.5, 5.0, 6.0};
    public static final double[] destructiveDamageAdded = new double[]{2.0, 3.5, 5.0, 7.0, 9.0};
    public static final double[] vengefulDamageAdded = new double[]{1.0, 1.5, 2.5, 3.5, 4.5};
    public static final double[] steadfastDamageAdded = new double[]{0.5, 1.0, 2.0, 3.0, 4.0};

    // Effect durations (in ticks)
    public static final int[] poisonTime = new int[]{40, 60, 100, 140, 200};
    public static final int[] poisonLevel = new int[]{0, 0, 1, 1, 2};
    public static final int[] slowTime = new int[]{60, 100, 140, 180, 240};
    public static final int[] slowLevel = new int[]{0, 1, 1, 2, 2};

    // Default will cost per shot (used before config loads)
    public static final double DEFAULT_WILL_COST = 1.0;

    /**
     * Gets the will cost per shot from config
     */
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        EnumDemonWillType type = getCurrentType(stack);
        String displayType = type == EnumDemonWillType.DEFAULT ? "raw" : type.name().toLowerCase();
        tooltip.add(Component.translatable("tooltip.animus.sentient_bow.will_type", displayType)
            .withStyle(ChatFormatting.AQUA));

        if (level != null && level.isClientSide) {
            Player player = level.getNearestPlayer(0, 0, 0, Double.MAX_VALUE, false);
            if (player != null) {
                double soulsRemaining = getTotalWillOfType(player, type);
                int willLevel = getLevel(stack, soulsRemaining);
                tooltip.add(Component.translatable("tooltip.animus.sentient_bow.level", willLevel, (int) soulsRemaining)
                    .withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("tooltip.animus.sentient_bow.damage_bonus", String.format("%.1f", getDamageAdded(type, willLevel)))
                    .withStyle(ChatFormatting.RED));
            }
        }

        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_SNARE)
            .withStyle(ChatFormatting.YELLOW));
    }

    public static int getLevel(ItemStack stack, double soulsRemaining) {
        for (int i = 0; i < soulBracket.length; i++) {
            if (soulsRemaining < soulBracket[i]) {
                return i;
            }
        }
        return soulBracket.length;
    }

    public static double getDamageAdded(EnumDemonWillType type, int level) {
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        // Get will type and amount
        EnumDemonWillType type = getCurrentType(stack);
        double soulsRemaining = getTotalWillOfType(player, type);

        // Check if player has enough will
        if (soulsRemaining < getWillCostPerShot()) {
            // Send "out of will" message
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.sentient_bow.out_of_will")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return;
        }

        int useDuration = this.getUseDuration(stack) - timeLeft;
        float power = getPowerForTime(useDuration);

        if (power < 0.1F) {
            return;
        }

        if (!level.isClientSide) {
            // Consume will
            drainWillFromPlayer(player, type, getWillCostPerShot());

            // Calculate damage based on will level
            int willLevel = getLevel(stack, soulsRemaining);
            double bonusDamage = getDamageAdded(type, willLevel);

            // Create sentient arrow
            EntitySentientArrow arrow = new EntitySentientArrow(level, player);
            arrow.setWillType(type);
            arrow.setWillLevel(willLevel);
            arrow.setBonusDamage(bonusDamage);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

            // Apply power enchantment bonus
            int powerEnchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
            if (powerEnchant > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchant * 0.5D + 0.5D);
            }

            // Apply punch enchantment
            int punchEnchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
            if (punchEnchant > 0) {
                arrow.setKnockback(punchEnchant);
            }

            // Apply flame enchantment
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                arrow.setSecondsOnFire(100);
            }

            // Critical hit if fully charged
            if (power >= 1.0F) {
                arrow.setCritArrow(true);
            }

            // Virtual arrows don't get picked up
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

            level.addFreshEntity(arrow);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

            // Damage the bow
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Check if player has will to shoot
        EnumDemonWillType type = getCurrentType(stack);
        double soulsRemaining = getTotalWillOfType(player, type);

        if (soulsRemaining < getWillCostPerShot()) {
            // Send "out of will" message
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
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        // Sentient bow doesn't use regular arrows
        return (stack) -> false;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    public EnumDemonWillType getCurrentType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("demonWillType")) {
            try {
                return EnumDemonWillType.valueOf(stack.getTag().getString("demonWillType").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EnumDemonWillType.DEFAULT;
            }
        }
        return EnumDemonWillType.DEFAULT;
    }

    public void setCurrentType(ItemStack stack, EnumDemonWillType type) {
        stack.getOrCreateTag().putString("demonWillType", type.toString());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (entity instanceof Player player) {
            // Update the will type based on the player's inventory
            EnumDemonWillType newType = findDemonWillType(player);
            if (newType != getCurrentType(stack)) {
                setCurrentType(stack, newType);
            }
        }
    }

    /**
     * Determines the demon will type based on will available from the player's soul network
     * Returns the type with the highest will amount
     */
    private static EnumDemonWillType findDemonWillType(Player player) {
        EnumDemonWillType highestType = EnumDemonWillType.DEFAULT;
        double highestAmount = 0;

        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            double amount = PlayerDemonWillHandler.getTotalDemonWill(type, player);
            if (type != EnumDemonWillType.DEFAULT && amount > highestAmount) {
                highestType = type;
                highestAmount = amount;
            }
        }

        return highestType;
    }

    private static double getTotalWillOfType(Player player, EnumDemonWillType type) {
        return PlayerDemonWillHandler.getTotalDemonWill(type, player);
    }

    private static void drainWillFromPlayer(Player player, EnumDemonWillType type, double amount) {
        PlayerDemonWillHandler.consumeDemonWill(type, player, amount);
    }

    // IDemonWillWeapon implementation
    @Override
    public List<ItemStack> getRandomDemonWillDrop(LivingEntity killedEntity, LivingEntity attackingEntity,
                                                   ItemStack stack, int looting) {
        // Sentient Bow doesn't drop will items directly
        return new ArrayList<>();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint based on will level
        return super.isFoil(stack) || (stack.hasTag() && getCurrentType(stack) != EnumDemonWillType.DEFAULT);
    }
}
