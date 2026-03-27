package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.util.DemonWillTypeHelper;
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
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;

import java.util.List;

/**
 * Sentient Shield - A demon-will powered shield
 * Has 4x the durability of a normal shield (1344 vs 336)
 * Grants special effects when blocking based on the Spiritus type available
 * Increases Spiritus gained by 30% while equipped
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

        SpiritusType type = getCurrentType(stack);
        String displayType = type == SpiritusType.DEFAULT ? "raw" : type.name().toLowerCase();
        tooltip.add(Component.translatable("tooltip.animusnv.sentient_shield.will_type", displayType)
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
            SpiritusType newType = DemonWillTypeHelper.findDemonWillType(player);
            if (newType != getCurrentType(stack)) {
                setCurrentType(stack, newType);
            }
        }
    }

    public SpiritusType getCurrentType(ItemStack stack) {
        return DemonWillTypeHelper.getCurrentType(stack);
    }

    public void setCurrentType(ItemStack stack, SpiritusType type) {
        DemonWillTypeHelper.setCurrentType(stack, type);
    }

    public List<ItemStack> getRandomSpiritusDrop(LivingEntity killedEntity, LivingEntity attackingEntity, ItemStack stack, int tier) {
        return new java.util.ArrayList<>();
    }

    public SpiritusType getActiveDemonWillType(ItemStack stack, LivingEntity player, Entity target) {
        return getCurrentType(stack);
    }

    /** 1.3 = 30% bonus will gain while equipped */
    public double getWillGainMultiplier() {
        return 1.3;
    }

    public static boolean hasSentientShieldEquipped(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();
        return mainHand.getItem() instanceof ItemSentientShield || offHand.getItem() instanceof ItemSentientShield;
    }

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
