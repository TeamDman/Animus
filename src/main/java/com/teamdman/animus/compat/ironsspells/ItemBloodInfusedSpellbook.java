package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Blood-Infused Spellbook - Enhanced spellbook powered by Blood Magic
 *
 * Infusion Tiers and Bonuses:
 * - Tier 1-3: +1/+2/+3 spell slots
 * - Tier 4-5: -10%/-20% LP cost reduction
 * - Tier 6: Spells gain 5% lifesteal
 *
 * Infused at Blood Altar using Blood Orbs
 */
public class ItemBloodInfusedSpellbook extends SpellBook {

    // NBT keys
    private static final String INFUSION_TIER_KEY = "InfusionTier";

    public ItemBloodInfusedSpellbook() {
        super(5, SpellRarity.COMMON); // 5 base slots, COMMON rarity
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        int tier = getInfusionTier(stack);
        return switch (tier) {
            case 0 -> Rarity.COMMON;
            case 1, 2 -> Rarity.UNCOMMON;
            case 3, 4 -> Rarity.RARE;
            case 5, 6 -> Rarity.EPIC;
            default -> Rarity.COMMON;
        };
    }

    @Override
    public int getMaxSpellSlots() {
        // Base spellbook has dynamic slots based on rarity
        // We'll override this in getMaxSpellSlots(ItemStack)
        return 10; // Maximum
    }

    /**
     * Get max spell slots based on infusion tier
     */
    public static int getMaxSpellSlots(ItemStack stack) {
        int tier = getInfusionTier(stack);

        // Base slots from parent class would be 5 for COMMON
        int baseSlots = 5;

        // Tier 1-3: +1/+2/+3 slots
        if (tier >= 1 && tier <= 3) {
            return baseSlots + tier;
        }
        // Tier 4+: +3 slots (same as tier 3)
        else if (tier >= 4) {
            return baseSlots + 3;
        }

        return baseSlots;
    }

    /**
     * Get LP cost reduction percentage (0.0 to 1.0)
     */
    public static double getLPCostReduction(ItemStack stack) {
        int tier = getInfusionTier(stack);

        return switch (tier) {
            case 4 -> 0.10; // 10% reduction
            case 5 -> 0.20; // 20% reduction
            case 6 -> 0.20; // 20% reduction (lifesteal is separate bonus)
            default -> 0.0;
        };
    }

    /**
     * Get lifesteal percentage (0.0 to 1.0)
     */
    public static double getLifesteal(ItemStack stack) {
        int tier = getInfusionTier(stack);
        return tier >= 6 ? 0.05 : 0.0; // 5% lifesteal at tier 6
    }

    /**
     * Get the infusion tier of this spellbook
     */
    public static int getInfusionTier(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt(INFUSION_TIER_KEY);
        }
        return 0;
    }

    /**
     * Set the infusion tier of this spellbook
     */
    public static void setInfusionTier(ItemStack stack, int tier) {
        stack.getOrCreateTag().putInt(INFUSION_TIER_KEY, Math.max(0, Math.min(6, tier)));
    }

    /**
     * Check if this spellbook can be upgraded to the next tier
     */
    public static boolean canUpgrade(ItemStack stack) {
        return getInfusionTier(stack) < 6;
    }

    /**
     * Get the LP cost to upgrade to the next tier
     */
    public static int getUpgradeCost(ItemStack stack) {
        int currentTier = getInfusionTier(stack);
        int nextTier = currentTier + 1;

        return switch (nextTier) {
            case 1 -> AnimusConfig.ironsSpells.bloodSpellbookTier1LP.get();
            case 2 -> AnimusConfig.ironsSpells.bloodSpellbookTier2LP.get();
            case 3 -> AnimusConfig.ironsSpells.bloodSpellbookTier3LP.get();
            case 4 -> AnimusConfig.ironsSpells.bloodSpellbookTier4LP.get();
            case 5 -> AnimusConfig.ironsSpells.bloodSpellbookTier5LP.get();
            case 6 -> AnimusConfig.ironsSpells.bloodSpellbookTier6LP.get();
            default -> 0;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        int tier = getInfusionTier(stack);

        if (tier > 0) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Blood Infusion: Tier " + tier)
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

            // Show current bonuses
            tooltip.add(Component.literal("Spell Slots: " + getMaxSpellSlots(stack))
                .withStyle(ChatFormatting.GRAY));

            double costReduction = getLPCostReduction(stack);
            if (costReduction > 0) {
                tooltip.add(Component.literal("LP Cost: -" + (int)(costReduction * 100) + "%")
                    .withStyle(ChatFormatting.GOLD));
            }

            double lifesteal = getLifesteal(stack);
            if (lifesteal > 0) {
                tooltip.add(Component.literal("Lifesteal: " + (int)(lifesteal * 100) + "%")
                    .withStyle(ChatFormatting.GREEN));
            }

            // Show next tier info if upgradeable
            if (canUpgrade(stack)) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Right-click on Blood Altar to upgrade")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
                tooltip.add(Component.literal("Next tier cost: " + getUpgradeCost(stack) + " LP")
                    .withStyle(ChatFormatting.DARK_RED));
            }
        } else {
            // Not infused yet
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Right-click on Blood Altar to infuse")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            tooltip.add(Component.literal("Requires: Blood Orb + " + AnimusConfig.ironsSpells.bloodSpellbookTier1LP.get() + " LP")
                .withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint if infused
        return getInfusionTier(stack) > 0;
    }
}
