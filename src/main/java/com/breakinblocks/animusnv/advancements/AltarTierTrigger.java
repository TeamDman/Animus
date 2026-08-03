package com.breakinblocks.animusnv.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Fires when an Ara Vitae near the player finishes forming at or above a given tier.
 */
public class AltarTierTrigger extends SimpleCriterionTrigger<AltarTierTrigger.Instance> {

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, int tier) {
        trigger(player, instance -> instance.matches(tier));
    }

    public record Instance(Optional<ContextAwarePredicate> player, int minTier)
        implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            Codec.INT.optionalFieldOf("min_tier", 1).forGetter(Instance::minTier)
        ).apply(instance, Instance::new));

        public boolean matches(int tier) {
            return tier >= minTier;
        }
    }
}
