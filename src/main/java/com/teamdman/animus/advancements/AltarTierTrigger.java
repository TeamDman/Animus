package com.teamdman.animus.advancements;

import com.google.gson.JsonObject;
import com.teamdman.animus.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * Fires when a Blood Altar near the player finishes forming at or above a given tier.
 */
public class AltarTierTrigger extends SimpleCriterionTrigger<AltarTierTrigger.Instance> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "altar_tier");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new Instance(player, GsonHelper.getAsInt(json, "min_tier", 1));
    }

    public void trigger(ServerPlayer player, int tier) {
        trigger(player, instance -> instance.matches(tier));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final int minTier;

        public Instance(ContextAwarePredicate player, int minTier) {
            super(ID, player);
            this.minTier = minTier;
        }

        public boolean matches(int tier) {
            return tier >= minTier;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("min_tier", minTier);
            return json;
        }
    }
}
