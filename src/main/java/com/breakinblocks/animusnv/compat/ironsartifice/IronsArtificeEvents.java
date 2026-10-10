package com.breakinblocks.animusnv.compat.ironsartifice;

import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import io.redspace.irons_artifice.api.AmmoEvent;
import io.redspace.irons_artifice.api.BulletImpactEvent;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class IronsArtificeEvents {
    private IronsArtificeEvents() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAmmoAmount(AmmoEvent.Amount event) {
        if (event.getAmmoToConsume() <= 0 || !BloodBulletModifier.isActive(event.getShotProfile())) {
            return;
        }
        LivingEntity shooter = event.getEntity();
        if (shooter.level().isClientSide() || BloodBulletModifier.canPay(shooter)) {
            event.setAmmoToConsume(0);
        } else if (shooter instanceof Player player) {
            player.sendOverlayMessage(Component.translatable("text.component.animusnv.blood_bullet.no_ev")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public static void onComposeShot(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        if (!SpiritPowderModifier.isActive(profile) || !(event.getEntity() instanceof Player player)) {
            return;
        }
        double bonus = SpiritPowderModifier.getDamageBonus(player);
        if (bonus > 0) {
            profile.modifyValue(
                    ShotComponents.DAMAGE,
                    new ValueModifier(bonus, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBulletImpact(BulletImpactEvent event) {
        Bullet bullet = event.getBullet();
        if (event.isCanceled() || bullet.level().isClientSide() || bullet.getProfile() == null) {
            return;
        }
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (!SpiritPowderModifier.isActive(bullet.getProfile()) || !Utils.canHarm(bullet.getOwner(), target)) {
            return;
        }
        target.addEffect(
                new MobEffectInstance(
                        NVMobEffects.SPIRITUS_SNARE, SpiritPowderModifier.SNARE_DURATION_TICKS, 0, false, true),
                bullet.getOwner());
    }
}
