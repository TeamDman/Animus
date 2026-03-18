package com.teamdman.animus.items;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntitySentientArrow;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.will.PlayerDemonWillHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Sentient Bow - A demon-will powered bow
 * Consumes 1 will per shot instead of arrows
 * Attunes to the demon will type in the player's gems
 * Fires spectral-like arrows that vanish after impact
 * Drops demon will matching the bow's attuned type on kill
 */
public class ItemSentientBow extends BowItem {
    // Soul brackets for level progression (same as sentient spear)
    public static final double[] soulBracket = new double[]{16, 60, 200, 400, 1000};

    // Will drop scaling by level (same as sentient spear)
    public static final double[] soulDrop = new double[]{2.0, 4.0, 7.0, 10.0, 15.0};
    public static final double[] staticDrop = new double[]{1.0, 1.0, 2.0, 3.0, 4.0};

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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        EnumWillType type = getCurrentType(stack);
        String displayType = type == EnumWillType.DEFAULT ? "raw" : type.name().toLowerCase();
        tooltip.add(Component.translatable("tooltip.animus.sentient_bow.will_type", displayType)
            .withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_BOW_WILL_DROPS)
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

    public static double getDamageAdded(EnumWillType type, int level) {
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
        EnumWillType type = getCurrentType(stack);
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

        int useDuration = this.getUseDuration(stack, entity) - timeLeft;
        float power = getPowerForTime(useDuration);

        if (power < 0.1F) {
            return;
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
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
            int powerEnchant = stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.POWER));
            if (powerEnchant > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchant * 0.5D + 0.5D);
            }

            // Note: Punch enchantment knockback is handled by vanilla arrow damage mechanics

            // Apply flame enchantment
            int flameEnchant = stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FLAME));
            if (flameEnchant > 0) {
                arrow.setRemainingFireTicks(100);
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
            stack.hurtAndBreak(1, serverLevel, player, (item) -> {});
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        // Sentient bow doesn't need arrows
        return (stack) -> false;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    // Will type management using data components
    public EnumWillType getCurrentType(ItemStack stack) {
        String typeStr = stack.get(AnimusDataComponents.DEMON_WILL_TYPE.get());
        if (typeStr != null) {
            try {
                return EnumWillType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                return EnumWillType.DEFAULT;
            }
        }
        return EnumWillType.DEFAULT;
    }

    public void setCurrentType(ItemStack stack, EnumWillType type) {
        stack.set(AnimusDataComponents.DEMON_WILL_TYPE.get(), type.toString());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (entity instanceof Player player) {
            // Update the will type based on the player's inventory
            EnumWillType newType = findDemonWillType(player);
            if (newType != getCurrentType(stack)) {
                setCurrentType(stack, newType);
            }
        }
    }

    /**
     * Determines the demon will type based on will available from the player's soul network
     * Returns the type with the highest will amount
     */
    private static EnumWillType findDemonWillType(Player player) {
        EnumWillType highestType = EnumWillType.DEFAULT;
        double highestAmount = 0;

        for (EnumWillType type : EnumWillType.values()) {
            double amount = PlayerDemonWillHandler.getTotalDemonWill(type, player);
            if (type != EnumWillType.DEFAULT && amount > highestAmount) {
                highestType = type;
                highestAmount = amount;
            }
        }

        return highestType;
    }

    /**
     * Gets the total amount of will the player has of a specific type
     */
    private static double getTotalWillOfType(Player player, EnumWillType type) {
        return PlayerDemonWillHandler.getTotalDemonWill(type, player);
    }

    /**
     * Drains will from the player's inventory
     */
    private static void drainWillFromPlayer(Player player, EnumWillType type, double amount) {
        PlayerDemonWillHandler.consumeDemonWill(type, player, amount);
    }

    // IDemonWillWeapon-like methods
    public List<ItemStack> getRandomDemonWillDrop(LivingEntity killedEntity, LivingEntity attackingEntity,
                                                   ItemStack stack, int looting) {
        return new ArrayList<>();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint when attuned to non-default will type
        return super.isFoil(stack) || getCurrentType(stack) != EnumWillType.DEFAULT;
    }
}
