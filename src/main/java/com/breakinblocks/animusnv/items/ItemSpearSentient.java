package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.entities.EntityThrownSpear;
import com.breakinblocks.animusnv.util.SpiritusTypeHelper;
import com.breakinblocks.animusnv.util.WillWeaponStats;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.will.ISpiritus;

import java.util.List;

/**
 * Sentient Spear - A demon-will powered javelin
 * Scales with Spiritus like the Sentient Sword
 * Applies AOE damage with sentient effects when thrown
 */
public class ItemSpearSentient extends ItemSpear {
    // Damage scaling by will type (differs from bow, kept here)
    public static final double[] corrosiveDamageAdded = new double[]{1.0, 1.5, 2.5, 3.5, 5.0};
    public static final double[] destructiveDamageAdded = new double[]{1.5, 2.5, 3.5, 5.0, 7.0};
    public static final double[] vengefulDamageAdded = new double[]{0.5, 1.0, 2.0, 3.0, 4.0};
    public static final double[] steadfastDamageAdded = new double[]{0.5, 1.0, 2.0, 3.0, 4.0};

    // Attack speed modifiers (spear-specific)
    public static final double[] vengefulAttackSpeed = new double[]{-2.0, -1.8, -1.6, -1.4, -1.2};
    public static final double[] destructiveAttackSpeed = new double[]{-2.6, -2.7, -2.8, -2.9, -3.0};

    // Movement speed for vengeful (spear-specific)
    public static final double[] movementSpeed = new double[]{0.05, 0.1, 0.15, 0.2, 0.3};

    // Effect durations - poisonTime differs from bow, so kept here
    public static final int[] poisonTime = new int[]{50, 80, 120, 160, 200};
    public static final int[] absorptionTime = new int[]{300, 400, 500, 600, 800};

    // Soul drain per melee swing (spear-specific)
    public static final double[] soulDrainPerSwing = new double[]{0.05, 0.1, 0.2, 0.4, 0.75};

    public ItemSpearSentient() {
        super(Tiers.DIAMOND);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(com.breakinblocks.animusnv.Constants.Localizations.Tooltips.SPEAR_SENTIENT_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        SpiritusType type = getCurrentType(stack);
        String displayType = type == SpiritusType.RAW ? "raw" : type.name().toLowerCase();
        tooltip.add(Component.translatable("tooltip.animusnv.spear_sentient.will_type", displayType)
            .withStyle(ChatFormatting.AQUA));

        // Note: In 1.21, TooltipContext doesn't provide Level access easily on client
        // We show base stats instead of player-specific info in tooltips
        tooltip.add(Component.translatable(com.breakinblocks.animusnv.Constants.Localizations.Tooltips.SPEAR_SENTIENT_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(com.breakinblocks.animusnv.Constants.Localizations.Tooltips.SPEAR_SENTIENT_AOE)
            .withStyle(ChatFormatting.YELLOW));
    }

    public static int getLevel(ItemStack stack, double soulsRemaining) {
        return WillWeaponStats.getLevel(soulsRemaining);
    }

    public static double getDamageAdded(SpiritusType type, int level) {
        level = Math.min(level, 4);
        return switch (type) {
            case NIHILUM -> destructiveDamageAdded[level];
            case VINDICTA -> vengefulDamageAdded[level];
            case INVICTUS -> steadfastDamageAdded[level];
            default -> corrosiveDamageAdded[level];
        };
    }

    public static double getAttackSpeed(SpiritusType type, int level) {
        level = Math.min(level, 4);
        return switch (type) {
            case NIHILUM -> destructiveAttackSpeed[level];
            case VINDICTA -> vengefulAttackSpeed[level];
            default -> -2.4;
        };
    }

    public static void applyEffectToEntity(SpiritusType type, int level, LivingEntity target, LivingEntity attacker) {
        level = Math.min(level, 4);

        switch (type) {
            case RUINA:
            case RAW:
                // Apply wither effect
                if (poisonTime[level] > 0) {
                    target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WITHER,
                        poisonTime[level],
                        WillWeaponStats.POISON_LEVEL[level]
                    ));
                }
                break;

            case INVICTUS:
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

            case NIHILUM:
            case VINDICTA:
                // No special effects for these types
                break;
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            Level level = player.level();
            SpiritusType type = getCurrentType(stack);
            double soulsRemaining = getTotalSpiritusOfType(player, type);
            int willLevel = getLevel(stack, soulsRemaining);

                applyEffectToEntity(type, willLevel, target, attacker);

            // Will drops handled by getRandomSpiritusDrop()
            if (soulsRemaining >= 16.0) {
                drainSpiritusFromPlayer(player, type, soulDrainPerSwing[Math.min(willLevel, 4)]);
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int useDuration = this.getUseDuration(stack, entity) - timeLeft;
            if (useDuration >= 10) {
                int riptide = getRiptideLevel(stack, level);
                if (riptide <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                        stack.hurtAndBreak(1, serverLevel, player, (item) -> {});
                        if (riptide == 0) {
                            // Get will type and level before spawning
                            SpiritusType type = getCurrentType(stack);
                            double soulsRemaining = getTotalSpiritusOfType(player, type);
                            int willLevel = getLevel(stack, soulsRemaining);

                            // Spawn sentient spear entity
                            EntityThrownSpear thrownSpear = new EntityThrownSpear(level, player, stack);
                            thrownSpear.setVariant("sentient");
                            thrownSpear.setWillType(type);
                            thrownSpear.setWillLevel(willLevel);
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

                            if (soulsRemaining >= 16.0) {
                                // Double will drain for throwing vs melee
                                drainSpiritusFromPlayer(player, type, soulDrainPerSwing[Math.min(willLevel, 4)] * 2.0);
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

    private int getRiptideLevel(ItemStack stack, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.RIPTIDE));
        }
        return 0;
    }

    public SpiritusType getCurrentType(ItemStack stack) {
        return SpiritusTypeHelper.getCurrentType(stack);
    }

    public void setCurrentType(ItemStack stack, SpiritusType type) {
        SpiritusTypeHelper.setCurrentType(stack, type);
    }

    public List<ItemStack> getRandomSpiritusDrop(LivingEntity killedEntity, LivingEntity attackingEntity, ItemStack stack, int looting) {
        List<ItemStack> soulList = new java.util.ArrayList<>();

        if (killedEntity.getCommandSenderWorld().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && !(killedEntity instanceof net.minecraft.world.entity.monster.Enemy)) {
            return soulList;
        }

        double willModifier = killedEntity instanceof net.minecraft.world.entity.monster.Slime ? 0.67 : 1;

        SpiritusType type = this.getCurrentType(stack);
        ISpiritus soul = switch (type) {
            case RUINA -> ((ISpiritus) NVItems.MONSTER_SOUL_RUINA.get());
            case NIHILUM -> ((ISpiritus) NVItems.MONSTER_SOUL_NIHILUM.get());
            case INVICTUS -> ((ISpiritus) NVItems.MONSTER_SOUL_INVICTUS.get());
            case VINDICTA -> ((ISpiritus) NVItems.MONSTER_SOUL_VINDICTA.get());
            default -> ((ISpiritus) NVItems.MONSTER_SOUL_RAW.get());
        };

        double soulsRemaining = 0;
        if (attackingEntity instanceof Player player) {
            soulsRemaining = getTotalSpiritusOfType(player, type);
        }
        int willLevel = Math.min(getLevel(stack, soulsRemaining), 4);

        for (int i = 0; i <= looting; i++) {
            if (i == 0 || attackingEntity.getCommandSenderWorld().random.nextDouble() < 0.4) {
                double dropAmount = willModifier * (WillWeaponStats.SOUL_DROP[willLevel] * attackingEntity.getCommandSenderWorld().random.nextDouble()
                    + WillWeaponStats.STATIC_DROP[willLevel]) * killedEntity.getMaxHealth() / 20.0;
                ItemStack soulStack = soul.createSpiritus(dropAmount);
                soulList.add(soulStack);
            }
        }

        return soulList;
    }

    public SpiritusType getActiveSpiritusType(ItemStack stack, LivingEntity player, Entity target) {
        return getCurrentType(stack);
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
}
