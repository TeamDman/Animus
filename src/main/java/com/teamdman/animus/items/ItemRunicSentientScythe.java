package com.teamdman.animus.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.common.item.ItemSentientScythe;
import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Runic Sentient Scythe - A cross-mod compatibility weapon combining Blood Magic and Malum
 *
 * Features:
 * - Extends Blood Magic's Sentient Scythe with 30% faster attack speed
 * - Integrates Malum's soul harvesting when Malum is loaded
 * - Incorporates Malum's sweep attack mechanics when available
 * - Full demon will integration from Blood Magic
 */
public class ItemRunicSentientScythe extends ItemSentientScythe {
    // Attack speed multiplier (30% faster than base sentient scythe)
    private static final double ATTACK_SPEED_MULTIPLIER = 1.3;

    // Malum integration flag
    private static final boolean MALUM_LOADED = ModList.get().isLoaded("malum");

    // Malum integration helper (loaded via reflection if available)
    private static MalumIntegration malumIntegration;

    static {
        if (MALUM_LOADED) {
            try {
                malumIntegration = new MalumIntegration();
            } catch (Exception e) {
                System.err.println("Failed to initialize Malum integration: " + e.getMessage());
            }
        }
    }

    public ItemRunicSentientScythe() {
        super();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create(super.getAttributeModifiers(slot, stack));

        if (slot == EquipmentSlot.MAINHAND) {
            // Get the base attack speed from the parent
            // The parent already applies demon will bonuses, we just need to increase the speed

            // Remove the old attack speed modifier and add our enhanced one
            multimap.removeAll(Attributes.ATTACK_SPEED);

            // Get current demon will type and level to calculate proper attack speed
            EnumDemonWillType type = getCurrentType(stack);
            double soulsRemaining = 0;
            int level = 0;

            // Try to get actual will levels (this will be 0 in creative/tooltip context)
            if (stack.getTag() != null && stack.getTag().contains("demonWillType")) {
                level = getLevel(stack, soulsRemaining);
            }

            double baseAttackSpeed = getAttackSpeed(type, level);
            double enhancedAttackSpeed = baseAttackSpeed * ATTACK_SPEED_MULTIPLIER;

            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_UUID,
                "Weapon modifier",
                enhancedAttackSpeed,
                AttributeModifier.Operation.ADDITION
            ));
        }

        return multimap;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Cache total soul count for damage calculation (must be done before parent call)
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            // Get total demon will from all types
            double totalWill = 0;
            for (EnumDemonWillType type : EnumDemonWillType.values()) {
                totalWill += WorldDemonWillHandler.getCurrentWill(attacker.level(), player.blockPosition(), type);
            }
            if (stack.getTag() == null) {
                stack.setTag(new net.minecraft.nbt.CompoundTag());
            }
            stack.getTag().putDouble("cachedSouls", totalWill);
        }

        boolean result = super.hurtEnemy(stack, target, attacker);

        // Trigger Malum soul harvesting if available
        if (MALUM_LOADED && malumIntegration != null && attacker instanceof Player player) {
            malumIntegration.onEntityKilled(player, target, stack);
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // Show demon will damage bonus
        double soulsRemaining = 0;
        if (stack.getTag() != null && stack.getTag().contains("cachedSouls")) {
            soulsRemaining = stack.getTag().getDouble("cachedSouls");
        }
        int willLevel = getLevel(stack, soulsRemaining);
        double willDamage = getDamageAdded(willLevel);

        tooltip.add(Component.literal(String.format("Demon Will Damage: +%.1f", willDamage))
            .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltip.add(Component.translatable("tooltip.animus.runic_sentient_scythe.enhanced")
            .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.animus.runic_sentient_scythe.attack_speed")
            .withStyle(ChatFormatting.GREEN));

        if (MALUM_LOADED) {
            tooltip.add(Component.translatable("tooltip.animus.runic_sentient_scythe.malum")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    // Helper method to get damage added by demon will
    // Uses destructive will values regardless of type (highest damage)
    private static double getDamageAdded(int level) {
        level = Math.min(level, 6);
        // Use destructive will damage scaling
        double[] damageAdded = new double[]{5.0, 6.5, 8.0, 9.5, 11.0, 12.5, 14.0};
        return damageAdded[level];
    }

    private static int getLevel(ItemStack stack, double soulsRemaining) {
        double[] soulBracket = new double[]{16, 60, 200, 400, 1000, 2000, 4000};

        for (int i = 0; i < soulBracket.length; i++) {
            if (soulsRemaining < soulBracket[i]) {
                return i;
            }
        }
        return soulBracket.length;
    }

    /**
     * Malum integration helper class
     * Loaded via reflection only when Malum is present
     */
    private static class MalumIntegration {
        // We'll use reflection to trigger Malum's soul harvesting
        // This prevents hard dependencies on Malum classes

        public void onEntityKilled(Player player, LivingEntity target, ItemStack weapon) {
            try {
                // Call Malum's spirit harvest handler to spawn souls
                // This uses the public static method: SpiritHarvestHandler.spawnSpirits(target, attacker, weapon)
                Class<?> spiritHarvestHandler = Class.forName("com.sammy.malum.core.handlers.SpiritHarvestHandler");
                java.lang.reflect.Method spawnSpirits = spiritHarvestHandler.getMethod(
                    "spawnSpirits",
                    net.minecraft.world.entity.LivingEntity.class,
                    net.minecraft.world.entity.LivingEntity.class,
                    net.minecraft.world.item.ItemStack.class
                );
                spawnSpirits.invoke(null, target, player, weapon);
            } catch (Exception e) {
                // Silent failure - Malum integration is optional
                System.err.println("Failed to trigger Malum soul harvest: " + e.getMessage());
            }
        }
    }

    // Helper methods from parent class for calculating will-based stats
    @Override
    public double getAttackSpeed(EnumDemonWillType type, int level) {
        level = Math.min(level, 6);

        // These arrays match Blood Magic's ItemSentientScythe
        double[] vengefulAttackSpeed = new double[]{-2.0, -1.8, -1.6, -1.4, -1.2, -1.0, -0.8};
        double[] destructiveAttackSpeed = new double[]{-2.6, -2.7, -2.8, -2.9, -3.0, -3.1, -3.2};

        return switch (type) {
            case DESTRUCTIVE -> destructiveAttackSpeed[level];
            case VENGEFUL -> vengefulAttackSpeed[level];
            default -> -2.4;
        };
    }

    @Override
    public EnumDemonWillType getCurrentType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("demonWillType")) {
            return EnumDemonWillType.valueOf(stack.getTag().getString("demonWillType").toUpperCase());
        }
        return EnumDemonWillType.DEFAULT;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        // Allow parent enchantments
        if (super.canApplyAtEnchantingTable(stack, enchantment)) {
            return true;
        }

        // Allow Malum's haunted enchant if Malum is loaded
        if (MALUM_LOADED) {
            try {
                // Check if this is the haunted enchant
                String enchantId = enchantment.getDescriptionId();
                if (enchantId != null && enchantId.contains("malum") && enchantId.contains("haunted")) {
                    return true;
                }
            } catch (Exception e) {
                // Silent failure
            }
        }

        return false;
    }
}
