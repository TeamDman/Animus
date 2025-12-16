package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Blood-Infused Spellbook - Enhanced spellbook powered by Blood Magic
 *
 * Infusion Tiers and Bonuses:
 * - All tiers: +50 max mana per tier (up to +300)
 * - Tier 1: 6 spell slots
 * - Tier 2: 7 spell slots
 * - Tier 3: 8 spell slots
 * - Tier 4: 10 spell slots, -10% LP cost reduction
 * - Tier 5: 11 spell slots, -20% LP cost reduction
 * - Tier 6: 12 spell slots, -20% LP cost reduction, +5% lifesteal
 *
 * Infused at Blood Altar using Blood Orbs
 */
public class ItemBloodInfusedSpellbook extends SpellBook {

    // NBT keys
    private static final String INFUSION_TIER_KEY = "InfusionTier";

    // UUID for the mana modifier (consistent across all instances)
    private static final UUID MANA_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

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
        return 12; // Maximum at tier 6
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
     * Get max mana bonus based on infusion tier (+50 per tier)
     */
    public static int getMaxManaBonus(ItemStack stack) {
        int tier = getInfusionTier(stack);
        return tier * 50; // +50 per tier, up to +300 at tier 6
    }

    /**
     * Get max mana bonus for a specific tier
     */
    public static int getMaxManaBonusForTier(int tier) {
        return tier * 50;
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
     * Also updates the SpellContainer's max spell count to match the new tier
     */
    public static void setInfusionTier(ItemStack stack, int tier) {
        tier = Math.max(0, Math.min(6, tier));
        stack.getOrCreateTag().putInt(INFUSION_TIER_KEY, tier);

        // Update the SpellContainer's max spell count to match the new tier
        updateSpellContainerSlots(stack, tier);
    }

    /**
     * Update the SpellContainer's maxSpellCount to match the infusion tier
     */
    public static void updateSpellContainerSlots(ItemStack stack, int tier) {
        int newSlotCount = calculateSlots(tier);

        // Get or create the spell container and update its max count
        ISpellContainer container = ISpellContainer.getOrCreate(stack);
        if (container.getMaxSpellCount() != newSlotCount) {
            container.setMaxSpellCount(newSlotCount);
            container.save(stack);
        }
    }

    /**
     * Calculate slots for a given tier
     * Scaling: 5 → 6 → 7 → 8 → 10 → 11 → 12
     */
    private static int calculateSlots(int tier) {
        return switch (tier) {
            case 0 -> 5;
            case 1 -> 6;
            case 2 -> 7;
            case 3 -> 8;
            case 4 -> 10;
            case 5 -> 11;
            case 6 -> 12;
            default -> 5;
        };
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

        tooltip.add(Component.literal(""));

        if (tier > 0) {
            tooltip.add(Component.literal("Blood Infusion: Tier " + tier + "/6")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Current Bonuses:")
                .withStyle(ChatFormatting.GOLD));

            // Show current bonuses
            tooltip.add(Component.literal("  Spell Slots: " + getMaxSpellSlots(stack))
                .withStyle(ChatFormatting.GRAY));

            int manaBonus = getMaxManaBonus(stack);
            if (manaBonus > 0) {
                tooltip.add(Component.literal("  Max Mana: +" + manaBonus)
                    .withStyle(ChatFormatting.AQUA));
            }

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
            tooltip.add(Component.literal("Not yet infused")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Tier 1 Bonus:")
                .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("  6 Spell Slots, +50 Max Mana")
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
        int slots = calculateSlots(tier);
        int mana = getMaxManaBonusForTier(tier);
        String base = slots + " Spell Slots, +" + mana + " Max Mana";
        return switch (tier) {
            case 1, 2, 3 -> base;
            case 4 -> base + ", -10% LP Cost";
            case 5 -> base + ", -20% LP Cost";
            case 6 -> base + ", -20% LP Cost, +5% Lifesteal";
            default -> "Unknown bonus";
        };
    }

    /**
     * Get the name of the Blood Orb required for a given tier
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

    /**
     * Called every tick when the spellbook is equipped in a curios slot
     * Applies the max mana modifier based on infusion tier
     */
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        if (slotContext.entity().level().isClientSide) {
            return;
        }

        int manaBonus = getMaxManaBonus(stack);
        AttributeInstance manaAttribute = slotContext.entity().getAttribute(AttributeRegistry.MAX_MANA.get());

        if (manaAttribute == null) {
            return;
        }

        AttributeModifier existingModifier = manaAttribute.getModifier(MANA_MODIFIER_UUID);

        if (manaBonus <= 0) {
            // Remove modifier if tier 0
            if (existingModifier != null) {
                manaAttribute.removeModifier(MANA_MODIFIER_UUID);
            }
        } else {
            // Add or update modifier
            if (existingModifier == null || existingModifier.getAmount() != manaBonus) {
                if (existingModifier != null) {
                    manaAttribute.removeModifier(MANA_MODIFIER_UUID);
                }
                manaAttribute.addPermanentModifier(new AttributeModifier(
                    MANA_MODIFIER_UUID,
                    "Blood Spellbook Mana Bonus",
                    manaBonus,
                    AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    /**
     * Called when the spellbook is unequipped from a curios slot
     * Removes the max mana modifier
     */
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (slotContext.entity().level().isClientSide) {
            return;
        }

        // Only remove if we're not just swapping to another blood spellbook
        if (newStack.getItem() instanceof ItemBloodInfusedSpellbook) {
            return;
        }

        AttributeInstance manaAttribute = slotContext.entity().getAttribute(AttributeRegistry.MAX_MANA.get());
        if (manaAttribute != null) {
            manaAttribute.removeModifier(MANA_MODIFIER_UUID);
        }
    }
}
