package com.breakinblocks.animusnv;

import com.breakinblocks.animusnv.registry.AnimusAttributes;
import com.breakinblocks.animusnv.rituals.RitualPersistence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class AnimusModEventHandler {

    private static final TicketController TICKET_CONTROLLER = new TicketController(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "chunk_loader"),
        RitualPersistence::validateTickets
    );

    public static TicketController getTicketController() {
        return TICKET_CONTROLLER;
    }

    @SubscribeEvent
    public static void onRegisterTicketControllers(RegisterTicketControllersEvent event) {
        event.register(TICKET_CONTROLLER);
    }

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AnimusAttributes.UNARMED_DAMAGE);
    }
}
