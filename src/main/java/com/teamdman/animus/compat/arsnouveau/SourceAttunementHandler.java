package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.LivingUpgradeHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import wayoftime.bloodmagic.common.living.LivingUpgrade;
import wayoftime.bloodmagic.common.registry.BMRegistries;

/**
 * Source Attunement - Living Armor upgrade tree for Ars Nouveau spellcasters
 *
 * Level 1: +5% spell damage
 * Level 2: +10% spell damage (total)
 * Level 3: +15% spell damage (total)
 * Level 4: +20% spell damage (total) + Mana Regen on cast (4s)
 * Level 5: +25% spell damage (total) + Spell Damage buff (amplifier 2, 5s)
 */
public class SourceAttunementHandler {

    public static final ResourceKey<LivingUpgrade> UPGRADE_KEY = ResourceKey.create(
        BMRegistries.Keys.LIVING_UPGRADES,
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "source_attunement")
    );

    public static void register() {
        NeoForge.EVENT_BUS.register(new SourceAttunementHandler());
    }

    /**
     * Modify spell damage based on upgrade level
     * Each level adds +5% spell damage (Level 1: +5%, Level 2: +10%, ..., Level 5: +25%)
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSpellDamage(SpellDamageEvent event) {
        if (!(event.caster instanceof Player player)) {
            return;
        }

        int upgradeLevel = LivingUpgradeHelper.getUpgradeLevel(player, UPGRADE_KEY);
        if (upgradeLevel <= 0) {
            return;
        }

        // Apply damage boost: 5% per level
        double damageBoost = 0.05 * upgradeLevel;
        float newDamage = event.damage * (1.0f + (float) damageBoost);
        event.damage = newDamage;
    }

    /**
     * Handle effects when spell is cast
     * Level 4: Grant Mana Regeneration for 4 seconds
     * Level 5: Spell Damage buff (amplifier 2) for 5 seconds
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCast(SpellCastEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        int upgradeLevel = LivingUpgradeHelper.getUpgradeLevel(player, UPGRADE_KEY);
        if (upgradeLevel <= 0) {
            return;
        }

        // Level 4: Grant Mana Regeneration on cast for 4 seconds
        if (upgradeLevel >= 4) {
            player.addEffect(new MobEffectInstance(
                ModPotions.MANA_REGEN_EFFECT,
                80, // 4 seconds
                0,  // Amplifier 0
                false,
                false,
                true
            ));
        }

        // Level 5: Grant Spell Damage buff (amplifier 2) for 5 seconds
        if (upgradeLevel >= 5) {
            player.addEffect(new MobEffectInstance(
                ModPotions.SPELL_DAMAGE_EFFECT,
                100, // 5 seconds
                2,   // Amplifier 2
                false,
                false,
                true
            ));
        }
    }
}
