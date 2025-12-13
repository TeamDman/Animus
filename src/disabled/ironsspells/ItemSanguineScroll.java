package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.AnimusConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Sanguine Scroll - Reusable spell scroll that consumes LP instead of being consumed
 *
 * Features:
 * - Stores a single spell with level
 * - Consumes LP to cast (mana cost × 150)
 * - Has durability based on slate tier
 * - Created by infusing normal scrolls at Blood Altar
 */
public class ItemSanguineScroll extends Item {

    // Slate tier determines max durability
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

    /**
     * Store spell data in the scroll
     */
    public static void setSpell(ItemStack stack, String spellId, int spellLevel) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("SpellId", spellId);
        tag.putInt("SpellLevel", spellLevel);
    }

    /**
     * Get spell ID from scroll
     */
    public static String getSpellId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("SpellId")) {
            return stack.getTag().getString("SpellId");
        }
        return "";
    }

    /**
     * Get spell level from scroll
     */
    public static int getSpellLevel(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("SpellLevel")) {
            return stack.getTag().getInt("SpellLevel");
        }
        return 0;
    }

    /**
     * Check if scroll has a spell
     */
    public static boolean hasSpell(ItemStack stack) {
        return !getSpellId(stack).isEmpty();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Check if scroll has a spell
        if (!hasSpell(stack)) {
            player.displayClientMessage(
                Component.literal("Empty Sanguine Scroll - Use at Blood Altar to infuse with a spell")
                    .withStyle(ChatFormatting.GRAY),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        // Get spell
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

        // Check cooldown
        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData.getPlayerCooldowns().hasCooldownsActive()) {
            player.displayClientMessage(
                Component.literal("Spell is on cooldown")
                    .withStyle(ChatFormatting.GOLD),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        // Calculate LP cost (mana cost × lpPerMana × multiplier)
        // Default: 100 × 1.5 = 150 LP per mana (50% more expensive than regular casting)
        int manaCost = spell.getManaCost(spellLevel);
        int lpPerMana = AnimusConfig.ironsSpells.lpPerMana.get();
        double multiplier = AnimusConfig.ironsSpells.sanguineScrollLPMultiplier.get();
        int lpCost = (int)(manaCost * lpPerMana * multiplier);

        // Check if player has enough LP
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
        if (network.getCurrentEssence() < lpCost) {
            player.displayClientMessage(
                Component.literal("Not enough LP! Need " + lpCost + " LP")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        // Consume LP
        network.syphon(new SoulTicket(
            Component.literal("Sanguine Scroll Casting"),
            lpCost
        ));

        // Cast spell
        try {
            spell.attemptInitiateCast(stack, spellLevel, level, player, CastSource.SCROLL, true, SpellSelectionManager.MAINHAND);

            // Apply cooldown
            magicData.getPlayerCooldowns().addCooldown(spell, spell.getSpellCooldown());

            // Damage scroll
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));

            // Play sound
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

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

                // Show LP cost
                int manaCost = spell.getManaCost(spellLevel);
                int lpPerMana = AnimusConfig.ironsSpells.lpPerMana.get();
                double multiplier = AnimusConfig.ironsSpells.sanguineScrollLPMultiplier.get();
                int lpCost = (int)(manaCost * lpPerMana * multiplier);

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Cost: " + lpCost + " LP")
                    .withStyle(ChatFormatting.RED));

                // Show uses remaining
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
            tooltip.add(Component.literal("Empty - Infuse at Blood Altar")
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
