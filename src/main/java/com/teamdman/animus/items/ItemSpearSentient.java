package com.teamdman.animus.items;

import com.teamdman.animus.entities.EntityThrownSpear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.api.compat.IDemonWillWeapon;
import wayoftime.bloodmagic.common.item.soul.ItemSoulGem;
import wayoftime.bloodmagic.potion.BloodMagicPotions;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Sentient Spear - A demon-will powered javelin
 * Scales with demon will like the Sentient Sword
 * Applies AOE damage with sentient effects when thrown
 */
public class ItemSpearSentient extends ItemSpear implements IDemonWillWeapon {
    // Soul brackets for level progression (simplified from sword's 7 levels to 5)
    public static final double[] soulBracket = new double[]{16, 60, 200, 400, 1000};

    // Damage scaling by will type
    public static final double[] corrosiveDamageAdded = new double[]{1.0, 1.5, 2.5, 3.5, 5.0};
    public static final double[] destructiveDamageAdded = new double[]{1.5, 2.5, 3.5, 5.0, 7.0};
    public static final double[] vengefulDamageAdded = new double[]{0.5, 1.0, 2.0, 3.0, 4.0};
    public static final double[] steadfastDamageAdded = new double[]{0.5, 1.0, 2.0, 3.0, 4.0};

    // Attack speed modifiers
    public static final double[] vengefulAttackSpeed = new double[]{-2.0, -1.8, -1.6, -1.4, -1.2};
    public static final double[] destructiveAttackSpeed = new double[]{-2.6, -2.7, -2.8, -2.9, -3.0};

    // Movement speed for vengeful
    public static final double[] movementSpeed = new double[]{0.05, 0.1, 0.15, 0.2, 0.3};

    // Effect durations and levels
    public static final int[] poisonTime = new int[]{50, 80, 120, 160, 200};
    public static final int[] poisonLevel = new int[]{0, 0, 1, 1, 2};
    public static final int[] absorptionTime = new int[]{300, 400, 500, 600, 800};

    // Soul drain and drop
    public static final double[] soulDrainPerSwing = new double[]{0.05, 0.1, 0.2, 0.4, 0.75};
    public static final double[] soulDrop = new double[]{2.0, 4.0, 7.0, 10.0, 15.0};
    public static final double[] staticDrop = new double[]{1.0, 1.0, 2.0, 3.0, 4.0};

    public ItemSpearSentient() {
        super(Tiers.DIAMOND);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(com.teamdman.animus.Constants.Localizations.Tooltips.SPEAR_SENTIENT_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        EnumDemonWillType type = getCurrentType(stack);
        String displayType = type == EnumDemonWillType.DEFAULT ? "raw" : type.name().toLowerCase();
        tooltip.add(Component.translatable("tooltip.animus.spear_sentient.will_type", displayType)
            .withStyle(ChatFormatting.AQUA));

        if (level != null && level.isClientSide) {
            Player player = level.getNearestPlayer(0, 0, 0, Double.MAX_VALUE, false);
            if (player != null) {
                double soulsRemaining = getTotalWillOfType(player, type);
                int willLevel = getLevel(stack, soulsRemaining);
                tooltip.add(Component.translatable("tooltip.animus.spear_sentient.level", willLevel, (int)soulsRemaining)
                    .withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("tooltip.animus.spear_sentient.damage_bonus", String.format("%.1f", getDamageAdded(type, willLevel)))
                    .withStyle(ChatFormatting.RED));
            }
        }

        tooltip.add(Component.translatable(com.teamdman.animus.Constants.Localizations.Tooltips.SPEAR_SENTIENT_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(com.teamdman.animus.Constants.Localizations.Tooltips.SPEAR_SENTIENT_AOE)
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
            case DESTRUCTIVE -> destructiveDamageAdded[level];
            case VENGEFUL -> vengefulDamageAdded[level];
            case STEADFAST -> steadfastDamageAdded[level];
            default -> corrosiveDamageAdded[level];
        };
    }

    public static double getAttackSpeed(EnumDemonWillType type, int level) {
        level = Math.min(level, 4);
        return switch (type) {
            case DESTRUCTIVE -> destructiveAttackSpeed[level];
            case VENGEFUL -> vengefulAttackSpeed[level];
            default -> -2.4;
        };
    }

    public static void applyEffectToEntity(EnumDemonWillType type, int level, LivingEntity target, LivingEntity attacker) {
        level = Math.min(level, 4);

        switch (type) {
            case CORROSIVE:
            case DEFAULT:
                // Apply wither effect
                if (poisonTime[level] > 0) {
                    target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WITHER,
                        poisonTime[level],
                        poisonLevel[level]
                    ));
                }
                break;

            case STEADFAST:
                // Apply absorption to attacker instead of target
                if (attacker != null && absorptionTime[level] > 0) {
                    float currentAbsorption = attacker.getAbsorptionAmount();
                    float maxHealth = attacker.getMaxHealth();
                    float newAbsorption = Math.min(10.0f, currentAbsorption + (maxHealth * 0.05f));
                    attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.ABSORPTION,
                        absorptionTime[level],
                        127
                    ));
                    attacker.setAbsorptionAmount(newAbsorption);
                }
                break;

            case DESTRUCTIVE:
            case VENGEFUL:
                // No special effects for these types
                break;
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            Level level = player.level();
            EnumDemonWillType type = getCurrentType(stack);
            double soulsRemaining = getTotalWillOfType(player, type);
            int willLevel = getLevel(stack, soulsRemaining);

            // Apply sentient effects
            applyEffectToEntity(type, willLevel, target, attacker);

            // Apply Soul Snare effect (5 seconds, amplifier 1) for guaranteed will drops
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                BloodMagicPotions.SOUL_SNARE.get(), 100, 1));

            // Drain will from soul network
            if (soulsRemaining >= 16.0) {
                drainWillFromPlayer(player, type, soulDrainPerSwing[Math.min(willLevel, 4)]);
            }
        }

        return super.hurtEnemy(stack, target, attacker);
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
                            // Spawn sentient spear entity
                            EntityThrownSpear thrownSpear = new EntityThrownSpear(level, player, stack);
                            // Mark it as sentient for special handling
                            thrownSpear.setVariant("sentient");
                            thrownSpear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                            if (player.getAbilities().instabuild) {
                                thrownSpear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            level.addFreshEntity(thrownSpear);
                            level.playSound(null, thrownSpear, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                            if (!player.getAbilities().instabuild) {
                                player.getInventory().removeItem(stack);
                            }

                            // Drain will on throw from player's soul network
                            EnumDemonWillType type = getCurrentType(stack);
                            double soulsRemaining = getTotalWillOfType(player, type);
                            if (soulsRemaining >= 16.0) {
                                int willLevel = getLevel(stack, soulsRemaining);
                                // Double drain for throwing
                                drainWillFromPlayer(player, type, soulDrainPerSwing[Math.min(willLevel, 4)] * 2.0);
                            }
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    if (riptide > 0) {
                        // Riptide handling (same as parent)
                        super.releaseUsing(stack, level, entity, timeLeft);
                    }
                }
            }
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create(super.getAttributeModifiers(slot, stack));

        if (slot == EquipmentSlot.MAINHAND) {
            Player player = null; // We'll try to get this from context if possible

            // Try to find a player to get their will
            if (stack.getTag() != null) {
                // Get will type
                EnumDemonWillType type = getCurrentType(stack);

                // For now, assume level 0 if we can't get player context
                // The tooltip will show the actual values
                int level = 0;

                double damage = getDamageAdded(type, level);
                double attackSpeed = getAttackSpeed(type, level);

                multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damage, AttributeModifier.Operation.ADDITION));
                multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                    BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));

                if (type == EnumDemonWillType.VENGEFUL) {
                    multimap.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                        java.util.UUID.fromString("4218052-0-0-0-0"), "Weapon modifier",
                        movementSpeed[level], AttributeModifier.Operation.ADDITION));
                }
            }
        }

        return multimap;
    }

    public EnumDemonWillType getCurrentType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("demonWillType")) {
            return EnumDemonWillType.valueOf(stack.getTag().getString("demonWillType"));
        }
        return EnumDemonWillType.DEFAULT;
    }

    public void setCurrentType(ItemStack stack, EnumDemonWillType type) {
        stack.getOrCreateTag().putString("demonWillType", type.toString());
    }

    public List<ItemStack> getRandomDemonWillDrop(LivingEntity killedEntity, LivingEntity attackingEntity, ItemStack stack, int tier) {
        // Sentient Spear doesn't drop will items, it drains will from the aura
        return new java.util.ArrayList<>();
    }

    public EnumDemonWillType getActiveDemonWillType(ItemStack stack, LivingEntity player, Entity target) {
        return getCurrentType(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.world.item.enchantment.Enchantment enchantment) {
        // Sentient spear has built-in loyalty - don't allow loyalty enchantment
        if (enchantment == net.minecraft.world.item.enchantment.Enchantments.LOYALTY) {
            return false;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
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
        // Get will amounts from the player's soul network
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

    /**
     * Gets the total amount of will the player has of a specific type from their soul network
     */
    private static double getTotalWillOfType(Player player, EnumDemonWillType type) {
        return PlayerDemonWillHandler.getTotalDemonWill(type, player);
    }

    /**
     * Drains will from the player's soul network
     */
    private static void drainWillFromPlayer(Player player, EnumDemonWillType type, double amount) {
        PlayerDemonWillHandler.consumeDemonWill(type, player, amount);
    }
}
