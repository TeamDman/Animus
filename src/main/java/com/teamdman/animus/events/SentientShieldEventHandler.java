package com.teamdman.animus.events;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.ItemSentientShield;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.will.WorldDemonWillHandler;

/**
 * Event handler for Sentient Shield blocking effects
 * When a player blocks with a sentient shield, applies effects based on will type
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class SentientShieldEventHandler {

    private static final int EFFECT_DURATION = 100; // 5 seconds in ticks

    /**
     * Sentient Shield blocking effects
     * When a player blocks an attack with a sentient shield, apply effects based on will type
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        // Only handle players on server side
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        // Check if player is blocking and has a sentient shield
        if (!player.isBlocking()) {
            return;
        }

        ItemStack shield = ItemSentientShield.getSentientShield(player);
        if (shield.isEmpty() || !(shield.getItem() instanceof ItemSentientShield sentientShield)) {
            return;
        }

        // Get the attacker
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        // Get the will type and amount
        EnumWillType willType = sentientShield.getCurrentType(shield);
        double willAmount = WorldDemonWillHandler.getCurrentWill(
            player.level(), player.blockPosition(), willType
        );

        // Need at least some will to trigger effects
        if (willAmount < 10.0) {
            return;
        }

        // Apply effects based on will type
        switch (willType) {
            case DEFAULT -> // Raw will - Strength 2 to player
                player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    EFFECT_DURATION,
                    1 // Level 2 (0-indexed)
                ));

            case STEADFAST -> // Resistance 2 to player
                player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    EFFECT_DURATION,
                    1 // Level 2
                ));

            case CORROSIVE -> // Poison to attacker
                livingAttacker.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    EFFECT_DURATION,
                    1 // Level 2
                ));

            case VENGEFUL -> {
                // 30% damage reflection + weakness to attacker
                float reflectedDamage = event.getAmount() * 0.3f;
                livingAttacker.hurt(player.damageSources().thorns(player), reflectedDamage);

                // Apply weakness
                livingAttacker.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    EFFECT_DURATION,
                    0 // Level 1
                ));
            }
        }
    }
}
