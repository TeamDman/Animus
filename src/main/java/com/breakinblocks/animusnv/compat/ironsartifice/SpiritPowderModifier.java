package com.breakinblocks.animusnv.compat.ironsartifice;

import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.spiritus.IPlayerSpiritusHandler;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.soul.SentientToolHelper;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ParticleBurst;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.GunModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public final class SpiritPowderModifier implements GunModifier {
    public static final double[] DAMAGE_BONUS = {0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
    public static final int SNARE_DURATION_TICKS = 1200;
    private static final int TRAIL_FROM = 0xb8f4f2;
    private static final int TRAIL_TO = 0x1d5a73;
    private static final int MUZZLE_TINT = 0x5fd0dd;

    @Override
    public void apply(ShotComponentMap components) {
        components.modifyValue(
                AnimusShotComponents.SPIRIT_POWDER,
                new ValueModifier(1, ValueModifier.Operation.ADD, ValueModifier.Type.NEUTRAL));
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(ColorTransitionParticleOption.bulletTrail(TRAIL_FROM, TRAIL_TO));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(MUZZLE_TINT);
        components.getOrCreate(ShotComponents.MUZZLE_FLASH)
                .addAirBurst(new ParticleBurst(ParticleTypes.SOUL, 2, 0.3f, false));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("tooltip.animusnv.spirit_powder_modifier.snare")
                .withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable(
                        "tooltip.animusnv.spirit_powder_modifier.scaling",
                        (int) Math.round(DAMAGE_BONUS[0] * 100),
                        (int) Math.round(DAMAGE_BONUS[DAMAGE_BONUS.length - 1] * 100))
                .withStyle(ChatFormatting.GREEN));
    }

    public static boolean isActive(ShotProfile profile) {
        return profile.value(AnimusShotComponents.SPIRIT_POWDER) > 0;
    }

    public static double getDamageBonus(Player player) {
        IPlayerSpiritusHandler handler = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler();
        SpiritusType type = handler.getLargestSpiritusType(player);
        int level = SentientToolHelper.getLevel(handler.getTotalSpiritus(type, player));
        if (level < 0) {
            return 0;
        }
        return DAMAGE_BONUS[Math.min(level, DAMAGE_BONUS.length - 1)];
    }
}
