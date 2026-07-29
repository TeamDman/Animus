package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.SentientUpgradeHelper;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Arcane Channeling - Sentient Armor upgrade tree for Iron's Spells casters
 *
 * Level 1: 5% mana cost reduction
 * Level 2: 10% mana cost reduction (total)
 * Level 3: 5% cooldown reduction
 * Level 4: 10% cooldown reduction (total)
 * Level 5: Casting grants brief damage resistance (Resistance I for 2 seconds)
 */
public class ArcaneChannelingHandler {

    public static final Identifier UPGRADE_ID =
        Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "arcane_channeling");

    private static final Identifier COOLDOWN_MODIFIER_ID =
        Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "arcane_channeling_cooldown");

    public static void register() {
        NeoForge.EVENT_BUS.register(new ArcaneChannelingHandler());
    }

    private double getManaCostReduction(int upgradeLevel) {
        if (upgradeLevel >= 2) {
            return 0.10; // 10% reduction
        } else if (upgradeLevel >= 1) {
            return 0.05; // 5% reduction
        }
        return 0.0;
    }

    private double getCooldownReduction(int upgradeLevel) {
        if (upgradeLevel >= 4) {
            return 0.10; // 10% reduction
        } else if (upgradeLevel >= 3) {
            return 0.05; // 5% reduction
        }
        return 0.0;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSpellCast(SpellOnCastEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        int upgradeLevel = SentientUpgradeHelper.getUpgradeLevel(player, UPGRADE_ID);
        if (upgradeLevel <= 0) {
            return;
        }

        double reduction = getManaCostReduction(upgradeLevel);
        if (reduction > 0) {
            int originalCost = event.getManaCost();
            int reducedCost = (int) Math.max(1, originalCost * (1.0 - reduction));
            event.setManaCost(reducedCost);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onSpellPreCast(SpellPreCastEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        int upgradeLevel = SentientUpgradeHelper.getUpgradeLevel(player, UPGRADE_ID);

        if (upgradeLevel >= 5) {
            player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                40, // 2 seconds
                0,  // Level I
                false,
                false,
                true
            ));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.tickCount % 20 != 0) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        int upgradeLevel = SentientUpgradeHelper.getUpgradeLevel(player, UPGRADE_ID);
        double targetReduction = getCooldownReduction(upgradeLevel);

        Holder<Attribute> cooldownAttr = AttributeRegistry.COOLDOWN_REDUCTION;
        AttributeInstance cooldownAttribute = player.getAttribute(cooldownAttr);
        if (cooldownAttribute == null) {
            return;
        }

        AttributeModifier existingModifier = cooldownAttribute.getModifier(COOLDOWN_MODIFIER_ID);

        if (targetReduction <= 0) {
            if (existingModifier != null) {
                cooldownAttribute.removeModifier(COOLDOWN_MODIFIER_ID);
            }
        } else {
            if (existingModifier == null || Math.abs(existingModifier.amount() - targetReduction) > 0.001) {
                if (existingModifier != null) {
                    cooldownAttribute.removeModifier(COOLDOWN_MODIFIER_ID);
                }
                cooldownAttribute.addPermanentModifier(new AttributeModifier(
                    COOLDOWN_MODIFIER_ID,
                    targetReduction,
                    AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }
}
