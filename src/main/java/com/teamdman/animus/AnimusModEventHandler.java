package com.teamdman.animus;

import com.teamdman.animus.registry.AnimusAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

/**
 * Event handler for mod bus events (registration, setup, etc.)
 */
@EventBusSubscriber(modid = Constants.Mod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class AnimusModEventHandler {

    // Ticket controller for chunk loading (used by Ritual of Persistence)
    private static final TicketController TICKET_CONTROLLER = new TicketController(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "chunk_loader")
    );

    /**
     * Get the ticket controller for chunk loading operations
     */
    public static TicketController getTicketController() {
        return TICKET_CONTROLLER;
    }

    /**
     * Register the ticket controller for chunk loading
     */
    @SubscribeEvent
    public static void onRegisterTicketControllers(RegisterTicketControllersEvent event) {
        event.register(TICKET_CONTROLLER);
    }

    /**
     * Add custom attributes to player entities
     */
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        // Add unarmed damage attribute to players
        event.add(EntityType.PLAYER, AnimusAttributes.UNARMED_DAMAGE);
    }
}
