package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import net.minecraft.core.registries.BuiltInRegistries;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

/**
 * Blood-Infused Spellbook - Enhanced spellbook powered by NeoVitae
 *
 * Infusion Tiers and Bonuses:
 * - All tiers: +50 max mana per tier (up to +300)
 * - Tier 1: 6 spell slots
 * - Tier 2: 7 spell slots
 * - Tier 3: 8 spell slots
 * - Tier 4: 10 spell slots, -10% EV cost reduction
 * - Tier 5: 11 spell slots, -20% EV cost reduction
 * - Tier 6: 12 spell slots, -20% EV cost reduction, +5% lifesteal
 *
 * Infused at Ara Vitae using Orbs of Vitae
 */
public class ItemBloodInfusedSpellbook extends SpellBook {

    private static final ResourceLocation MANA_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "blood_spellbook_mana");

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
        return 12; // Maximum at tier 6
    }

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

    public static int getMaxSpellSlots(ItemStack stack) {
        return calculateSlots(getInfusionTier(stack));
    }

    public static double getEVCostReduction(ItemStack stack) {
        int tier = getInfusionTier(stack);

        return switch (tier) {
            case 4 -> 0.10; // 10% reduction
            case 5 -> 0.20; // 20% reduction
            case 6 -> 0.20; // 20% reduction (lifesteal is separate bonus)
            default -> 0.0;
        };
    }

    public static double getLifesteal(ItemStack stack) {
        int tier = getInfusionTier(stack);
        return tier >= 6 ? 0.05 : 0.0; // 5% lifesteal at tier 6
    }

    public static int getMaxManaBonus(ItemStack stack) {
        int tier = getInfusionTier(stack);
        return tier * 50; // +50 per tier, up to +300 at tier 6
    }

    public static int getMaxManaBonusForTier(int tier) {
        return tier * 50;
    }

    public static int getInfusionTier(ItemStack stack) {
        return stack.getOrDefault(AnimusDataComponents.INFUSION_TIER.get(), 0);
    }

    public static void setInfusionTier(ItemStack stack, int tier) {
        tier = Math.max(0, Math.min(6, tier));
        stack.set(AnimusDataComponents.INFUSION_TIER.get(), tier);
        updateSpellContainerSlots(stack, tier);
    }

    public static void updateSpellContainerSlots(ItemStack stack, int tier) {
        int newSlotCount = calculateSlots(tier);
        ISpellContainer container = ISpellContainer.getOrCreate(stack);
        if (container.getMaxSpellCount() != newSlotCount) {
            ISpellContainerMutable mutable = container.mutableCopy();
            mutable.setMaxSpellCount(newSlotCount);
            ISpellContainer.set(stack, mutable.toImmutable());
        }
    }

    public static boolean canUpgrade(ItemStack stack) {
        return getInfusionTier(stack) < 6;
    }

    public static int getUpgradeCost(ItemStack stack) {
        int currentTier = getInfusionTier(stack);
        int nextTier = currentTier + 1;

        return switch (nextTier) {
            case 1 -> AnimusConfig.ironsSpells.bloodSpellbookTier1EV.get();
            case 2 -> AnimusConfig.ironsSpells.bloodSpellbookTier2EV.get();
            case 3 -> AnimusConfig.ironsSpells.bloodSpellbookTier3EV.get();
            case 4 -> AnimusConfig.ironsSpells.bloodSpellbookTier4EV.get();
            case 5 -> AnimusConfig.ironsSpells.bloodSpellbookTier5EV.get();
            case 6 -> AnimusConfig.ironsSpells.bloodSpellbookTier6EV.get();
            default -> 0;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        int tier = getInfusionTier(stack);

        if (tier > 0) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Blood Infusion: Tier " + tier + "/6")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Current Bonuses:")
                .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.literal("  Spell Slots: " + getMaxSpellSlots(stack))
                .withStyle(ChatFormatting.GRAY));

            int manaBonus = getMaxManaBonus(stack);
            if (manaBonus > 0) {
                tooltip.add(Component.literal("  Max Mana: +" + manaBonus)
                    .withStyle(ChatFormatting.AQUA));
            }

            double costReduction = getEVCostReduction(stack);
            if (costReduction > 0) {
                tooltip.add(Component.literal("  EV Cost Reduction: -" + (int)(costReduction * 100) + "%")
                    .withStyle(ChatFormatting.GRAY));
            }

            double lifesteal = getLifesteal(stack);
            if (lifesteal > 0) {
                tooltip.add(Component.literal("  Spell Lifesteal: " + (int)(lifesteal * 100) + "%")
                    .withStyle(ChatFormatting.GREEN));
            }

            if (canUpgrade(stack)) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Next Tier Bonus:")
                    .withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal("  " + getNextTierBonus(tier + 1))
                    .withStyle(ChatFormatting.GRAY));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Upgrade Requirements:")
                    .withStyle(ChatFormatting.DARK_RED));
                tooltip.add(Component.literal("  " + getRequiredOrbName(tier + 1) + " or higher")
                    .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("  " + String.format("%,d EV in Ara Vitae", getUpgradeCost(stack)))
                    .withStyle(ChatFormatting.GRAY));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Right-click Ara Vitae to upgrade")
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
            tooltip.add(Component.literal("  6 Spell Slots, +50 Max Mana")
                .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("To Infuse:")
                .withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("  Place in Ara Vitae with " + String.format("%,d EV", AnimusConfig.ironsSpells.bloodSpellbookTier1EV.get()))
                .withStyle(ChatFormatting.GRAY));
        }
    }

    private static String getNextTierBonus(int tier) {
        int slots = calculateSlots(tier);
        int mana = getMaxManaBonusForTier(tier);
        String base = slots + " Spell Slots, +" + mana + " Max Mana";
        return switch (tier) {
            case 1, 2, 3 -> base;
            case 4 -> base + ", -10% EV Cost";
            case 5 -> base + ", -20% EV Cost";
            case 6 -> base + ", -20% EV Cost, +5% Lifesteal";
            default -> "Unknown bonus";
        };
    }

    public static int getRequiredOrbTier(int infusionTier) {
        return infusionTier - 1;
    }

    public static String getRequiredOrbName(int infusionTier) {
        int required = getRequiredOrbTier(infusionTier);
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BloodOrbItem orb && orb.getOrbTier(new ItemStack(item)) == required) {
                return item.getDescription().getString();
            }
        }
        return "Orb of Vitae";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getInfusionTier(stack) > 0;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        if (slotContext.entity().level().isClientSide) {
            return;
        }

        int manaBonus = getMaxManaBonus(stack);
        AttributeInstance manaAttribute = slotContext.entity().getAttribute(AttributeRegistry.MAX_MANA);

        if (manaAttribute == null) {
            return;
        }

        AttributeModifier existingModifier = manaAttribute.getModifier(MANA_MODIFIER_ID);

        if (manaBonus <= 0) {
            if (existingModifier != null) {
                manaAttribute.removeModifier(MANA_MODIFIER_ID);
            }
        } else {
            // Add or update modifier
            if (existingModifier == null || existingModifier.amount() != manaBonus) {
                if (existingModifier != null) {
                    manaAttribute.removeModifier(MANA_MODIFIER_ID);
                }
                manaAttribute.addPermanentModifier(new AttributeModifier(
                    MANA_MODIFIER_ID,
                    manaBonus,
                    AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

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

        AttributeInstance manaAttribute = slotContext.entity().getAttribute(AttributeRegistry.MAX_MANA);
        if (manaAttribute != null) {
            manaAttribute.removeModifier(MANA_MODIFIER_ID);
        }
    }
}
