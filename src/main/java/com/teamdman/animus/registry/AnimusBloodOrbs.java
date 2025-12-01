package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import wayoftime.bloodmagic.common.item.BloodOrb;
import wayoftime.bloodmagic.common.registration.impl.BloodOrbDeferredRegister;
import wayoftime.bloodmagic.common.registration.impl.BloodOrbRegistryObject;

public class AnimusBloodOrbs {
    public static final BloodOrbDeferredRegister BLOOD_ORBS = new BloodOrbDeferredRegister(Constants.Mod.MODID);

    // Transcendent Blood Orb - Tier 6, 300,000 LP
    public static final BloodOrbRegistryObject<BloodOrb> BLOOD_ORB_TRANSCENDENT = BLOOD_ORBS.register(
        "blood_orb_transcendent",
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "blood_orb_transcendent"),
        6,      // tier
        300000, // capacity
        100     // fill rate
    );

    public static void register(IEventBus modEventBus) {
        BLOOD_ORBS.register(modEventBus);
    }
}
