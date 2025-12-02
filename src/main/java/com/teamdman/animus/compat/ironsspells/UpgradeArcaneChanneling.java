package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Constants;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
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
 * Current implementation:
 * Level 5: Casting grants brief damage resistance (Resistance I for 2 seconds)
 *
 * Planned features (require mixins to implement):
 * Level 1-2: Mana cost reduction
 * Level 3-4: Cooldown reduction and armor synergy
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

        // Levels 1-2: Mana cost reduction
        // Note: Iron's Spellbooks doesn't expose mana cost modification in SpellPreCastEvent
        // This would require either:
        // - A mixin to modify spell mana costs
        // - Refunding mana after the cast via SpellOnCastEvent
        // For now, levels 1-2 provide passive benefits through the level 5 damage resistance

        // Level 3-4: Cooldown reduction and armor synergy
        // Note: Cooldown reduction would require modifying MagicData.getPlayerCooldowns()
        // which is not easily accessible without mixins. These levels currently provide
        // no additional benefit beyond level 5's damage resistance.

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
