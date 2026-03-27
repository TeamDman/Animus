package com.breakinblocks.animusnv.events;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.items.ItemSentientShield;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;

/**
 * Applies will-type-based effects when a player blocks with a sentient shield.
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class SentientShieldEventHandler {

    private static final int EFFECT_DURATION = 100; // 5 seconds

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (!player.isBlocking()) {
            return;
        }

        ItemStack shield = ItemSentientShield.getSentientShield(player);
        if (shield.isEmpty() || !(shield.getItem() instanceof ItemSentientShield sentientShield)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        SpiritusType willType = sentientShield.getCurrentType(shield);
        double willAmount = NeoVitaeAPI.getInstance().getSpiritusHandler().getCurrentWill(
            player.level(), player.blockPosition(), willType
        );

        if (willAmount < 10.0) {
            return;
        }

        switch (willType) {
            case DEFAULT ->
                player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    EFFECT_DURATION,
                    1
                ));

            case STEADFAST ->
                player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    EFFECT_DURATION,
                    1
                ));

            case CORROSIVE ->
                livingAttacker.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    EFFECT_DURATION,
                    1
                ));

            case VENGEFUL -> {
                float reflectedDamage = event.getAmount() * 0.3f;
                livingAttacker.hurt(player.damageSources().thorns(player), reflectedDamage);

                livingAttacker.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    EFFECT_DURATION,
                    0
                ));
            }
        }
    }
}
