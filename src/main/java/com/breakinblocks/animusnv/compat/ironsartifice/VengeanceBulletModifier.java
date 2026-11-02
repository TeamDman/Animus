package com.breakinblocks.animusnv.compat.ironsartifice;

import com.breakinblocks.animusnv.Constants;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ParticleBurst;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public final class VengeanceBulletModifier implements GunModifier {
    public static final ResourceKey<DamageType> VENGEANCE_BEAM =
            ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("evilcraft", "vengeance_beam"));
    private static final int TRAIL_FROM = 0xd8c8ff;
    private static final int TRAIL_TO = 0x2a1245;
    private static final int MUZZLE_TINT = 0x8f5fd6;

    @Override
    public void apply(ShotComponentMap components) {
        components.modifyValue(
                AnimusShotComponents.VENGEANCE_BULLET,
                new ValueModifier(1, ValueModifier.Operation.ADD, ValueModifier.Type.NEUTRAL));
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(ColorTransitionParticleOption.bulletTrail(TRAIL_FROM, TRAIL_TO));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(MUZZLE_TINT);
        components.getOrCreate(ShotComponents.MUZZLE_FLASH)
                .addAirBurst(new ParticleBurst(ParticleTypes.SCULK_SOUL, 2, 0.3f, false));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("tooltip.animusnv.vengeance_bullet_modifier.effect")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static boolean isActive(ShotProfile profile) {
        return profile != null && profile.value(AnimusShotComponents.VENGEANCE_BULLET) > 0;
    }

    public static boolean isTarget(Entity entity) {
        return entity.is(Constants.Tags.VENGEANCE_BULLET_TARGETS);
    }

    public static boolean canStrike(Bullet bullet, Entity entity) {
        return entity.isAlive()
                && isTarget(entity)
                && isActive(bullet.getProfile())
                && Utils.canHarm(bullet.getOwner(), entity);
    }

    public static boolean strike(ServerLevel level, Bullet bullet, Entity target) {
        return level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .get(VENGEANCE_BEAM)
                .map(type -> target.hurtServer(
                        level, new DamageSource(type, bullet, bullet.getOwner()), bullet.resolveDamage()))
                .orElse(false);
    }
}
