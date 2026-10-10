package com.breakinblocks.animusnv.compat.ironsartifice;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.GunModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public final class BloodBulletModifier implements GunModifier {
    public static final int DEFAULT_EV_COST = 50;
    private static final int TRAIL_FROM = 0xd9303a;
    private static final int TRAIL_TO = 0x4a0510;
    private static final int MUZZLE_TINT = 0xa8121f;

    @Override
    public void apply(ShotComponentMap components) {
        components.modifyValue(
                AnimusShotComponents.BLOOD_BULLET,
                new ValueModifier(1, ValueModifier.Operation.ADD, ValueModifier.Type.NEUTRAL));
        components.getOrCreate(ShotComponents.ON_SHOT).getOrCreate(BloodBulletCostOnShot.class, BloodBulletCostOnShot::new);
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(ColorTransitionParticleOption.bulletTrail(TRAIL_FROM, TRAIL_TO));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(MUZZLE_TINT);
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("tooltip.animusnv.blood_bullet_modifier.effect")
                .withStyle(ChatFormatting.GREEN));
        builder.accept(Component.translatable("tooltip.animusnv.blood_bullet_modifier.cost", getEvCost())
                .withStyle(ChatFormatting.DARK_RED));
    }

    public static boolean isActive(ShotProfile profile) {
        return profile.value(AnimusShotComponents.BLOOD_BULLET) > 0;
    }

    public static int getEvCost() {
        try {
            return AnimusConfig.ironsArtifice.bloodBulletEvCost.get();
        } catch (IllegalStateException e) {
            return DEFAULT_EV_COST;
        }
    }

    public static boolean hasInfiniteMaterials(LivingEntity shooter) {
        return shooter instanceof Player player && player.hasInfiniteMaterials();
    }

    public static boolean canPay(LivingEntity shooter) {
        if (!(shooter instanceof Player player)) {
            return false;
        }
        int cost = getEvCost();
        if (cost <= 0 || player.hasInfiniteMaterials()) {
            return true;
        }
        IAnima anima = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        return anima != null && anima.getCurrentEV() >= cost;
    }
}
