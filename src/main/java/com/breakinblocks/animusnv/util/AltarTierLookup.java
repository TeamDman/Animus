package com.breakinblocks.animusnv.util;

import com.breakinblocks.neovitae.common.registry.AltarTier;
import com.breakinblocks.neovitae.common.registry.NVRegistries;
import com.breakinblocks.neovitae.common.tag.NVTags;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class AltarTierLookup {
    private AltarTierLookup() {}

    public static Optional<AltarTier> findTier(RegistryAccess registries, int tier) {
        return validTiers(registries)
                .filter(t -> t.tier() == tier)
                .findFirst();
    }

    public static Optional<AltarTier> findNextTier(RegistryAccess registries, int currentTier) {
        return validTiers(registries)
                .filter(t -> t.tier() > currentTier)
                .min(Comparator.comparingInt(AltarTier::tier));
    }

    public static int maxTier(RegistryAccess registries) {
        return validTiers(registries)
                .mapToInt(AltarTier::tier)
                .max()
                .orElse(0);
    }

    private static Stream<AltarTier> validTiers(RegistryAccess registries) {
        Registry<AltarTier> registry = registries.registryOrThrow(NVRegistries.Keys.ALTAR_TIER_KEY);
        return registry.getOrCreateTag(NVTags.Tiers.VALID_TIERS)
                .stream()
                .map(Holder::value);
    }
}
