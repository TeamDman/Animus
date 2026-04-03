package com.breakinblocks.animusnv.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.breakinblocks.neovitae.api.registry.NeoVitaeRegistries;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.ironsspells.CrimsonWillSigilEffect;
import com.breakinblocks.animusnv.items.sigils.effects.*;

import java.util.function.Supplier;

public class AnimusSigilEffects {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<MapCodec<? extends ISigilEffect>> SIGIL_EFFECTS =
            DeferredRegister.create(
                    (ResourceKey<Registry<MapCodec<? extends ISigilEffect>>>) (ResourceKey<?>) NeoVitaeRegistries.SIGIL_EFFECT_TYPE_KEY,
                    Constants.Mod.MODID
            );

    public static final Supplier<MapCodec<StormSigilEffect>> STORM =
            SIGIL_EFFECTS.register("storm", () -> StormSigilEffect.CODEC);

    public static final Supplier<MapCodec<ChainsSigilEffect>> CHAINS =
            SIGIL_EFFECTS.register("chains", () -> ChainsSigilEffect.CODEC);

    public static final Supplier<MapCodec<ConsumptionSigilEffect>> CONSUMPTION =
            SIGIL_EFFECTS.register("consumption", () -> ConsumptionSigilEffect.CODEC);

    public static final Supplier<MapCodec<HeavenlyWrathSigilEffect>> HEAVENLY_WRATH =
            SIGIL_EFFECTS.register("heavenly_wrath", () -> HeavenlyWrathSigilEffect.CODEC);

    public static final Supplier<MapCodec<BuilderSigilEffect>> BUILDER =
            SIGIL_EFFECTS.register("builder", () -> BuilderSigilEffect.CODEC);

    public static final Supplier<MapCodec<LeachSigilEffect>> LEACH =
            SIGIL_EFFECTS.register("leach", () -> LeachSigilEffect.CODEC);

    public static final Supplier<MapCodec<RemediumSigilEffect>> REMEDIUM =
            SIGIL_EFFECTS.register("remedium", () -> RemediumSigilEffect.CODEC);

    public static final Supplier<MapCodec<RepareSigilEffect>> REPARE =
            SIGIL_EFFECTS.register("reparare", () -> RepareSigilEffect.CODEC);

    public static final Supplier<MapCodec<TranspositionSigilEffect>> TRANSPOSITION =
            SIGIL_EFFECTS.register("transposition", () -> TranspositionSigilEffect.CODEC);

    public static final Supplier<MapCodec<FreeSoulSigilEffect>> FREE_SOUL =
            SIGIL_EFFECTS.register("free_soul", () -> FreeSoulSigilEffect.CODEC);

    public static final Supplier<MapCodec<TemporalDominanceSigilEffect>> TEMPORAL_DOMINANCE =
            SIGIL_EFFECTS.register("temporal_dominance", () -> TemporalDominanceSigilEffect.CODEC);

    public static final Supplier<MapCodec<EquivalencySigilEffect>> EQUIVALENCY =
            SIGIL_EFFECTS.register("equivalency", () -> EquivalencySigilEffect.CODEC);

    public static final Supplier<MapCodec<MonkSigilEffect>> MONK =
            SIGIL_EFFECTS.register("monk", () -> MonkSigilEffect.CODEC);

    public static final Supplier<MapCodec<CrimsonWillSigilEffect>> CRIMSON_WILL =
            SIGIL_EFFECTS.register("crimson_will", () -> CrimsonWillSigilEffect.CODEC);

    public static void init() {
        // Force access to ensure static initializers run
        var storm = STORM;
        var chains = CHAINS;
        var consumption = CONSUMPTION;
        var heavenlyWrath = HEAVENLY_WRATH;
        var builder = BUILDER;
        var leach = LEACH;
        var remedium = REMEDIUM;
        var repare = REPARE;
        var transposition = TRANSPOSITION;
        var freeSoul = FREE_SOUL;
        var temporalDominance = TEMPORAL_DOMINANCE;
        var equivalency = EQUIVALENCY;
        var monk = MONK;
        var crimsonWill = CRIMSON_WILL;
    }

    public static void register(IEventBus modEventBus) {
        init();
        SIGIL_EFFECTS.register(modEventBus);
    }
}
