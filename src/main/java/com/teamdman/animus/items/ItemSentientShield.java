package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;

import java.util.List;

/**
 * Sentient Shield - A demon-will powered shield
 * Has 4x the durability of a normal shield (1344 vs 336)
 * Grants special effects when blocking based on the demon will type available
 * Increases demon will gained by 30% while equipped
 */
public class ItemSentientShield extends ShieldItem {
    // Normal shield has 336 durability, sentient has 4x
    private static final int SENTIENT_SHIELD_DURABILITY = 336 * 4; // 1344

    public ItemSentientShield() {
        super(new Properties().durability(SENTIENT_SHIELD_DURABILITY));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_SHIELD_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        EnumWillType type = getCurrentType(stack);
        String displayType = type == EnumWillType.DEFAULT ? "raw" : type.name().toLowerCase();
        tooltip.add(Component.translatable("tooltip.animus.sentient_shield.will_type", displayType)
            .withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.literal(""));

        // Show only the relevant effect for the current will type
        switch (type) {
            case DEFAULT:
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_SHIELD_RAW)
                    .withStyle(ChatFormatting.GOLD));
                break;
            case STEADFAST:
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_SHIELD_STEADFAST)
                    .withStyle(ChatFormatting.GOLD));
                break;
            case CORROSIVE:
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_SHIELD_CORROSIVE)
                    .withStyle(ChatFormatting.GOLD));
                break;
            case VENGEFUL:
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_SHIELD_VENGEFUL)
                    .withStyle(ChatFormatting.GOLD));
                break;
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_SHIELD_WILL_BONUS)
            .withStyle(ChatFormatting.GREEN));

        super.appendHoverText(stack, context, tooltip, flag);
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
        // Get will amounts from the player's soul network
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
     * Gets the total amount of will the player has of a specific type from their soul network
     */
    private static double getTotalWillOfType(Player player, EnumWillType type) {
        return PlayerDemonWillHandler.getTotalDemonWill(type, player);
    }

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

    public List<ItemStack> getRandomDemonWillDrop(LivingEntity killedEntity, LivingEntity attackingEntity, ItemStack stack, int tier) {
        // Sentient Shield doesn't drop will items, it drains will from the aura
        return new java.util.ArrayList<>();
    }

    public EnumWillType getActiveDemonWillType(ItemStack stack, LivingEntity player, Entity target) {
        return getCurrentType(stack);
    }

    /**
     * Get the demon will multiplier for this shield (1.3 = 30% bonus)
     */
    public double getWillGainMultiplier() {
        return 1.3;
    }

    /**
     * Check if a player has a sentient shield equipped (main or offhand)
     */
    public static boolean hasSentientShieldEquipped(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();
        return mainHand.getItem() instanceof ItemSentientShield || offHand.getItem() instanceof ItemSentientShield;
    }

    /**
     * Get the sentient shield if equipped, null otherwise
     */
    public static ItemStack getSentientShield(LivingEntity entity) {
        if (entity == null) {
            return ItemStack.EMPTY;
        }
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();
        if (mainHand.getItem() instanceof ItemSentientShield) {
            return mainHand;
        } else if (offHand.getItem() instanceof ItemSentientShield) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }
}
