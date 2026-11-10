package com.breakinblocks.animusnv.events;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.rituals.RitualArsVitae;
import com.breakinblocks.neovitae.common.structure.RitualMultiblockBuilder;
import com.breakinblocks.neovitae.ritual.RitualComponent;
import com.klikli_dev.modonomicon.api.multiblock.Multiblock;
import com.klikli_dev.modonomicon.data.MultiblockDataManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public final class AnimusRitualBookSync {

    private static final Identifier ARS_VITAE_MULTIBLOCK =
            Identifier.fromNamespaceAndPath("neovitae", "ritual/" + Constants.Rituals.ARS_VITAE);

    private AnimusRitualBookSync() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onServerStarted(ServerStartedEvent event) {
        installMissing(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        installMissing(event.getPlayerList().getServer());
    }

    private static void installMissing(MinecraftServer server) {
        if (server == null) return;

        Map<Identifier, Multiblock> multiblocks = MultiblockDataManager.get().getMultiblocks();
        if (multiblocks.containsKey(ARS_VITAE_MULTIBLOCK)) return;

        List<RitualComponent> components = new ArrayList<>();
        new RitualArsVitae().gatherComponents(components::add);

        try {
            Multiblock mb = RitualMultiblockBuilder.build(components, server.registryAccess());
            mb.setId(ARS_VITAE_MULTIBLOCK);
            multiblocks.put(ARS_VITAE_MULTIBLOCK, mb);
        } catch (Throwable t) {
            Animus.LOGGER.error("Failed to build the Ars Vitae book multiblock", t);
        }
    }
}
