package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusDataComponents;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

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

    public ItemBloodInfusedSpellbook() {
        super(5); // 5 base spell slots
    }

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
     * Calculate slots for a given tier
     */
    private static int calculateSlots(int tier) {
        int baseSlots = 5;
        if (tier >= 1 && tier <= 3) {
            return baseSlots + tier;
        } else if (tier >= 4) {
            return baseSlots + 3;
        }
        return baseSlots;
    }

    /**
     * Get max spell slots based on infusion tier
     */
    public static int getMaxSpellSlots(ItemStack stack) {
        return calculateSlots(getInfusionTier(stack));
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
     * Get the infusion tier of this spellbook using data components
     */
    public static int getInfusionTier(ItemStack stack) {
        return stack.getOrDefault(AnimusDataComponents.INFUSION_TIER.get(), 0);
    }

    /**
     * Set the infusion tier of this spellbook using data components
     * Also updates the SpellContainer's max spell count to match
     */
    public static void setInfusionTier(ItemStack stack, int tier) {
        tier = Math.max(0, Math.min(6, tier));
        stack.set(AnimusDataComponents.INFUSION_TIER.get(), tier);
        updateSpellContainerSlots(stack, tier);
    }

    /**
     * Update the SpellContainer's max spell count based on infusion tier
     */
    public static void updateSpellContainerSlots(ItemStack stack, int tier) {
        int newSlotCount = calculateSlots(tier);
        ISpellContainer container = ISpellContainer.getOrCreate(stack);
        if (container.getMaxSpellCount() != newSlotCount) {
            ISpellContainerMutable mutable = container.mutableCopy();
            mutable.setMaxSpellCount(newSlotCount);
            ISpellContainer.set(stack, mutable.toImmutable());
        }
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        int tier = getInfusionTier(stack);

        if (tier > 0) {
            tooltip.add(Component.literal(""));
            // Show current tier with max tier for reference
            tooltip.add(Component.literal("Blood Infusion: Tier " + tier + "/6")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Current Bonuses:")
                .withStyle(ChatFormatting.GOLD));

            // Show current bonuses
            tooltip.add(Component.literal("  Spell Slots: " + getMaxSpellSlots(stack))
                .withStyle(ChatFormatting.GRAY));

            double costReduction = getLPCostReduction(stack);
            if (costReduction > 0) {
                tooltip.add(Component.literal("  LP Cost Reduction: -" + (int)(costReduction * 100) + "%")
                    .withStyle(ChatFormatting.GRAY));
            }

            double lifesteal = getLifesteal(stack);
            if (lifesteal > 0) {
                tooltip.add(Component.literal("  Spell Lifesteal: " + (int)(lifesteal * 100) + "%")
                    .withStyle(ChatFormatting.GREEN));
            }

            // Show next tier info if upgradeable
            if (canUpgrade(stack)) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Next Tier Bonus:")
                    .withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal("  " + getNextTierBonus(tier + 1))
                    .withStyle(ChatFormatting.GRAY));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Upgrade Requirements:")
                    .withStyle(ChatFormatting.DARK_RED));
                tooltip.add(Component.literal("  " + getOrbNameForTier(tier + 1) + " or higher")
                    .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("  " + String.format("%,d LP in Blood Altar", getUpgradeCost(stack)))
                    .withStyle(ChatFormatting.GRAY));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Right-click Blood Altar to upgrade")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            } else {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Maximum tier reached!")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            }
        } else {
            // Tier 0 - this shouldn't normally happen since altar recipe outputs tier 1
            // But handle it gracefully in case of commands/creative
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Not yet infused")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Tier 1 Bonus:")
                .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("  +1 Spell Slot (6 total)")
                .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("To Infuse:")
                .withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("  Place in Blood Altar with " + String.format("%,d LP", AnimusConfig.ironsSpells.bloodSpellbookTier1LP.get()))
                .withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * Get the bonus description for a given tier
     */
    private static String getNextTierBonus(int tier) {
        return switch (tier) {
            case 1 -> "+1 Spell Slot (6 total)";
            case 2 -> "+2 Spell Slots (7 total)";
            case 3 -> "+3 Spell Slots (8 total)";
            case 4 -> "-10% LP Cost Reduction";
            case 5 -> "-20% LP Cost Reduction";
            case 6 -> "+5% Spell Lifesteal";
            default -> "Unknown bonus";
        };
    }

    /**
     * Get the required orb name for a given tier
     */
    private static String getOrbNameForTier(int tier) {
        return switch (tier) {
            case 1 -> "Weak Blood Orb";
            case 2 -> "Apprentice Blood Orb";
            case 3 -> "Magician's Blood Orb";
            case 4 -> "Master Blood Orb";
            case 5 -> "Archmage's Blood Orb";
            case 6 -> "Transcendent Blood Orb";
            default -> "Blood Orb";
        };
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint if infused
        return getInfusionTier(stack) > 0;
    }
}
