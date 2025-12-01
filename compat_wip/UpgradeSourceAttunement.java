package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.mana.IManaStorage;
import com.hollingsworth.arsnouveau.common.capability.ManaCapability;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import com.teamdman.animus.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.core.living.LivingUpgrade;
import wayoftime.bloodmagic.core.living.LivingUtil;

/**
 * Source Attunement - Living Armor upgrade tree for Ars Nouveau spellcasters
 *
 * Level 1: +5% spell damage
 * Level 2: +10% spell damage (total)
 * Level 3: +15% spell damage (total)
 * Level 4: +20% spell damage (total) + Mana Regen on cast (4s)
 * Level 5: +25% spell damage (total) + 20% mana cost reduction + Spell Damage buff (amplifier 2, 5s)
 */
public class UpgradeSourceAttunement extends LivingUpgrade {

    public static final int[] COST = new int[]{5, 10, 15, 20, 25};
    public static final String KEY = Constants.Mod.MODID + ".upgrade.source_attunement";

    public UpgradeSourceAttunement() {
        super(KEY, COST.length);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public int getMaxTier() {
        return COST.length;
    }

    @Override
    public int getCost(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= COST.length) {
            return 0;
        }
        return COST[currentLevel];
    }

    @Override
    public void onTick(Player player, int level) {
        // Passive effects handled in event listeners
    }

    /**
     * Modify spell damage based on upgrade level
     * Each level adds +5% spell damage (Level 1: +5%, Level 2: +10%, ..., Level 5: +25%)
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSpellDamage(SpellDamageEvent event) {
        if (!(event.shooter instanceof Player player)) {
            return;
        }

        // Get upgrade level
        int upgradeLevel = LivingUtil.getUpgradeLevel(player, KEY);
        if (upgradeLevel <= 0) {
            return;
        }

        // Apply damage boost: 5% per level
        double damageBoost = 0.05 * upgradeLevel;
        float newDamage = event.damage * (1.0f + (float)damageBoost);
        event.damage = newDamage;
    }

    /**
     * Handle effects when spell is cast
     * Level 4: Grant Mana Regeneration for 4 seconds
     * Level 5: 20% mana cost reduction + Spell Damage buff (amplifier 2) for 5 seconds
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCast(SpellCastEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Get upgrade level
        int upgradeLevel = LivingUtil.getUpgradeLevel(player, KEY);
        if (upgradeLevel <= 0) {
            return;
        }

        // Level 4: Grant Mana Regeneration on cast for 4 seconds
        if (upgradeLevel >= 4) {
            player.addEffect(new MobEffectInstance(
                ModPotions.MANA_REGEN_EFFECT.get(),
                80, // 4 seconds
                0,  // Amplifier 0
                false,
                false,
                true
            ));
        }

        // Level 5: Grant Spell Damage buff (amplifier 2) for 5 seconds
        // Also provides 20% mana cost reduction (would need to be handled via cost modification or refund)
        if (upgradeLevel >= 5) {
            player.addEffect(new MobEffectInstance(
                ModPotions.SPELL_DAMAGE_EFFECT.get(),
                100, // 5 seconds
                2,   // Amplifier 2
                false,
                false,
                true
            ));

            // Refund 20% of mana cost
            int manaCost = event.getSpell().getCastingCost();
            int refundAmount = (int) (manaCost * 0.20);
            if (refundAmount > 0) {
                player.getCapability(ManaCapability.MANA).ifPresent(mana -> {
                    if (mana instanceof IManaStorage storage) {
                        int currentMana = storage.getCurrentMana();
                        storage.setMana(currentMana + refundAmount);
                    }
                });
            }
        }
    }
}
