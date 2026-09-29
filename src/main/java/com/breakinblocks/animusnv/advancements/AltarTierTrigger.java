package com.breakinblocks.animusnv.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Fires when an Ara Vitae near the player finishes forming at or above a given tier.
 */
public class AltarTierTrigger implements CriterionTrigger<AltarTierTrigger.Instance> {

    private final Map<PlayerAdvancements, Set<Listener<Instance>>> players = Maps.newIdentityHashMap();

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements advancements, Listener<Instance> listener) {
        players.computeIfAbsent(advancements, key -> Sets.newHashSet()).add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements advancements, Listener<Instance> listener) {
        Set<Listener<Instance>> listeners = players.get(advancements);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                players.remove(advancements);
            }
        }
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements advancements) {
        players.remove(advancements);
    }

    public boolean hasListeners(ServerPlayer player) {
        return players.containsKey(player.getAdvancements());
    }

    public void trigger(ServerPlayer player, int tier) {
        PlayerAdvancements advancements = player.getAdvancements();
        Set<Listener<Instance>> listeners = players.get(advancements);
        if (listeners == null) {
            return;
        }

        LootContext context = EntityPredicate.createContext(player, player);
        List<Listener<Instance>> matched = new ArrayList<>();
        for (Listener<Instance> listener : listeners) {
            Instance instance = listener.trigger();
            if (instance.matches(tier) && instance.player().map(predicate -> predicate.matches(context)).orElse(true)) {
                matched.add(listener);
            }
        }

        for (Listener<Instance> listener : matched) {
            listener.run(advancements);
        }
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
