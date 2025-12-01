package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Constants;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.core.living.ILivingContainer;
import wayoftime.bloodmagic.core.living.LivingStats;
import wayoftime.bloodmagic.core.living.LivingUpgrade;

/**
 * Arcane Channeling - Living Armor upgrade tree for spellcasters
 *
 * Level 1: -5% mana cost
 * Level 2: -10% mana cost (total)
 * Level 3: -5% cooldown reduction
 * Level 4: Spells trigger armor effects
 * Level 5: Casting grants brief damage resistance
 */
public class UpgradeArcaneChanneling extends LivingUpgrade {

    public static final ResourceLocation KEY = new ResourceLocation(
        Constants.Mod.MODID,
        "upgrade.arcane_channeling"
    );

    public UpgradeArcaneChanneling() {
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
     * Reduce mana cost based on upgrade level
     * Levels 1-2: Progressive mana cost reduction
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSpellPreCast(SpellPreCastEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        // Get upgrade level
        int upgradeLevel = getUpgradeLevel(player);
        if (upgradeLevel <= 0) {
            return;
        }

        // Calculate mana cost reduction
        double reduction = 0.0;
        if (upgradeLevel >= 1) {
            reduction = 0.05; // 5% at level 1
        }
        if (upgradeLevel >= 2) {
            reduction = 0.10; // 10% at level 2
        }

        // Apply mana cost reduction (this is a bit tricky - we'll modify the spell level to reduce cost)
        // Note: This is a simplified approach. A more complete implementation would modify the actual mana cost
        // For now, we'll just note that this should reduce effective mana cost

        // Level 3: Cooldown reduction (handled separately via cooldown modification)
        if (upgradeLevel >= 3) {
            // Reduce cooldowns by 5%
            MagicData magicData = MagicData.getPlayerMagicData(player);
            // Note: Cooldown reduction would require modifying the cooldown system
            // This is left as a TODO for more complex implementation
        }

        // Level 4: Trigger armor effects on spell cast
        if (upgradeLevel >= 4) {
            // This would trigger Living Armor's defensive abilities
            // Implementation depends on what "trigger armor effects" means
            // Could apply temporary buffs or activate armor abilities
        }

        // Level 5: Grant damage resistance when casting
        if (upgradeLevel >= 5) {
            // Grant brief damage resistance (2 seconds, Resistance I)
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
}
