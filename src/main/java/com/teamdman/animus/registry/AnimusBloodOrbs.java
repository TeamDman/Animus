package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.ItemBloodOrbTranscendent;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Blood Orb registration for Animus.
 *
 * In NeoVitae 1.21.1, blood orbs are regular Items with their stats defined via DataMaps.
 * The orb stats (tier, capacity, fillRate) are defined in:
 * data/animus/data_maps/item/blood_orb_stats.json
 *
 * This replaces the old BloodOrbDeferredRegister system.
 */
public class AnimusBloodOrbs {
    public static final DeferredRegister<Item> BLOOD_ORBS = DeferredRegister.createItems(Constants.Mod.MODID);

    public static final DeferredHolder<Item, ItemBloodOrbTranscendent> BLOOD_ORB_TRANSCENDENT =
        BLOOD_ORBS.register("blood_orb_transcendent", ItemBloodOrbTranscendent::new);
}
