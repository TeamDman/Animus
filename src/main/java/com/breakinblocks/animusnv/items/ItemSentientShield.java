package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.util.SpiritusTypeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.TooltipDisplay;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.soul.SpiritusTooltipHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Sentient Shield - A demon-will powered shield
 * Has 4x the durability of a normal shield (1344 vs 336)
 * Grants special effects when blocking based on the Spiritus type available
 * Increases Spiritus gained by 30% while equipped
 */
public class ItemSentientShield extends ShieldItem {
    // Normal shield has 336 durability, sentient has 4x
    private static final int SENTIENT_SHIELD_DURABILITY = 336 * 4; // 1344

    public ItemSentientShield(Properties props) {
        super(props.durability(SENTIENT_SHIELD_DURABILITY)
            .equippableUnswappable(EquipmentSlot.OFFHAND)
            .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(
                0.25F,
                1.0F,
                List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                Optional.of(SoundEvents.SHIELD_BLOCK),
                Optional.of(SoundEvents.SHIELD_BREAK)))
            .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_SHIELD_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        SpiritusTooltipHelper.appendSpiritusInfo(stack, "sentientShield", tooltip, flag);

        if (flag.hasShiftDown()) {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SENTIENT_SHIELD_SPIRITUS_BONUS)
                .withStyle(ChatFormatting.GREEN));
        }

        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (entity instanceof Player player) {
            SpiritusType newType = SpiritusTypeHelper.findSpiritusType(player);
            if (newType != getCurrentType(stack)) {
                setCurrentType(stack, newType);
            }
        }
    }

    public SpiritusType getCurrentType(ItemStack stack) {
        return SpiritusTypeHelper.getCurrentType(stack);
    }

    public void setCurrentType(ItemStack stack, SpiritusType type) {
        SpiritusTypeHelper.setCurrentType(stack, type);
    }

    public List<ItemStack> getRandomSpiritusDrop(LivingEntity killedEntity, LivingEntity attackingEntity, ItemStack stack, int tier) {
        return new ArrayList<>();
    }

    public SpiritusType getActiveSpiritusType(ItemStack stack, LivingEntity player, Entity target) {
        return getCurrentType(stack);
    }

    /** 1.3 = 30% bonus will gain while equipped */
    public double getSpiritusGainMultiplier() {
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
