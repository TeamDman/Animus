package com.teamdman.animus.rituals;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
// TODO: Demon will system not available in Blood Magic 1.20.1
// import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
// import wayoftime.bloodmagic.soul.EnumDemonWillType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Ritual of the Steadfast Heart - Grants Absorption to nearby players
 * Provides increasingly powerful absorption effect to players in range
 * Also generates Steadfast demon will
 * Activation Cost: 20000 LP
 * Refresh Cost: 100 LP per player
 * Refresh Time: 600 ticks (30 seconds)
 */
@RitualRegister(Constants.Rituals.STEADFAST)
public class RitualSteadfastHeart extends Ritual {
    public static final String EFFECT_RANGE = "effect";
    public final int maxWill = 100;
    public double willBuffer = 0;

    public RitualSteadfastHeart() {
        super(Constants.Rituals.STEADFAST, 0, 20000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.STEADFAST);

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-16, -16, -16), 32));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 15, 15);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        willBuffer = tag.getDouble("willBuffer");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putDouble("willBuffer", willBuffer);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        Level level = mrs.getWorldObj();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = mrs.getMasterBlockPos();

        // TODO: Demon will system not available in Blood Magic 1.20.1
        // Get current steadfast demon will
        // EnumDemonWillType type = EnumDemonWillType.STEADFAST;
        // double currentAmount = WorldDemonWillHandler.getCurrentWill(level, pos, type);
        double currentAmount = 0;

        // Get all players in range
        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        AABB range = effectRange.getAABB(pos);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, range);

        int entityCount = 0;
        MobEffect absorbEffect = MobEffects.ABSORPTION;

        for (LivingEntity entity : entities) {
            // Only affect real players
            if (!(entity instanceof Player) || entity instanceof FakePlayer) {
                continue;
            }

            entityCount++;

            // Get existing absorption effect
            MobEffectInstance existingEffect = entity.getEffect(absorbEffect);
            int currentDuration = 0;

            if (existingEffect != null) {
                currentDuration = existingEffect.getDuration();
                entity.removeEffect(absorbEffect);
            }

            // Calculate new duration and amplifier
            int newDuration = Math.min(((currentDuration + 800) * 2), 30000);
            int amplifier = Math.min((5 * (1 + (newDuration + 60)) / 36000), 4);

            // Apply new absorption effect
            entity.addEffect(new MobEffectInstance(
                absorbEffect,
                newDuration,
                amplifier,
                true,
                false
            ));
        }

        // Consume LP based on number of players affected
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_STEADFAST),
            getRefreshCost() * entityCount
        );
        network.syphon(ticket, false);

        // TODO: Demon will system not available in Blood Magic 1.20.1
        // Generate steadfast demon will
        // double drainAmount = 2 * Math.min((maxWill - currentAmount) + 1, Math.min(entityCount / 2, 10));
        // double filled = WorldDemonWillHandler.fillWillToMaximum(level, pos, type, drainAmount, maxWill, false);
        // if (filled > 0) {
        //     WorldDemonWillHandler.fillWillToMaximum(level, pos, type, filled, maxWill, true);
        // }
    }

    @Override
    public int getRefreshCost() {
        return 100;
    }

    @Override
    public int getRefreshTime() {
        return 600;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 1, 0, 1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.WATER);
        addRune(components, 1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, -1, EnumRuneType.WATER);
        addRune(components, 0, -1, 0, EnumRuneType.AIR);
        addRune(components, 2, -1, 2, EnumRuneType.EARTH);
        addRune(components, 2, -1, -2, EnumRuneType.EARTH);
        addRune(components, -2, -1, 2, EnumRuneType.WATER);
        addRune(components, -2, -1, -2, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSteadfastHeart();
    }
}
