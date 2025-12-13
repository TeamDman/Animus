package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.ItemBloodOrbTranscendent;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Blood Orb registration for Animus.
 *
 * In Blood Magic 1.21.1, blood orbs are regular Items with their stats defined via DataMaps.
 * The orb stats (tier, capacity, fillRate) are defined in:
 * data/animus/data_maps/item/blood_orb_stats.json
 *
 * This replaces the old BloodOrbDeferredRegister system.
 */
public class AnimusBloodOrbs {
    public static final DeferredRegister<Item> BLOOD_ORBS = DeferredRegister.createItems(Constants.Mod.MODID);

    // Transcendent Blood Orb - Tier 7, 30,000,000 LP
    // Stats are defined via data maps in data/animus/data_maps/item/blood_orb_stats.json
    public static final DeferredHolder<Item, ItemBloodOrbTranscendent> BLOOD_ORB_TRANSCENDENT =
        BLOOD_ORBS.register("blood_orb_transcendent", ItemBloodOrbTranscendent::new);
}
