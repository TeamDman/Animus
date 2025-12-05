package com.teamdman.animus;

import com.teamdman.animus.registry.AnimusAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for mod bus events (registration, setup, etc.)
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AnimusModEventHandler {

    /**
     * Add custom attributes to player entities
     */
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        // Add unarmed damage attribute to players
        event.add(EntityType.PLAYER, AnimusAttributes.UNARMED_DAMAGE.get());
    }
}
