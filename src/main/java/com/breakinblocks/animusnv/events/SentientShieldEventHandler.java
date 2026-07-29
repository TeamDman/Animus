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

        SpiritusType spiritusType = sentientShield.getCurrentType(shield);
        double spiritusAmount = NeoVitaeAPI.getInstance().getSpiritusHandler().getCurrentSpiritus(
            player.level(), player.blockPosition(), spiritusType
        );

        if (spiritusAmount < 10.0) {
            return;
        }

        switch (spiritusType) {
            case RAW ->
                player.addEffect(new MobEffectInstance(
                    MobEffects.STRENGTH,
                    EFFECT_DURATION,
                    1
                ));

            case INVICTUS ->
                player.addEffect(new MobEffectInstance(
                    MobEffects.RESISTANCE,
                    EFFECT_DURATION,
                    1
                ));

            case RUINA ->
                livingAttacker.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    EFFECT_DURATION,
                    1
                ));

            case VINDICTA -> {
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
