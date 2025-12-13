package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import java.util.UUID;

/**
 * Handles spell power boosting for Sigil of Crimson Will
 *
 * When active:
 * - Boosts spell power and summon damage by 30% base + up to 20% from demon will (50% max)
 * - Applies temporary attribute modifiers during spell casting
 * - Consumes LP based on spell mana cost
 * - Consumes demon will from player's aura
 */
public class CrimsonWillSpellHandler {

    // UUIDs for attribute modifiers
    private static final UUID SPELL_POWER_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SUMMON_DAMAGE_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    // Base bonus: 30%
    private static final double BASE_SPELL_POWER_BONUS = 0.30;

    // Max additional bonus from will: 20% (at 4096 will)
    private static final double MAX_WILL_BONUS = 0.20;
    private static final double MAX_WILL_AMOUNT = 4096.0;

    // Will consumed per spell cast
    private static final double WILL_CONSUMED_PER_CAST = 5.0;

    /**
     * Hook into spell casting to boost power and consume resources
     * Priority LOWEST to run after other modifications
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellCast(SpellPreCastEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        // Find active Sigil of Crimson Will
        ItemStack activeSigil = findActiveSigil(player);
        if (activeSigil == null) {
            return;
        }

        // Get spell level (this is the mana cost base)
        int spellLevel = event.getSpellLevel();
        // Note: We use spell level as an approximation of mana cost
        // In a more complete implementation, you'd want to get the actual spell object
        int manaCost = spellLevel; // Approximation

        // Calculate LP cost
        int lpCost = manaCost * AnimusConfig.ironsSpells.crimsonWillLPPerMana.get();

        // Check if player has enough LP
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
        if (network.getCurrentEssence() < lpCost) {
            player.displayClientMessage(
                Component.literal("Not enough LP! Need " + lpCost + " LP")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Get demon will from player's soul network
        double currentWill = PlayerDemonWillHandler.getTotalDemonWill(EnumWillType.DEFAULT, player);

        // Note: Sigil works at 0 will, just with lower bonus
        // No minimum will requirement check

        // Calculate spell power bonus
        // Base 30% + scaled bonus up to 20% based on will
        double willMultiplier = Math.min(currentWill / MAX_WILL_AMOUNT, 1.0);
        double willBonus = MAX_WILL_BONUS * willMultiplier;
        double totalBonus = BASE_SPELL_POWER_BONUS + willBonus;

        // Apply temporary attribute modifiers for spell power and summon damage
        applyPowerModifiers(player, totalBonus);

        // Consume LP
        network.syphon(new SoulTicket(
            Component.literal("Crimson Will Spell Empowerment"),
            lpCost
        ));

        // Consume demon will if player has any
        if (currentWill >= WILL_CONSUMED_PER_CAST) {
            PlayerDemonWillHandler.consumeDemonWill(EnumWillType.DEFAULT, player, WILL_CONSUMED_PER_CAST);
        }

        // Visual and audio feedback
        if (player.level() instanceof ServerLevel serverLevel) {
            // Crimson particles around player
            for (int i = 0; i < 10; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 2;
                double offsetY = serverLevel.random.nextDouble() * 2;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 2;

                serverLevel.sendParticles(
                    ParticleTypes.CRIMSON_SPORE,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1,
                    0.1, 0.1, 0.1,
                    0.05
                );
            }

            // Soul particles for demon will consumption
            for (int i = 0; i < 5; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5);
                double offsetY = serverLevel.random.nextDouble();
                double offsetZ = (serverLevel.random.nextDouble() - 0.5);

                serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1,
                    0, 0.2, 0,
                    0.1
                );
            }

            // Sound effect
            serverLevel.playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_PREPARE_ATTACK,
                SoundSource.PLAYERS,
                0.3F,
                1.5F + serverLevel.random.nextFloat() * 0.4F
            );
        }

        Animus.LOGGER.debug("Crimson Will empowered spell: {} LP, {} will, {:.1f}% bonus",
            lpCost, WILL_CONSUMED_PER_CAST, totalBonus * 100);
    }

    /**
     * Remove attribute modifiers after spell completes
     * Priority LOWEST to run after spell execution
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellOnCast(SpellOnCastEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        // Find active sigil
        ItemStack activeSigil = findActiveSigil(player);
        if (activeSigil == null) {
            return;
        }

        // Remove the attribute modifiers now that the spell has been cast
        removePowerModifiers(player);
    }

    /**
     * Find active Sigil of Crimson Will in player's inventory
     */
    private static ItemStack findActiveSigil(Player player) {
        // Check main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof ItemSigilCrimsonWill sigil && sigil.isActive(mainHand)) {
            return mainHand;
        }

        // Check offhand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof ItemSigilCrimsonWill sigil && sigil.isActive(offHand)) {
            return offHand;
        }

        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemSigilCrimsonWill sigil && sigil.isActive(stack)) {
                return stack;
            }
        }

        return null;
    }

    /**
     * Apply temporary attribute modifiers for spell power and summon damage
     */
    private static void applyPowerModifiers(Player player, double bonus) {
        // Apply spell power modifier
        AttributeInstance spellPowerAttr = player.getAttribute(AttributeRegistry.SPELL_POWER.get());
        if (spellPowerAttr != null) {
            // Remove existing modifier if present
            spellPowerAttr.removeModifier(SPELL_POWER_MODIFIER_UUID);

            // Add new modifier (MULTIPLY_TOTAL for percentage bonuses)
            AttributeModifier spellPowerMod = new AttributeModifier(
                SPELL_POWER_MODIFIER_UUID,
                "Crimson Will Spell Power",
                bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            spellPowerAttr.addTransientModifier(spellPowerMod);
        }

        // Apply summon damage modifier
        AttributeInstance summonDamageAttr = player.getAttribute(AttributeRegistry.SUMMON_DAMAGE.get());
        if (summonDamageAttr != null) {
            // Remove existing modifier if present
            summonDamageAttr.removeModifier(SUMMON_DAMAGE_MODIFIER_UUID);

            // Add new modifier
            AttributeModifier summonDamageMod = new AttributeModifier(
                SUMMON_DAMAGE_MODIFIER_UUID,
                "Crimson Will Summon Damage",
                bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            summonDamageAttr.addTransientModifier(summonDamageMod);
        }
    }

    /**
     * Remove temporary attribute modifiers
     */
    private static void removePowerModifiers(Player player) {
        // Remove spell power modifier
        AttributeInstance spellPowerAttr = player.getAttribute(AttributeRegistry.SPELL_POWER.get());
        if (spellPowerAttr != null) {
            spellPowerAttr.removeModifier(SPELL_POWER_MODIFIER_UUID);
        }

        // Remove summon damage modifier
        AttributeInstance summonDamageAttr = player.getAttribute(AttributeRegistry.SUMMON_DAMAGE.get());
        if (summonDamageAttr != null) {
            summonDamageAttr.removeModifier(SUMMON_DAMAGE_MODIFIER_UUID);
        }
    }
}
