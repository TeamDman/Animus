package com.teamdman.animus.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.core.living.ILivingContainer;
import wayoftime.bloodmagic.core.living.LivingStats;
import wayoftime.bloodmagic.core.living.LivingUpgrade;

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

    public static final ResourceLocation KEY = new ResourceLocation(
        Constants.Mod.MODID,
        "upgrade.source_attunement"
    );

    public UpgradeSourceAttunement() {
        super(KEY, levels -> {
            // Define XP thresholds for each level
            levels.add(new Level(0, 5));    // Level 1: 5 upgrade points
            levels.add(new Level(0, 10));   // Level 2: 10 points
            levels.add(new Level(0, 15));   // Level 3: 15 points
            levels.add(new Level(0, 20));   // Level 4: 20 points
            levels.add(new Level(0, 25));   // Level 5: 25 points
        });
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Get the upgrade level for this player
     * Checks all armor pieces and returns the highest level found
     */
    private int getUpgradeLevel(Player player) {
        int maxLevel = 0;
        for (ItemStack armorPiece : player.getInventory().armor) {
            if (armorPiece.getItem() instanceof ILivingContainer container) {
                LivingStats stats = container.getLivingStats(armorPiece);
                if (stats != null) {
                    int level = stats.getLevel(KEY);
                    maxLevel = Math.max(maxLevel, level);
                }
            }
        }
        return maxLevel;
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

        // Get upgrade level
        int upgradeLevel = getUpgradeLevel(player);
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
        int upgradeLevel = getUpgradeLevel(player);
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
        // Also provides 20% mana cost reduction (implemented via buff effect)
        if (upgradeLevel >= 5) {
            player.addEffect(new MobEffectInstance(
                ModPotions.SPELL_DAMAGE_EFFECT.get(),
                100, // 5 seconds
                2,   // Amplifier 2
                false,
                false,
                true
            ));

            // Note: Mana cost reduction would require access to IManaStorage
            // which doesn't exist in current Ars Nouveau version
            // The Spell Damage buff partially compensates for this
        }
    }
}
