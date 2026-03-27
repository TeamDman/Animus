package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;

import java.util.List;

/**
 * Sanguine Scroll - Reusable spell scroll that consumes EV instead of being consumed
 *
 * Features:
 * - Stores a single spell with level
 * - Consumes EV to cast (mana cost × 150)
 * - Has durability based on slate tier
 * - Created by infusing normal scrolls at Ara Vitae
 */
public class ItemSanguineScroll extends Item {

    public enum SlateType {
        BLANK(50),
        REINFORCED(100),
        IMBUED(200),
        DEMON(400),
        ETHEREAL(600);

        private final int durability;

        SlateType(int durability) {
            this.durability = durability;
        }

        public int getDurability() {
            return durability;
        }
    }

    private final SlateType slateType;

    public ItemSanguineScroll(SlateType slateType) {
        super(new Properties()
            .stacksTo(1)
            .durability(slateType.getDurability()));
        this.slateType = slateType;
    }

    public SlateType getSlateType() {
        return slateType;
    }

    public static void setSpell(ItemStack stack, String spellId, int spellLevel) {
        stack.set(AnimusDataComponents.SPELL_ID.get(), spellId);
        stack.set(AnimusDataComponents.SPELL_LEVEL.get(), spellLevel);
    }

    public static String getSpellId(ItemStack stack) {
        return stack.getOrDefault(AnimusDataComponents.SPELL_ID.get(), "");
    }

    public static int getSpellLevel(ItemStack stack) {
        return stack.getOrDefault(AnimusDataComponents.SPELL_LEVEL.get(), 0);
    }

    public static boolean hasSpell(ItemStack stack) {
        String spellId = getSpellId(stack);
        return spellId != null && !spellId.isEmpty();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        if (!hasSpell(stack)) {
            player.displayClientMessage(
                Component.literal("Empty Sanguine Scroll - Use at Ara Vitae to infuse with a spell")
                    .withStyle(ChatFormatting.GRAY),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        String spellId = getSpellId(stack);
        int spellLevel = getSpellLevel(stack);

        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null) {
            player.displayClientMessage(
                Component.literal("Invalid spell data")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData.getPlayerCooldowns().hasCooldownsActive()) {
            player.displayClientMessage(
                Component.literal("Spell is on cooldown")
                    .withStyle(ChatFormatting.GOLD),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        int manaCost = spell.getManaCost(spellLevel);
        int evPerMana = AnimusConfig.ironsSpells.evPerMana.get();
        double multiplier = AnimusConfig.ironsSpells.sanguineScrollLPMultiplier.get();
        int evCost = (int)(manaCost * evPerMana * multiplier);

        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (network.getCurrentEV() < evCost) {
            player.displayClientMessage(
                Component.literal("Not enough EV! Need " + evCost + " EV")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        network.syphonAndDamage(player, AnimaTicket.create(evCost));
        try {
            spell.attemptInitiateCast(stack, spellLevel, level, player, CastSource.SCROLL, true, "sanguine_scroll");

            magicData.getPlayerCooldowns().addCooldown(spell, spell.getSpellCooldown());
            if (level instanceof ServerLevel serverLevel) {
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, serverLevel, player, (item) ->
                    player.onEquippedItemBroken(item, slot));
            }

            level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS,
                1.0F,
                1.2F
            );

            return InteractionResultHolder.consume(stack);

        } catch (Exception e) {
            player.displayClientMessage(
                Component.literal("Failed to cast spell: " + e.getMessage())
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.literal("Reusable spell scroll")
            .withStyle(ChatFormatting.DARK_RED));

        tooltip.add(Component.literal(""));

        if (hasSpell(stack)) {
            String spellId = getSpellId(stack);
            int spellLevel = getSpellLevel(stack);

            AbstractSpell spell = SpellRegistry.getSpell(spellId);
            if (spell != null) {
                tooltip.add(Component.literal("Spell: ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(spell.getDisplayName(null).getString())
                        .withStyle(ChatFormatting.WHITE)));

                tooltip.add(Component.literal("Level: " + spellLevel)
                    .withStyle(ChatFormatting.GRAY));

                int manaCost = spell.getManaCost(spellLevel);
                int evPerMana = AnimusConfig.ironsSpells.evPerMana.get();
                double multiplier = AnimusConfig.ironsSpells.sanguineScrollLPMultiplier.get();
                int evCost = (int)(manaCost * evPerMana * multiplier);

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Cost: " + evCost + " EV")
                    .withStyle(ChatFormatting.RED));

                int damage = stack.getDamageValue();
                int maxDamage = stack.getMaxDamage();
                int usesRemaining = maxDamage - damage;

                tooltip.add(Component.literal("Uses: " + usesRemaining + "/" + maxDamage)
                    .withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.add(Component.literal("Invalid spell data")
                    .withStyle(ChatFormatting.RED));
            }
        } else {
            tooltip.add(Component.literal("Empty - Infuse at Ara Vitae")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Slate: " + slateType.name())
            .withStyle(ChatFormatting.DARK_GRAY));

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Right-click to cast spell")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}
